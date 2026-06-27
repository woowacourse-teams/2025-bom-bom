#!/usr/bin/env bash
set -euo pipefail

HEALTH_CHECK_URL="${HEALTH_CHECK_URL:-http://localhost:8081/actuator/health}"
SERVER_CONTAINER_NAME="${SERVER_CONTAINER_NAME:-prod}"
SUCCESS_COUNT=0
INITIAL_DELAY_SECONDS="${INITIAL_DELAY_SECONDS:-30}"
MAX_RETRIES="${MAX_RETRIES:-7}"
REQUIRED_SUCCESS="${REQUIRED_SUCCESS:-2}"
RETRY_INTERVAL_SECONDS="${RETRY_INTERVAL_SECONDS:-10}"

sleep "$INITIAL_DELAY_SECONDS"

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

  sleep "$RETRY_INTERVAL_SECONDS"
done

docker logs "$SERVER_CONTAINER_NAME" --tail 80 || true
exit 1
