#!/usr/bin/env sh
set -eu

project_dir=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)

cp "$project_dir"/build/libs/mc-server-plugin-*.jar \
   "$project_dir"/server/plugins/mc-server.jar