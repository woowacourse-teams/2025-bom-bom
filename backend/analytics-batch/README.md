# analytics-batch

운영 DB의 행동 데이터를 매일 한 번 BigQuery로 옮기는 Lambda입니다. 지금은 읽음 이력(`article_read_history`) 한 테이블만 옮깁니다.

## 흐름

```
EventBridge Scheduler (매일 01:00 KST)
  → Lambda (VPC private 서브넷)
      1. S3의 체크포인트에서 지난번 마지막 id를 읽는다
      2. MySQL에서 id 순서로 조금씩 읽는다 (읽기 전용 계정, TLS)
      3. gzip NDJSON 파일로 S3에 둔다
      4. BigQuery raw에 적재 → core에 MERGE → 행 수 대조
      5. 4가 모두 성공하면 체크포인트를 앞으로 옮긴다
```

- **운영 DB를 아끼는 규칙:** PK 범위 조회만 씁니다(`WHERE id > ? ORDER BY id LIMIT ?`). 전체 스캔이나 정렬이 필요한 조회는 없습니다. 묶음마다 짧은 자동 커밋 조회 하나를 쓰고, 묶음 사이에 쉬며, 조회마다 `MAX_EXECUTION_TIME`을 겁니다.
- **늦게 커밋된 행:** id 순서와 커밋 순서가 다를 수 있어서, 매번 마지막 id에서 `OVERLAP_ROWS`만큼 뒤로 물러나 다시 읽습니다. raw에는 겹친 행이 중복으로 남고, core에서 id 기준으로 하나로 합칩니다.
- **실패했을 때:** 적재 전에 "무엇을 올릴지"를 `state/<테이블>.pending.json`에 적어 둡니다. BigQuery 단계에서 실패하면 다음 실행이 MySQL을 다시 읽지 않고 S3 파일로 이어서 적재합니다. load job id를 실행 번호로 고정해서 같은 파일이 두 번 적재되지 않습니다.
- **중간에 멈췄을 때:** 행 상한이나 시간 상한에 걸리면 읽은 데까지만 적재하고 체크포인트를 옮깁니다. 다음 실행이 그 지점부터 이어갑니다.

## 구성

| 파일 | 역할 |
|---|---|
| `src/analytics_batch/core.py` | 순서와 안전 규칙. 외부 라이브러리 없음 |
| `src/analytics_batch/adapters.py` | MySQL(pymysql), S3(boto3), BigQuery(REST) 구현 |
| `src/analytics_batch/handler.py` | Lambda 진입점, 환경 변수 읽기 |
| `tests/test_core.py` | 가짜 저장소로 돌리는 테스트 |
| `build.sh` | Lambda 배포용 zip 생성 |

## 환경 변수

| 이름 | 기본값 | 설명 |
|---|---|---|
| `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER` | 포트 3306 | 읽기 전용 계정으로 접속 |
| `DB_PASSWORD_PARAMETER` | 없음 | 비밀번호가 들어 있는 Parameter Store 이름 |
| `DB_SSL_CA` | 없음 | CA 파일 경로. 주면 서버 인증서까지 검증하고, 없으면 암호화만 함 |
| `S3_BUCKET` | 없음 | 추출 파일과 체크포인트를 두는 버킷 |
| `GCP_PROJECT` | `bombom-analytics` | |
| `BIGQUERY_LOCATION` | `asia-northeast3` | |
| `BIGQUERY_RAW_DATASET`, `BIGQUERY_CORE_DATASET` | `raw`, `core` | dev는 `dev_raw`, `dev_core` |
| `GOOGLE_APPLICATION_CREDENTIALS` | 없음 | `/var/task/gcp-credentials.json` |
| `TABLES` | `article_read_history` | 쉼표로 구분 |
| `CHUNK_SIZE` | 2000 | 한 번의 SELECT로 읽는 행 수 |
| `OVERLAP_ROWS` | 1000 | 다시 읽는 id 폭 |
| `SLEEP_SECONDS` | 0.2 | 묶음 사이에 쉬는 시간 |
| `MAX_ROWS_PER_RUN` | 200000 | 한 번 실행에서 읽는 행 상한 |
| `TIME_BUDGET_SECONDS` | 480 | 추출에 쓰는 시간 상한 |
| `PART_ROWS` | 50000 | S3 파일 하나에 담는 행 수 |

## Google 인증

서비스 계정 키 파일을 쓰지 않습니다. Lambda의 AWS 역할을 Workload Identity Federation으로 Google 서비스 계정으로 바꿉니다. zip에 들어가는 `gcp-credentials.json`은 "어떤 AWS 역할이 어떤 서비스 계정이 되는지"만 적힌 설정 파일이고 비밀값이 없습니다. `build.sh`가 `gcloud`로 만듭니다.

| 환경 | AWS 역할 | Google 서비스 계정 | 쓸 수 있는 데이터셋 |
|---|---|---|---|
| dev | `bombom-analytics-batch-dev` | `analytics-batch-dev@bombom-analytics` | `dev_raw`, `dev_core` |
| prod | `bombom-analytics-batch-prod` | `analytics-batch-prod@bombom-analytics` | `raw`, `core` |

## 테스트와 배포

```bash
# 테스트 (네트워크 불필요)
uvx --with pytest pytest -q

# 패키지 만들기와 반영 (uv, gcloud 로그인 필요)
./build.sh dev
aws lambda update-function-code --function-name bombom-analytics-batch-dev \
  --zip-file fileb://build/analytics-batch-dev.zip

# 수동 실행
aws lambda invoke --function-name bombom-analytics-batch-dev --cli-read-timeout 660 out.json
```

## AWS 리소스

| 종류 | 이름 |
|---|---|
| Lambda | `bombom-analytics-batch-dev`, `bombom-analytics-batch-prod` (Python 3.12, arm64, private 서브넷 2개) |
| S3 | `bombom-analytics-batch-dev`, `bombom-analytics-batch-prod` |
| 보안 그룹 | `bombom-analytics-batch` (운영 RDS와 dev MySQL의 3306 인바운드에 등록) |
| Parameter Store | `/bom-bom/analytics/{dev,prod}/DB_PASSWORD` |
| 예약 | EventBridge Scheduler `bombom-analytics-batch-prod`, `cron(0 1 * * ? *)` Asia/Seoul |
| 알림 | CloudWatch 알람 `bombom-analytics-batch-prod-errors` → SNS `bombom-db-alerts` |

네트워크 경로: S3는 gateway endpoint, Parameter Store와 Google은 NAT 서버를 거칩니다.

## BigQuery 테이블

- `raw.article_read_history`: 받은 그대로에 `_run_id`, `_extracted_at`을 붙여 덧붙입니다. `_extracted_at` 날짜로 파티션합니다. load job이 처음 실행 때 만듭니다.
- `core.article_read_history`: id당 한 줄입니다. `DATE(read_at)`으로 파티션하고 `member_id`로 클러스터링합니다. 병합 직전에 `CREATE TABLE IF NOT EXISTS`로 만듭니다.

## 아직 정하지 않았거나 확인하지 않은 것

- **시간대:** 시각 열은 변환 없이 BigQuery `DATETIME`으로 저장합니다. dev DB는 UTC로 동작하는데, 운영 DB에 저장된 값이 UTC인지 KST인지는 확인하지 않았습니다.
- **삭제 반영:** 탈퇴로 지워진 읽음 이력은 BigQuery에 그대로 남습니다. 삭제 대조와 보존 기간은 정하지 않았습니다.
- **S3 날짜 경로:** `dt=`는 UTC 날짜입니다. 01:00 KST 실행분은 전날 날짜 폴더에 들어갑니다.
- **실행 시각:** 운영 RDS 자동 백업이 01:15~01:45 KST입니다. 01:00에 시작해 추출을 8분 안에 끝내도록 상한을 뒀습니다.
- **미실행 감지:** 실행 실패는 알람이 잡지만, 예약 자체가 돌지 않은 경우는 잡지 못합니다.
- **다른 테이블:** `core.TABLES`에 `TableSpec`을 추가하면 같은 방식으로 옮길 수 있습니다. 다만 수정·삭제가 있는 테이블은 id 증분만으로 부족합니다.
