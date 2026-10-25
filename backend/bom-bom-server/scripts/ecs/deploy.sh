#!/usr/bin/env bash
# ECS 운영 앱 배포: 새 이미지로 task definition을 등록하고 고른 방식으로 서비스를 업데이트한 뒤 결과를 기다린다.
# 사용법: deploy.sh <이미지 URI 또는 task definition 리비전 번호> <blue-green|rolling|inplace>
set -euo pipefail

AWS_REGION="${AWS_REGION:-ap-northeast-2}"
CLUSTER="${CLUSTER:-bombom-prod}"
SERVICE="${SERVICE:-bombom-prod-app}"
FAMILY="${FAMILY:-bombom-prod-app}"
CONTAINER_NAME="${CONTAINER_NAME:-app}"
BAKE_TIME_MINUTES="${BAKE_TIME_MINUTES:-15}"
WAIT_TIMEOUT_SECONDS="${WAIT_TIMEOUT_SECONDS:-3600}"
POLL_INTERVAL_SECONDS="${POLL_INTERVAL_SECONDS:-15}"
# 블루그린은 ECS가 운영 리스너 규칙의 대상 그룹을 바꾸므로, 대상 그룹에 ECS 밖 서버(prod1·prod2)가 없을 때만 켠다
BLUE_GREEN_READY="${BLUE_GREEN_READY:-false}"
BLUE_TARGET_GROUP_ARN="${BLUE_TARGET_GROUP_ARN:-arn:aws:elasticloadbalancing:ap-northeast-2:715432481104:targetgroup/bombom-prod-tg/a2e242fb45ab8bbe}"
GREEN_TARGET_GROUP_ARN="${GREEN_TARGET_GROUP_ARN:-arn:aws:elasticloadbalancing:ap-northeast-2:715432481104:targetgroup/bombom-prod-tg-green/ffd5cf588aec07d8}"
PRODUCTION_LISTENER_RULE_ARN="${PRODUCTION_LISTENER_RULE_ARN:-arn:aws:elasticloadbalancing:ap-northeast-2:715432481104:listener-rule/app/bombom-prod/4fe928a12ab8c4b5/abe138f9d3faa61d/f554be468be5a6d3}"
INFRASTRUCTURE_ROLE_ARN="${INFRASTRUCTURE_ROLE_ARN:-arn:aws:iam::715432481104:role/bombomProdEcsInfrastructure}"

TARGET="${1:?이미지 URI 또는 task definition 리비전 번호가 필요합니다}"
STRATEGY="${2:-blue-green}"
export AWS_REGION

register_task_definition() {
  local image="$1"
  local current
  current="$(aws ecs describe-task-definition --task-definition "$FAMILY" --include TAGS --output json)"
  CURRENT_JSON="$current" python3 - "$image" "$CONTAINER_NAME" > /tmp/ecs-task-definition.json <<'PY'
import json
import os
import sys

image, container_name = sys.argv[1], sys.argv[2]
described = json.loads(os.environ["CURRENT_JSON"])
task_definition = described["taskDefinition"]
for key in ("taskDefinitionArn", "revision", "status", "requiresAttributes", "compatibilities",
            "registeredAt", "registeredBy", "deregisteredAt"):
    task_definition.pop(key, None)
containers = [container for container in task_definition["containerDefinitions"] if container["name"] == container_name]
if len(containers) != 1:
    sys.exit(f"container {container_name} not found")
containers[0]["image"] = image
if described.get("tags"):
    task_definition["tags"] = described["tags"]
print(json.dumps(task_definition))
PY
  aws ecs register-task-definition \
    --cli-input-json file:///tmp/ecs-task-definition.json \
    --query 'taskDefinition.taskDefinitionArn' \
    --output text
}

deployment_configuration() {
  case "$1" in
    blue-green)
      printf '{"strategy":"BLUE_GREEN","bakeTimeInMinutes":%s,"minimumHealthyPercent":100,"maximumPercent":200,"deploymentCircuitBreaker":{"enable":true,"rollback":true}}' "$BAKE_TIME_MINUTES"
      ;;
    rolling)
      printf '{"strategy":"ROLLING","minimumHealthyPercent":50,"maximumPercent":100,"deploymentCircuitBreaker":{"enable":true,"rollback":true}}'
      ;;
    inplace)
      printf '{"strategy":"ROLLING","minimumHealthyPercent":0,"maximumPercent":100,"deploymentCircuitBreaker":{"enable":true,"rollback":true}}'
      ;;
    *)
      echo "알 수 없는 배포 방식: $1 (blue-green|rolling|inplace)" >&2
      exit 1
      ;;
  esac
}

case "$STRATEGY" in
  blue-green|rolling|inplace) ;;
  *) echo "알 수 없는 배포 방식: $STRATEGY (blue-green|rolling|inplace)" >&2; exit 1 ;;
esac

if [ "$STRATEGY" = "blue-green" ] && [ "$BLUE_GREEN_READY" != "true" ]; then
  echo "블루그린 준비 전(BLUE_GREEN_READY=false)이라 rolling으로 배포합니다." >&2
  STRATEGY="rolling"
fi

if [[ "$TARGET" =~ ^[0-9]+$ ]]; then
  TASK_DEFINITION_ARN="$(aws ecs describe-task-definition --task-definition "${FAMILY}:${TARGET}" --query 'taskDefinition.taskDefinitionArn' --output text)"
else
  TASK_DEFINITION_ARN="$(register_task_definition "$TARGET")"
fi
echo "task definition: $TASK_DEFINITION_ARN"
echo "배포 방식: $STRATEGY"

STARTED_AT="$(date -u +%Y-%m-%dT%H:%M:%SZ)"
UPDATE_ARGS=(
  --cluster "$CLUSTER"
  --service "$SERVICE"
  --task-definition "$TASK_DEFINITION_ARN"
  --deployment-configuration "$(deployment_configuration "$STRATEGY")"
)
# 대상 그룹 연결은 롤링에서 블루그린으로 처음 바꿀 때만 보낸다. 이후 어느 대상 그룹이 운영 중인지는 ECS가 관리한다.
CURRENT_STRATEGY="$(aws ecs describe-services --cluster "$CLUSTER" --services "$SERVICE" --query 'services[0].deploymentConfiguration.strategy' --output text)"
if [ "$STRATEGY" = "blue-green" ] && [ "$CURRENT_STRATEGY" != "BLUE_GREEN" ]; then
  UPDATE_ARGS+=(--load-balancers "[{\"targetGroupArn\":\"$BLUE_TARGET_GROUP_ARN\",\"containerName\":\"$CONTAINER_NAME\",\"containerPort\":8080,\"advancedConfiguration\":{\"alternateTargetGroupArn\":\"$GREEN_TARGET_GROUP_ARN\",\"productionListenerRule\":\"$PRODUCTION_LISTENER_RULE_ARN\",\"roleArn\":\"$INFRASTRUCTURE_ROLE_ARN\"}}]")
fi
aws ecs update-service "${UPDATE_ARGS[@]}" --query 'service.serviceName' --output text >/dev/null

DEPLOYMENT_ARN=""
DEADLINE=$(( $(date +%s) + WAIT_TIMEOUT_SECONDS ))
while [ "$(date +%s)" -lt "$DEADLINE" ]; do
  if [ -z "$DEPLOYMENT_ARN" ]; then
    DEPLOYMENT_ARN="$(aws ecs list-service-deployments \
      --cluster "$CLUSTER" \
      --service "$SERVICE" \
      --created-at "after=$STARTED_AT" \
      --query 'serviceDeployments[0].serviceDeploymentArn' \
      --output text 2>/dev/null || true)"
    [ "$DEPLOYMENT_ARN" = "None" ] && DEPLOYMENT_ARN=""
  fi

  if [ -n "$DEPLOYMENT_ARN" ]; then
    read -r STATUS LIFECYCLE <<<"$(aws ecs describe-service-deployments \
      --service-deployment-arns "$DEPLOYMENT_ARN" \
      --query 'serviceDeployments[0].[status,lifecycleStage]' \
      --output text)"
    echo "$(date -u +%H:%M:%S) status=$STATUS stage=${LIFECYCLE:-none}"
    case "$STATUS" in
      SUCCESSFUL)
        echo "배포 성공: $TASK_DEFINITION_ARN"
        exit 0
        ;;
      ROLLBACK_SUCCESSFUL|ROLLBACK_FAILED|STOPPED)
        echo "배포 실패 ($STATUS). 이전 버전 상태를 확인하세요." >&2
        exit 1
        ;;
    esac
  else
    echo "$(date -u +%H:%M:%S) 배포 생성 대기 중"
  fi
  sleep "$POLL_INTERVAL_SECONDS"
done

echo "배포 대기 시간 초과 (${WAIT_TIMEOUT_SECONDS}초)" >&2
exit 1
