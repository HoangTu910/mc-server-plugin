# Requires -Version 5.1
$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

Write-Host "=== Checking prerequisites ===" -ForegroundColor Cyan
try {
    $javaVer = java -version 2>&1 | Select-Object -First 1
    Write-Host "Found Java: $javaVer" -ForegroundColor Green
} catch {
    Write-Error "Java is not installed or not in PATH.`nPlease install JDK 25 (or JDK 21+) from: https://adoptium.net/temurin/releases/?version=25"
    exit 1
}

# 1. Prepare server directories
New-Item -ItemType Directory -Force server\plugins | Out-Null

# 2. Accept Minecraft EULA
$eulaPath = "server\eula.txt"
if (-not (Test-Path $eulaPath) -or -not ((Get-Content $eulaPath -Raw) -match "eula=true")) {
    Write-Host "Accepting Minecraft EULA in server\eula.txt..." -ForegroundColor Yellow
    @"
# By changing the setting below to TRUE you are indicating your agreement to our EULA (https://aka.ms/MinecraftEULA).
eula=true
"@ | Set-Content $eulaPath -Encoding UTF8
}

# 3. Create server start scripts if missing
$startBat = "server\start.bat"
if (-not (Test-Path $startBat)) {
    Write-Host "Creating server\start.bat..."
    @"
@echo off
java -Xms1G -Xmx2G -jar paper-26.2-134.jar --nogui
pause
"@ | Set-Content $startBat -Encoding ASCII
}

$startSh = "server\start.sh"
if (-not (Test-Path $startSh)) {
    @"
#!/usr/bin/env bash
set -e
java -Xms1G -Xmx2G -jar paper-26.2-134.jar --nogui
"@ | Set-Content $startSh -Encoding UTF8
}

# 4. Download Paper server jar
$paperVersion = "26.2"
$paperBuild = "134"
$paperJar = "paper-$paperVersion-$paperBuild.jar"
$targetJarPath = "server\$paperJar"

$paperUrl = if ($env:PAPER_DOWNLOAD_URL) {
    $env:PAPER_DOWNLOAD_URL
} else {
    "https://api.papermc.io/v2/projects/paper/versions/$paperVersion/builds/$paperBuild/downloads/$paperJar"
}

if (-not (Test-Path $targetJarPath)) {
    Write-Host "Downloading Paper $paperVersion (build $paperBuild)..." -ForegroundColor Cyan
    try {
        if (Get-Command curl.exe -ErrorAction SilentlyContinue) {
            curl.exe -fL -o $targetJarPath $paperUrl
        } else {
            [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
            Invoke-WebRequest -Uri $paperUrl -OutFile $targetJarPath
        }
        Write-Host "Paper downloaded successfully." -ForegroundColor Green
    } catch {
        Write-Error "Failed to download Paper server jar: $_"
        exit 1
    }
} else {
    Write-Host "Paper jar already exists: $targetJarPath" -ForegroundColor Green
}

# 5. Download Gradle dependencies & build the plugin
Write-Host "Downloading project dependencies and building plugin..." -ForegroundColor Cyan
& .\gradlew.bat build --no-daemon
if ($LASTEXITCODE -ne 0) {
    Write-Error "Gradle build failed."
    exit $LASTEXITCODE
}

# 6. Copy plugin to server\plugins
$builtJars = Get-ChildItem "build\libs\mc-server-plugin-*.jar" -ErrorAction SilentlyContinue
if ($builtJars) {
    $pluginJar = $builtJars[0].FullName
    Write-Host "Installing plugin to server\plugins\mc-server.jar..." -ForegroundColor Cyan
    Copy-Item $pluginJar -Destination "server\plugins\mc-server.jar" -Force
}

Write-Host ""
Write-Host "=========================================" -ForegroundColor Green
Write-Host " Dependency download and setup complete! " -ForegroundColor Green
Write-Host "=========================================" -ForegroundColor Green
Write-Host "To start the Minecraft server:"
Write-Host "  .\start_server.bat"
Write-Host "To rebuild and update the plugin after editing code:"
Write-Host "  .\gradlew.bat build; .\update-plugin.bat"

