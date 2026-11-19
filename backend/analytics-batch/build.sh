#!/usr/bin/env bash
# Lambda 배포용 zip을 만든다. 사용법: ./build.sh <dev|prod>
# 의존성은 모두 순수 파이썬이지만, Lambda(arm64, Python 3.12)에 맞는 wheel을 받도록 플랫폼을 지정한다.
set -euo pipefail

ENVIRONMENT="${1:?사용법: ./build.sh <dev|prod>}"
GCP_PROJECT_NUMBER="${GCP_PROJECT_NUMBER:-770798802570}"
GCP_PROJECT="${GCP_PROJECT:-bombom-analytics}"
HERE="$(cd "$(dirname "$0")" && pwd)"
BUILD="$HERE/build/$ENVIRONMENT"

rm -rf "$BUILD" "$HERE/build/analytics-batch-$ENVIRONMENT.zip"
mkdir -p "$BUILD"

uv pip install --quiet --target "$BUILD" \
  --python-platform aarch64-manylinux2014 --python-version 3.12 \
  -r "$HERE/requirements.txt"

cp -R "$HERE/src/analytics_batch" "$BUILD/"
find "$BUILD" -name "__pycache__" -type d -prune -exec rm -rf {} +

# 키가 아니라 "AWS 역할로 어떤 Google 서비스 계정이 될지" 적힌 설정 파일이다. 비밀값은 들어 있지 않다.
gcloud iam workload-identity-pools create-cred-config \
  "projects/$GCP_PROJECT_NUMBER/locations/global/workloadIdentityPools/aws-pool/providers/aws-bombom" \
  --service-account="analytics-batch-$ENVIRONMENT@$GCP_PROJECT.iam.gserviceaccount.com" \
  --aws --output-file="$BUILD/gcp-credentials.json" --quiet >/dev/null 2>&1

(cd "$BUILD" && zip -qr "../analytics-batch-$ENVIRONMENT.zip" .)
echo "$HERE/build/analytics-batch-$ENVIRONMENT.zip"
