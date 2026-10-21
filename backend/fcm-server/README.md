# 봄봄 FCM 서버

봄봄 서비스의 Firebase Cloud Messaging(FCM) 알림을 처리하는 Spring Boot 애플리케이션입니다.

## 데이터베이스 설정

운영 환경은 AWS Systems Manager Parameter Store에서 DB 접속 정보를 읽습니다. 메일 서버와 동일한 파라미터를 사용하므로 FCM 전용 값을 추가하지 않습니다. 리전은 `ap-northeast-2`입니다.

| Parameter Store 경로 | 값 |
| --- | --- |
| `/bom-bom/prod/RDS_ENDPOINT` | 포트를 제외한 RDS 호스트명 |
| `/bom-bom/prod/DATABASE` | 데이터베이스 이름 |
| `/bom-bom/prod/MYSQL_USER` | 데이터베이스 사용자 이름 |
| `/bom-bom/prod/MYSQL_PASSWORD` | 현재 데이터베이스 비밀번호 |

네 항목은 `SecureString`으로 저장합니다. 애플리케이션이 포트 `3306`과 MySQL 연결 옵션을 붙여 JDBC URL을 구성합니다. DB 비밀번호를 바꾸면 기존 `/bom-bom/prod/MYSQL_PASSWORD` 값을 갱신합니다.

운영 컨테이너는 `parameter-store` 프로파일로 실행됩니다. 컨테이너가 시작될 때 EC2 인스턴스 역할의 AWS 자격 증명으로 Parameter Store를 읽으므로, 해당 역할에 `ssm:GetParametersByPath` 권한이 필요합니다. 고객 관리형 KMS 키를 사용한다면 해당 역할에 `kms:Decrypt` 권한도 부여합니다. 컨테이너가 SSM과 EC2 인스턴스 메타데이터 엔드포인트에 접근할 수 있어야 합니다. IMDSv2를 사용하는 경우 Docker 네트워크에서 메타데이터 응답 홉 제한이 컨테이너까지의 경로를 허용하는지도 확인합니다.

로컬 및 테스트 실행에서는 Parameter Store를 비활성화합니다. 각 환경에 필요한 설정은 해당 환경의 `application.yml`에서 관리합니다.

## 배포

GitHub Actions의 `fcm-deploy.yml` 워크플로우는 `fcm-server` 브랜치에 푸시될 때 FCM 이미지를 빌드하고 배포합니다.

빌드에 필요한 나머지 운영 설정은 GitHub Secret `FCM_APPLICATION_PROD_YML`에서 생성합니다. 워크플로우는 이미 설정된 `spring.datasource.url`, `spring.datasource.username`, `spring.datasource.password` 항목을 JAR/이미지에 포함하기 전에 제거합니다. 따라서 Secret에 과거 DB 접속 값이 남아 있어도 새 이미지에는 포함되지 않습니다. 해당 항목을 Secret에서 직접 삭제해도 됩니다.

배포 전 워크플로우는 자체 호스팅 `fcm` 러너에서 네 파라미터의 조회 권한과 존재 여부를 확인합니다. 이 러너의 AWS 권한과 애플리케이션이 실행되는 EC2 인스턴스 역할은 서로 다른 주체일 수 있으므로, 러너에는 사전 점검을 위한 SSM 읽기 권한을, EC2 인스턴스 역할에는 애플리케이션 시작 시 필요한 SSM 읽기 권한을 각각 설정합니다. 점검이 실패하면 기존 컨테이너를 중단하지 않습니다.

필수 파라미터와 권한에 대한 자세한 내용은 [`scripts/parameter-store.md`](scripts/parameter-store.md)를 참고하세요.
