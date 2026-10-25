# MC Survival Plugin

A Minecraft Paper plugin for a Survival / Fantasy RPG server.

## Overview & Architecture

- **Platform**: Minecraft Paper Server (`26.2` build `134`).
- **Language & Runtime**: Java 25 (Gradle toolchain configured for Java 25).
- **Build System**: Gradle 9.8.1 (Gradle Wrapper included: `./gradlew` / `.\gradlew.bat`).
- **Plugin Artifact**: `build/libs/mc-server-plugin-0.1.0.jar` deployed to `server/plugins/mc-server.jar`.
- **Server Runtime Directory**: `server/` (local directory, excluded from git).

---

## Directory Structure

```text
.
├── build.gradle.kts      # Gradle build configuration (Java 25, Paper API 26.2)
├── src/main/java/        # Plugin source code
├── src/main/resources/   # plugin.yml and config.yml
├── setup.sh              # Unix setup script (downloads Paper & builds dependencies)
├── setup.ps1             # Windows PowerShell setup script
├── download-deps.sh      # Core dependency resolution & bootstrap script (Unix)
├── download-deps.ps1     # Core dependency resolution & bootstrap script (Windows)
├── start_server.sh       # Unix script to launch the local Paper server
├── start_server.bat      # Windows batch script to launch the local Paper server
├── update-plugin.sh      # Unix script to copy built jar to server/plugins/
├── update-plugin.bat     # Windows batch script to copy built jar to server/plugins/
└── server/               # Runtime Paper server folder (created during setup)
    ├── paper-26.2-134.jar
    ├── eula.txt
    └── plugins/
        └── mc-server.jar
```

---

## Instructions for AI Agents & Automated Environments

> [!IMPORTANT]
> **Deterministic Execution**:
> - All setup scripts are idempotent and non-interactive.
> - The server process (`start_server.sh` / `start_server.bat`) is a **long-running blocking process**. Do NOT run it synchronously within tool execution; launch it as a background task/daemon if testing runtime server behavior.

### 1. Prerequisites Check
Ensure Java 25 (or at least Java 21) is installed and available:
```bash
java -version
```

### 2. Initial Setup & Dependency Download
Run the setup script once to download the Paper jar, accept the EULA, resolve Gradle dependencies, and compile the plugin:

- **Linux / macOS**:
  ```bash
  ./setup.sh
  ```
- **Windows (PowerShell)**:
  ```powershell
  .\setup.ps1
  ```

### 3. Build & Test Verification
To verify code changes and compile the plugin without starting the server:
- **Linux / macOS**:
  ```bash
  ./gradlew build --no-daemon
  ```
- **Windows (PowerShell / CMD)**:
  ```powershell
  .\gradlew.bat build --no-daemon
  ```

### 4. Deploying Built Plugin to Server
After a successful build, copy the compiled jar into the local test server:
- **Linux / macOS**:
  ```bash
  ./update-plugin.sh
  ```
- **Windows**:
  ```cmd
  update-plugin.bat
  ```
*(Combined one-liner for rapid iteration: `./gradlew build --no-daemon && ./update-plugin.sh`)*

### 5. Running the Local Minecraft Server
- **Linux / macOS**:
  ```bash
  ./start_server.sh
  ```
- **Windows**:
  ```cmd
  start_server.bat
  ```
Default port: `25565` (configured in `server/server.properties`).
To stop the server safely when running interactively, type `stop` in the console.

---

## Configuration & Notes

- **EULA**: `server/eula.txt` is automatically created with `eula=true` by the setup script.
- **Custom Paper Build**: To use a custom Paper build, set the `PAPER_DOWNLOAD_URL` environment variable before running `setup.sh` / `setup.ps1`.

