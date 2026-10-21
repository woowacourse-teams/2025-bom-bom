#!/usr/bin/env bash
set -euo pipefail

aws ssm get-parameters-by-path \
  --path /bom-bom/prod/ \
  --recursive \
  --with-decryption \
  --region ap-northeast-2 \
  --query 'Parameters[].Name' \
  --output json \
  | python3 -c '
import json
import sys

names = set(json.load(sys.stdin))
required = {"/bom-bom/prod/" + key for key in (
    "RDS_ENDPOINT", "DATABASE", "MYSQL_USER", "MYSQL_PASSWORD"
)}
missing = required - names
if missing:
    sys.exit("Required parameters missing: " + ", ".join(sorted(missing)))
print("Required database parameters are accessible.")
'
