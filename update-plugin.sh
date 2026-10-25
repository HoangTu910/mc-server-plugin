#!/usr/bin/env sh
set -eu

project_dir=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)

if command -v gradle >/dev/null 2>&1; then
    gradle jar
fi

cp "$project_dir"/build/libs/mc-server-plugin-*.jar \
   "$project_dir"/server/plugins/mc-server.jar
echo "Updated mc-server.jar"
