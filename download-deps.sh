#!/usr/bin/env bash
set -euo pipefail

script_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
cd "$script_dir"

echo "=== Checking prerequisites ==="
if ! command -v java >/dev/null 2>&1; then
  echo "Error: Java is not installed or not in PATH." >&2
  echo "Please install JDK 25 (or JDK 21+) before continuing." >&2
  echo "Download: https://adoptium.net/temurin/releases/?version=25" >&2
  exit 1
fi

java_version=$(java -version 2>&1 | head -n 1)
echo "Found Java: $java_version"

# 1. Prepare server directories
mkdir -p server/plugins

# 2. Accept Minecraft EULA
if [[ ! -f server/eula.txt ]] || ! grep -q '^eula=true$' server/eula.txt 2>/dev/null; then
  echo "Accepting Minecraft EULA in server/eula.txt..."
  cat > server/eula.txt <<'EOF'
# By changing the setting below to TRUE you are indicating your agreement to our EULA (https://aka.ms/MinecraftEULA).
eula=true
EOF
fi

# 3. Create server start script if missing
if [[ ! -f server/start.sh ]]; then
  echo "Creating server/start.sh..."
  cat > server/start.sh <<'EOF'
#!/usr/bin/env bash
set -e
java -Xms1G -Xmx2G -jar paper-26.2-134.jar --nogui
EOF
  chmod +x server/start.sh
fi

if [[ ! -f server/start.bat ]]; then
  echo "Creating server/start.bat for Windows..."
  cat > server/start.bat <<'EOF'
@echo off
java -Xms1G -Xmx2G -jar paper-26.2-134.jar --nogui
pause
EOF
fi

# 4. Download Paper server jar
PAPER_VERSION="26.2"
PAPER_BUILD="134"
PAPER_JAR="paper-${PAPER_VERSION}-${PAPER_BUILD}.jar"
if [[ ! -f "server/${PAPER_JAR}" ]]; then
  echo "Downloading Paper ${PAPER_VERSION} (build ${PAPER_BUILD})..."
  PAPER_URL="${PAPER_DOWNLOAD_URL:-}"
  if [[ -z "$PAPER_URL" ]]; then
    # PaperMC Fill v3 API (the old v2 API returns 410 Gone)
    BUILD_API="https://fill.papermc.io/v3/projects/paper/versions/${PAPER_VERSION}/builds/${PAPER_BUILD}"
    if command -v curl >/dev/null 2>&1; then
      BUILD_JSON="$(curl -fsSL -A mc-server-plugin-setup "$BUILD_API")"
    else
      BUILD_JSON="$(wget -qO- -U mc-server-plugin-setup "$BUILD_API")"
    fi
    PAPER_URL="$(printf '%s' "$BUILD_JSON" | grep -o "\"url\":\"[^\"]*${PAPER_JAR}\"" | head -n1 | cut -d'"' -f4)"
    if [[ -z "$PAPER_URL" ]]; then
      echo "Error: could not resolve Paper download URL from $BUILD_API" >&2
      exit 1
    fi
  fi
  if command -v curl >/dev/null 2>&1; then
    curl --fail --location --progress-bar --output "server/${PAPER_JAR}" "$PAPER_URL"
  elif command -v wget >/dev/null 2>&1; then
    wget -O "server/${PAPER_JAR}" "$PAPER_URL"
  else
    echo "Error: Neither curl nor wget found to download Paper jar." >&2
    exit 1
  fi
  echo "Paper downloaded successfully."
else
  echo "Paper jar already exists: server/${PAPER_JAR}"
fi

# 5. Download Gradle dependencies & build the plugin
echo "Downloading project dependencies and building plugin..."
chmod +x ./gradlew
./gradlew build --no-daemon

# 6. Copy plugin to server/plugins
mkdir -p server/plugins
plugin_jar=$(ls -1 build/libs/mc-server-plugin-*.jar 2>/dev/null | head -n 1)
if [[ -n "$plugin_jar" && -f "$plugin_jar" ]]; then
  echo "Installing plugin to server/plugins/mc-server.jar..."
  cp "$plugin_jar" server/plugins/mc-server.jar
fi

echo
echo "========================================="
echo " Dependency download and setup complete! "
echo "========================================="
echo "To start the Minecraft server:"
echo "  ./start_server.sh"
echo "To rebuild and update the plugin after editing code:"
echo "  ./gradlew build && ./update-plugin.sh"

