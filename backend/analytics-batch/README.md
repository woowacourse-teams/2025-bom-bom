# analytics-batch

운영 DB의 행동 데이터를 매일 한 번 BigQuery로 옮기는 Lambda입니다.

## 옮기는 테이블

| 테이블 | 방식 | 가져오는 열 |
|---|---|---|
| `article_read_history` | 증분 | 전체 |
| `article` | 증분 | id, member_id, newsletter_id, arrived_date_time, expected_read_time, created_at |
| `member` | 전체 교체 | id, provider, created_at |
| `subscribe` | 전체 교체 | id, member_id, newsletter_id, status, created_at, updated_at |
| `newsletter` | 전체 교체 | id, name, category_id, status, suspended_at, source, created_at |
| `category` | 전체 교체 | id, name |
| `withdrawn_member` | 전체 교체 | id, member_id, provider, 날짜 3개, 활동량 카운트 7개, created_at |
| `member_signup_source` | 전체 교체 | id, member_id, source, created_at (기타 직접 입력 `detail`은 제외) |

- **증분:** 추가만 되는 테이블입니다. 마지막 id 이후만 읽어 core에 합칩니다.
- **전체 교체:** 값이 바뀌거나 지워지는 작은 테이블입니다. 매번 전체를 읽어 raw에 덧붙이고, 행 수가 맞으면 core를 그 실행 분량으로 통째로 바꿉니다. raw에는 날짜별 전체본이 남아서 과거 상태를 볼 수 있습니다.
- **가져오지 않는 것:** 이메일, 닉네임, 생년월일, 성별, 프로필 이미지, 아티클 제목과 본문, 가입 경로 직접 입력. 읽기 전용 계정에도 위 열에만 조회 권한을 줍니다.
- `article.is_read`는 나중에 바뀌는 값이라 증분으로 맞출 수 없어 뺐습니다. 읽음 여부는 `article_read_history`로 봅니다.

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
- **중간에 멈췄을 때:** 행 상한이나 시간 상한에 걸리면 읽은 데까지만 적재하고 체크포인트를 옮깁니다. 다음 실행이 그 지점부터 이어갑니다. 전체 교체 테이블은 일부만으로 바꾸면 안 되므로 상한에 걸리면 실패로 처리하고 core를 그대로 둡니다.
- **테이블 하나가 실패했을 때:** 나머지 테이블은 계속 옮기고, 마지막에 Lambda를 오류로 끝내 알람이 울리게 합니다.
- **원본이 비어 있을 때:** 전체 교체 테이블이 0행이면 core를 바꾸지 않습니다.

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
| `TABLES` | `article_read_history` | 쉼표로 구분. 운영은 위 표의 여덟 테이블을 모두 적습니다 |
| `CHUNK_SIZE` | 2000 | 한 번의 SELECT로 읽는 행 수. `article`은 행이 커서 코드에서 500으로 고정 |
| `OVERLAP_ROWS` | 1000 | 다시 읽는 id 폭 |
| `SLEEP_SECONDS` | 0.2 | 묶음 사이에 쉬는 시간. `article`은 0.3으로 고정 |
| `MAX_ROWS_PER_RUN` | 200000 | 한 번 실행에서 읽는 행 상한. `article`은 코드에서 20000으로 낮춤 |
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

# 테이블을 골라서, 행 상한을 낮춰서 실행 (과거분을 조금씩 넣을 때)
aws lambda invoke --function-name bombom-analytics-batch-dev --cli-read-timeout 660 \
  --cli-binary-format raw-in-base64-out \
  --payload '{"tables":["article"],"max_rows_per_run":20000}' out.json
```

## 테이블을 추가할 때

1. `core.py`에 `TableSpec`을 추가하고 `TABLES`에 넣습니다. 수정·삭제가 있으면 `mode=SNAPSHOT`으로 둡니다.
2. 읽기 전용 계정에 그 열만 조회 권한을 줍니다. 예: `GRANT SELECT (id, name) ON bombom.category TO 'analytics_ro'@'10.0.%';`
3. Lambda의 `TABLES` 환경 변수에 이름을 추가합니다.

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

- `raw.<테이블>`: 받은 그대로에 `_run_id`, `_extracted_at`을 붙여 덧붙입니다. `_extracted_at` 날짜로 파티션합니다. load job이 처음 실행 때 만듭니다.
- `core.article_read_history`, `core.article`: id당 한 줄입니다. 각각 `DATE(read_at)`, `DATE(arrived_date_time)`으로 파티션하고 `member_id`로 클러스터링합니다. 병합 직전에 `CREATE TABLE IF NOT EXISTS`로 만듭니다.
- `core.member` 등 전체 교체 테이블: 매 실행마다 `CREATE OR REPLACE TABLE ... AS SELECT`로 바꿉니다.

## 아직 정하지 않았거나 확인하지 않은 것

- **시간대:** 시각 열은 변환 없이 BigQuery `DATETIME`으로 저장합니다. 운영 `article_read_history`는 `read_at`이 KST, `created_at`·`updated_at`이 UTC입니다(정확히 9시간 차이). 다른 테이블의 시각 열은 아직 확인하지 않았습니다.
- **삭제 반영:** 탈퇴로 지워진 읽음 이력과 아티클은 BigQuery에 그대로 남습니다(증분 테이블). 삭제 대조와 보존 기간은 정하지 않았습니다. 전체 교체 테이블은 다음 실행에서 사라집니다.
- **S3 날짜 경로:** `dt=`는 UTC 날짜입니다. 01:00 KST 실행분은 전날 날짜 폴더에 들어갑니다.
- **실행 시각:** 운영 RDS 자동 백업이 01:15~01:45 KST입니다. 01:00에 시작해 추출을 8분 안에 끝내도록 상한을 뒀습니다. 다만 이 상한은 테이블마다 따로 셉니다.
- **미실행 감지:** 실행 실패는 알람이 잡지만, 예약 자체가 돌지 않은 경우는 잡지 못합니다.
