#!/usr/bin/env bash
set -euo pipefail

AWS_REGION="${AWS_REGION:-ap-northeast-2}"
PARAMETER_PATH="${PARAMETER_PATH:-/bom-bom/prod}"
APP_DIR="${APP_DIR:-/opt/bom-bom/app}"
ENV_FILE="${ENV_FILE:-/opt/bom-bom/deploy/.env}"
RELEASE_ENV_FILE="${RELEASE_ENV_FILE:-/opt/bom-bom/app/release.env}"
COMPOSE_FILE="${COMPOSE_FILE:-docker-compose-prod.yml}"
SERVER_CONTAINER_NAME="${SERVER_CONTAINER_NAME:-prod}"
LEGACY_CONTAINER_NAMES="${LEGACY_CONTAINER_NAMES:-bombom-server-prod bombom-server-prod-sub}"

get_parameter() {
  local name="$1"
  aws ssm get-parameter \
    --name "${PARAMETER_PATH}/${name}" \
    --with-decryption \
    --query "Parameter.Value" \
    --output text \
    --region "$AWS_REGION"
}

RDS_ENDPOINT="$(get_parameter RDS_ENDPOINT)"
DATABASE="$(get_parameter DATABASE)"
MYSQL_USER="$(get_parameter MYSQL_USER)"
MYSQL_PASSWORD="$(get_parameter MYSQL_PASSWORD)"
PROD_OTEL_ENDPOINT="$(get_parameter PROD_OTEL_ENDPOINT)"
export OAUTH2_APPLE_PRIVATE_KEY
OAUTH2_APPLE_PRIVATE_KEY="$(printf '%b' "$(get_parameter OAUTH2_APPLE_PRIVATE_KEY)")"

if [ ! -f "$RELEASE_ENV_FILE" ]; then
  echo "release.env not found: $RELEASE_ENV_FILE" >&2
  exit 1
fi

DOCKER_IMAGE="$(sed -n 's/^DOCKER_IMAGE=//p' "$RELEASE_ENV_FILE" | tail -n 1)"
if [ -z "$DOCKER_IMAGE" ]; then
  echo "DOCKER_IMAGE is empty in $RELEASE_ENV_FILE" >&2
  exit 1
fi

if [[ ! "$DOCKER_IMAGE" =~ ^[A-Za-z0-9._/-]+/bom-bom:prod-[A-Za-z0-9._-]+$ ]]; then
  echo "Invalid DOCKER_IMAGE: $DOCKER_IMAGE" >&2
  exit 1
fi

install -m 600 /dev/null "$ENV_FILE"
{
  printf 'DOCKER_IMAGE=%s\n' "$DOCKER_IMAGE"
  printf 'RDS_ENDPOINT=%s\n' "$RDS_ENDPOINT"
  printf 'DATABASE=%s\n' "$DATABASE"
  printf 'MYSQL_USER=%s\n' "$MYSQL_USER"
  printf 'MYSQL_PASSWORD=%s\n' "$MYSQL_PASSWORD"
  printf 'SPRING_PROFILES_ACTIVE=prod\n'
  printf 'PROD_OTEL_ENDPOINT=%s\n' "$PROD_OTEL_ENDPOINT"
} > "$ENV_FILE"

cd "$APP_DIR"

docker compose \
  -f "$COMPOSE_FILE" \
  --env-file "$ENV_FILE" \
  config >/dev/null

docker compose \
  -f "$COMPOSE_FILE" \
  --env-file "$ENV_FILE" \
  down || true

for container_name in $SERVER_CONTAINER_NAME $LEGACY_CONTAINER_NAMES; do
  if docker ps -a --format "{{.Names}}" | grep -Fxq "$container_name"; then
    docker stop "$container_name" || true
    docker rm "$container_name" || true
  fi
done

docker compose \
  -f "$COMPOSE_FILE" \
  --env-file "$ENV_FILE" \
  up -d --remove-orphans --pull=always --force-recreate
