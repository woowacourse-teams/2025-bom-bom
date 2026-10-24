#!/bin/bash
# bombom prod ECS 호스트 부팅 (bombom-ecs-al2023-arm64 AMI: docker·ecs-init·journald·OTel agent 설치됨)
set -euxo pipefail

# 1) 호스트 스왑 1GiB: 시스템 프로세스용 안전망 (앱 task는 maxSwap 0)
if [ ! -f /swapfile ]; then
  fallocate -l 1G /swapfile || dd if=/dev/zero of=/swapfile bs=1M count=1024
  chmod 600 /swapfile
  mkswap /swapfile
fi
swapon /swapfile || true
grep -q '^/swapfile ' /etc/fstab || echo '/swapfile none swap sw 0 0' >> /etc/fstab

# 2) ECS 에이전트 설정
mkdir -p /etc/ecs
cat > /etc/ecs/ecs.config <<'CFG'
ECS_CLUSTER=bombom-prod
ECS_ENABLE_CONTAINER_METADATA=true
ECS_CONTAINER_STOP_TIMEOUT=45s
ECS_IMAGE_CLEANUP_INTERVAL=30m
ECS_IMAGE_MINIMUM_CLEANUP_AGE=1h
ECS_ENGINE_TASK_CLEANUP_WAIT_DURATION=1h
CFG

# 3) 모니터링 라벨: bombom-prod-ecs-<AZ>-<인스턴스 ID 뒤 6자리> (Alloy가 읽음)
set +x
IMDS_TOKEN=$(curl -fsS -X PUT http://169.254.169.254/latest/api/token -H 'X-aws-ec2-metadata-token-ttl-seconds: 300')
imds() { curl -fsS -H "X-aws-ec2-metadata-token: $IMDS_TOKEN" "http://169.254.169.254/latest/meta-data/$1"; }
INSTANCE_ID=$(imds instance-id)
AVAILABILITY_ZONE=$(imds placement/availability-zone)
set -x
MONITORING_NAME="bombom-prod-ecs-${AVAILABILITY_ZONE: -2}-${INSTANCE_ID: -6}"
mkdir -p /etc/bombom
printf 'APP_INSTANCE=%s\nNODE_INSTANCE=%s\nNODE_JOB=bombom-prod-ecs-node\n' "$MONITORING_NAME" "$MONITORING_NAME" > /etc/bombom/instance.env

# cloud-init이 끝나야 docker·ecs가 시작되므로 --no-block (문서의 deadlock 주의사항)
systemctl enable --now --no-block docker.service
systemctl enable --now --no-block ecs.service
