#!/bin/bash
# bombom ECS 호스트 AMI 굽기: 설치만 하고 docker·ecs는 켜지 않는다 (부팅 user data가 켬)
# 한 달에 한 번 최신 AL2023 arm64 AMI로 다시 굽는다.
#   1) launch template bombom-prod-ecs로 빌더 서버를 띄우되 이미지는 최신 AL2023
#      (/aws/service/ami-amazon-linux-latest/al2023-ami-kernel-6.1-arm64), user data는 이 파일, 루트 8GiB
#   2) /etc/bombom/ami-baked가 생기면 빌더를 중지하고 create-image (이름 bombom-ecs-al2023-arm64-<날짜>)
#   3) launch template에 새 버전(ImageId만 교체)을 만들고 기본 버전으로 지정한 뒤 빌더를 종료
#   4) 서버 교체는 블루그린 배포 또는 ASG 인스턴스 교체로 자연스럽게 반영된다
set -euxo pipefail
dnf install -y docker
mkdir -p /etc/docker
printf '{"log-driver":"json-file","log-opts":{"max-size":"10m","max-file":"3"}}\n' > /etc/docker/daemon.json
curl -fsSL -o /tmp/ecs-init.rpm https://s3.ap-northeast-2.amazonaws.com/amazon-ecs-agent-ap-northeast-2/amazon-ecs-init-latest.aarch64.rpm
dnf install -y /tmp/ecs-init.rpm
rm -f /tmp/ecs-init.rpm
mkdir -p /etc/systemd/journald.conf.d
printf '[Journal]\nSystemMaxUse=500M\n' > /etc/systemd/journald.conf.d/99-bombom.conf
mkdir -p /opt/bombom/otel /var/log/bombom-prod /var/lib/bombom-prod/dumps /var/lib/bombom-alloy /etc/bombom
aws s3 cp --region ap-northeast-2 s3://bom-bom-codedeploy-prod/ecs/otel/opentelemetry-javaagent-2.32.0.jar /opt/bombom/otel/opentelemetry-javaagent.jar
echo "f787eb6c7f3d18e69a431e108a15278d25ee37f83d68b678f621e063f3988f82  /opt/bombom/otel/opentelemetry-javaagent.jar" | sha256sum -c -
# ECS Exec 의존성: aws/amazon-ecs-ami scripts/install-exec-dependencies.sh와 같은 방식 (SSM 에이전트 정적 바이너리 + gpg 서명 검증)
EXEC_SSM_VERSION=3.3.5390.0
EXEC_DIR=/var/lib/ecs/deps/execute-command
mkdir -p /tmp/ssm-binaries
curl -fsSL -o /tmp/ssm-binaries/amazon-ssm-agent.gpg https://raw.githubusercontent.com/aws/amazon-ecs-ami/main/files/amazon-ssm-agent.gpg
curl -fsSL -o /tmp/ssm-binaries/agent.tar.gz "https://amazon-ssm-ap-northeast-2.s3.ap-northeast-2.amazonaws.com/${EXEC_SSM_VERSION}/linux_arm64/amazon-ssm-agent-binaries.tar.gz"
curl -fsSL -o /tmp/ssm-binaries/agent.tar.gz.sig "https://amazon-ssm-ap-northeast-2.s3.ap-northeast-2.amazonaws.com/${EXEC_SSM_VERSION}/linux_arm64/amazon-ssm-agent-binaries.tar.gz.sig"
export GNUPGHOME=/tmp/ssm-binaries/gnupg
mkdir -m 700 -p "$GNUPGHOME"
# 키 파일의 오래된 서브키 때문에 import가 경고와 함께 2를 반환할 수 있다. 서명 검증(--verify)으로 확인한다.
gpg --batch --import /tmp/ssm-binaries/amazon-ssm-agent.gpg || true
gpg --batch --verify /tmp/ssm-binaries/agent.tar.gz.sig /tmp/ssm-binaries/agent.tar.gz
unset GNUPGHOME
tar -xzf /tmp/ssm-binaries/agent.tar.gz -C /tmp/ssm-binaries
mkdir -p "${EXEC_DIR}/bin/${EXEC_SSM_VERSION}" "${EXEC_DIR}/certs"
cp /tmp/ssm-binaries/amazon-ssm-agent /tmp/ssm-binaries/ssm-agent-worker /tmp/ssm-binaries/ssm-session-worker "${EXEC_DIR}/bin/${EXEC_SSM_VERSION}/"
cp /etc/pki/ca-trust/extracted/pem/tls-ca-bundle.pem "${EXEC_DIR}/certs/tls-ca-bundle.pem"
chmod 400 "${EXEC_DIR}/certs/tls-ca-bundle.pem"
rm -rf /tmp/ssm-binaries

# 앱 이미지의 베이스(Dockerfile FROM)를 미리 받아 둔다. 새 서버에서 앱 이미지 pull이 짧아진다.
# docker.service는 cloud-init이 끝나길 기다리므로 user data 안에서는 dockerd를 직접 잠깐 띄운다.
dockerd > /tmp/dockerd-bake.log 2>&1 &
DOCKERD_PID=$!
for i in $(seq 1 30); do docker info > /dev/null 2>&1 && break; sleep 2; done
docker pull amazoncorretto:21
docker images --format '{{.Repository}}:{{.Tag}} {{.Size}}' >> /etc/bombom/ami-packages.txt
kill "$DOCKERD_PID"
wait "$DOCKERD_PID" || true
rm -f /tmp/dockerd-bake.log

systemctl disable docker.service ecs.service || true
rpm -qa | grep -E "^(docker|amazon-ecs-init)-" >> /etc/bombom/ami-packages.txt
dnf clean all
rm -rf /var/lib/ecs/data/* /var/log/ecs/*
touch /etc/bombom/ami-baked
