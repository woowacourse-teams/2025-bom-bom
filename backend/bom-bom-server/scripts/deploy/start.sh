#!/usr/bin/env bash
set -euo pipefail

AWS_REGION="${AWS_REGION:-ap-northeast-2}"
PARAMETER_PATH="${PARAMETER_PATH:-/bom-bom/prod}"
APP_DIR="/opt/bom-bom/app"
ENV_FILE="/opt/bom-bom/deploy/.env"
COMPOSE_FILE="${COMPOSE_FILE:-docker-compose-prod-sub.yml}"

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
OAUTH2_APPLE_PRIVATE_KEY="$(get_parameter OAUTH2_APPLE_PRIVATE_KEY)"

install -m 600 /dev/null "$ENV_FILE"
{
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
  down || true

docker compose \
  -f "$COMPOSE_FILE" \
  --env-file "$ENV_FILE" \
  up -d --remove-orphans --pull=always --force-recreate
