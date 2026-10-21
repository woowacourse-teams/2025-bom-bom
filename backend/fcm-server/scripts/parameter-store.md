# FCM 운영 DB 설정

FCM은 메일 서버 및 메인 서비스와 동일한 Parameter Store 항목을 사용합니다. FCM 전용 파라미터를 별도로 만들지 않습니다. 리전은 `ap-northeast-2`이며, 애플리케이션 시작 시 아래 `SecureString` 값을 읽습니다.

| 기존 Parameter Store 경로 | 값 |
| --- | --- |
| `/bom-bom/prod/RDS_ENDPOINT` | 포트를 제외한 RDS 호스트명 |
| `/bom-bom/prod/DATABASE` | 데이터베이스 이름 |
| `/bom-bom/prod/MYSQL_USER` | 데이터베이스 사용자 이름 |
| `/bom-bom/prod/MYSQL_PASSWORD` | 현재 데이터베이스 비밀번호 |

JDBC URL은 RDS 호스트명 뒤에 포트 `3306`과 MySQL 연결 옵션을 붙여 구성합니다. 비밀번호가 변경되면 기존 `/bom-bom/prod/MYSQL_PASSWORD` 값을 갱신하면 됩니다. FCM용으로 복제된 값을 만들 필요는 없습니다.

## GitHub Actions와 YAML

나머지 운영 설정은 GitHub Secret `FCM_APPLICATION_PROD_YML`에서 생성합니다. 워크플로우는 패키징 전에 생성된 `application.yml`에서 `spring.datasource.url`, `spring.datasource.username`, `spring.datasource.password` 항목을 제거합니다. 따라서 이 Secret에 예전 DB 접속 정보가 남아 있어도 새 JAR나 이미지에는 포함되지 않습니다. Secret에서 해당 세 항목을 직접 삭제해도 됩니다.

FCM 이미지는 `parameter-store` Spring 프로파일로 실행됩니다. 로컬 및 테스트 실행에서는 Parameter Store 사용을 비활성화합니다.

## IAM 권한 및 재배포

배포 전 점검을 실행하는 자체 호스팅 `fcm` 러너에는 `/bom-bom/prod/` 경로에서 네 파라미터를 조회할 수 있는 AWS CLI와 `ssm:GetParametersByPath` 권한이 필요합니다. 애플리케이션 컨테이너도 시작할 때 Parameter Store에 접근해야 하므로, 컨테이너가 실행되는 EC2 인스턴스 역할에도 같은 경로를 읽을 권한을 부여해야 합니다. 러너의 AWS 자격 증명과 EC2 인스턴스 역할은 별개의 권한 주체일 수 있습니다.

파라미터가 고객 관리형 KMS 키로 암호화되어 있다면, 조회에 사용하는 각 권한 주체에 해당 키의 `kms:Decrypt` 권한도 필요합니다. 컨테이너가 SSM 서비스와 EC2 인스턴스 메타데이터 엔드포인트에 접근할 수 있어야 합니다. IMDSv2를 사용한다면 Docker 네트워크를 거쳐 컨테이너까지 자격 증명을 전달할 수 있도록 메타데이터 응답 홉 제한을 확인하세요. 컨테이너 네트워크 구성에 따라 홉 제한을 2 이상으로 설정해야 할 수 있습니다.

워크플로우는 기존 컨테이너를 중단하기 전에 필수 파라미터와 러너의 조회 권한을 확인합니다. 확인이 실패하면 배포를 진행하지 않습니다. 비밀번호 변경 후 `fcm-server` 브랜치에 변경 사항을 병합하면 새 이미지를 빌드하고 배포합니다. DB 접속 값은 이미지나 배포 파일에 포함되지 않습니다.
