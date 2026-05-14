#!/usr/bin/env bash
set -euo pipefail

HEALTH_CHECK_URL="${HEALTH_CHECK_URL:-http://localhost:8081/actuator/health}"
SERVER_CONTAINER_NAME="${SERVER_CONTAINER_NAME:-bombom-server-prod-sub}"
SUCCESS_COUNT=0
MAX_RETRIES="${MAX_RETRIES:-10}"
REQUIRED_SUCCESS="${REQUIRED_SUCCESS:-5}"

sleep 20

for i in $(seq 1 "$MAX_RETRIES"); do
  STATUS="$(curl -s -o /dev/null -w "%{http_code}" "$HEALTH_CHECK_URL" || echo "000")"

  if [ "$STATUS" = "200" ]; then
    SUCCESS_COUNT=$((SUCCESS_COUNT + 1))

    if [ "$SUCCESS_COUNT" -ge "$REQUIRED_SUCCESS" ]; then
      exit 0
    fi
  else
    SUCCESS_COUNT=0
  fi

  sleep 10
done

docker logs "$SERVER_CONTAINER_NAME" --tail 80 || true
exit 1
