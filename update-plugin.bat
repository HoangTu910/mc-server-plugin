@echo off
copy /Y "%~dp0build\libs\mc-server-plugin-*.jar" "%~dp0server\plugins\mc-server.jar"
echo Plugin copied to server\plugins\mc-server.jar

