#!/usr/bin/env bash
# Resolves and validates the requested CD image inputs, then exposes them as
# GitHub Actions job outputs through $GITHUB_OUTPUT.
set -Eeuo pipefail

version="${DISPATCH_VERSION:-${INPUT_VERSION:-}}"
image="${DISPATCH_IMAGE:-}"

[[ -n "${GITHUB_OUTPUT:-}" ]] || {
  echo "GITHUB_OUTPUT is required when running this script from GitHub Actions." >&2
  exit 1
}
[[ -n "$version" ]] || {
  echo "A version must be provided." >&2
  exit 1
}
[[ "$version" =~ ^[0-9A-Za-z][0-9A-Za-z._-]*$ ]] || {
  echo "Invalid image version: $version" >&2
  exit 1
}
if [[ -n "$image" && ! "$image" =~ ^ghcr\.io/.+@sha256:[a-f0-9]{64}$ ]]; then
  echo "Release dispatch image must be a ghcr.io digest reference." >&2
  exit 1
fi

printf 'image=%s\nversion=%s\n' "$image" "$version" >>"$GITHUB_OUTPUT"
