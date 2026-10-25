#!/usr/bin/env bash
# Pulls a signed image into the target server's Docker cache.
# This script never creates, restarts, or removes containers.
set -Eeuo pipefail

require_var() {
  local name="$1"
  [[ -n "${!name:-}" ]] || {
    echo "Required environment variable '$name' is not set." >&2
    exit 1
  }
}

validate_digest_reference() {
  local image_ref="$1"
  local expected_repository="$2"
  local digest

  [[ "$image_ref" == "${expected_repository}@sha256:"* ]] || {
    echo "Image must belong to the expected repository:" >&2
    echo "  Expected: ${expected_repository}@sha256:<digest>" >&2
    echo "  Actual:   ${image_ref}" >&2
    exit 1
  }

  digest="${image_ref#*@sha256:}"

  [[ "$digest" =~ ^[0-9a-f]{64}$ ]] || {
    echo "Invalid SHA-256 digest in image reference: ${image_ref}" >&2
    exit 1
  }
}

require_var REGISTRY
require_var IMAGE_NAME
require_var VERSION
require_var GHCR_USERNAME
require_var GHCR_TOKEN
require_var COSIGN_CERT_IDENTITY_REGEXP
require_var COSIGN_CERT_OIDC_ISSUER

image_name="${IMAGE_NAME,,}"
image_repository="${REGISTRY}/${image_name}"
image_tag="${image_repository}:${VERSION}"

# Do not modify the persistent Docker credentials on the self-hosted runner.
export DOCKER_CONFIG
DOCKER_CONFIG="$(mktemp -d)"
chmod 700 "$DOCKER_CONFIG"
trap 'rm -rf "$DOCKER_CONFIG"' EXIT

printf '%s' "$GHCR_TOKEN" |
  docker login \
    "$REGISTRY" \
    --username "$GHCR_USERNAME" \
    --password-stdin

if [[ -n "${REQUESTED_IMAGE:-}" ]]; then
  # REQUESTED_IMAGE must already be immutable.
  image="$REQUESTED_IMAGE"
  validate_digest_reference "$image" "$image_repository"
else
  # Fallback: pull the version tag and resolve it to its immutable digest.
  docker pull "$image_tag"

  image="$(
    docker image inspect \
      --format '{{index .RepoDigests 0}}' \
      "$image_tag"
  )"

  validate_digest_reference "$image" "$image_repository"
fi

echo "Verifying signed image:"
echo "  ${image}"

cosign verify \
  --certificate-identity-regexp "$COSIGN_CERT_IDENTITY_REGEXP" \
  --certificate-oidc-issuer "$COSIGN_CERT_OIDC_ISSUER" \
  "$image" >/dev/null

echo "Cosign verification succeeded."

# Pull exactly the digest that passed Cosign verification.
docker pull "$image"

# Add a readable local tag after verification.
docker image tag "$image" "$image_tag"

docker image inspect "$image_tag" \
  --format 'Local tag: {{index .RepoTags 0}} | Registry digest: {{index .RepoDigests 0}}'