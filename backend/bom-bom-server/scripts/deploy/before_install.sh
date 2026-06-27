#!/usr/bin/env bash
set -euo pipefail

APP_DIR="/opt/bom-bom/app"
DEPLOY_DIR="/opt/bom-bom/deploy"
RELEASE_DIR="/opt/bom-bom/releases"

mkdir -p "$APP_DIR" "$DEPLOY_DIR" "$RELEASE_DIR"
chmod 755 /opt/bom-bom "$APP_DIR" "$DEPLOY_DIR" "$RELEASE_DIR"
