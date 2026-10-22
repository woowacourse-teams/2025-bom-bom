#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

# 외부 AWS 인증 없이 기존 테스트 대역과 로컬 MySQL 컨테이너만 사용한다.
AWS_REGION=ap-northeast-2 \
AWS_ACCESS_KEY_ID=testing \
AWS_SECRET_ACCESS_KEY=testing \
AWS_EC2_METADATA_DISABLED=true \
SPRING_SESSION_JDBC_INITIALIZE_SCHEMA=never \
DISCORD_WEBHOOK_INQUIRY_URL=http://127.0.0.1:9/inquiry \
SPRING_CLOUD_AWS_S3_INQUIRY_BUCKET=test-inquiry \
INQUIRY_SCHEDULER_DAILY_STATUS_REPORT_CRON=- \
./gradlew unitTest test \
  --tests '*AppleAuthorizationRequestServiceTest' \
  --tests '*MemberControllerTest' \
  --tests '*CsrfControllerTest' \
  --tests '*CsrfDisabledControllerTest' \
  --tests '*OAuthLoginFlowTest' \
  --tests '*AppleOAuth2AuthorizationRequestRepositoryTest' \
  --tests '*SecurityCookieConfigTest' \
  --tests '*OAuth2DiagnosticsFilterTest' \
  --tests '*OAuth2LoginSuccessHandlerTest' \
  --tests '*BrowserSecurityTest' \
  --no-daemon --console=plain
