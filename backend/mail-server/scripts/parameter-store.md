# 메일 서버 DB 설정

Docker 실행 시 `docker,parameter-store` 프로필로 `/bom-bom/prod/`의
`RDS_ENDPOINT`, `DATABASE`, `MYSQL_USER`, `MYSQL_PASSWORD`를 조회한다.
`RDS_ENDPOINT`는 포트 없는 호스트명이며 JDBC URL은 3306 포트로 구성한다.
리전은 `ap-northeast-2`다. 파라미터 변경 후에는 컨테이너를 재시작한다.

CI는 기존 GitHub Secrets에서 생성한 application.yml과 application-docker.yml의
DB URL·아이디·비밀번호를 패키징 전에 제거한다. GitHub Secrets의 원본에서도
`spring.datasource.url`, `username`, `password`를 삭제할 수 있다.
테스트의 H2 설정은 유지하며 로컬 실행에서는 AWS 조회를 활성화하지 않는다.

배포 전제:

- 메일 EC2의 IAM Role에 `/bom-bom/prod/*`의 `ssm:GetParametersByPath`를 허용한다.
- 고객 관리 KMS 키로 암호화했다면 해당 키의 `kms:Decrypt`도 허용한다.
- 배포 러너에 AWS CLI와 Python 3가 있어야 한다. 기존 서버 중지 전에 조회를 검사한다.
- EC2 Role 자격 증명을 Docker 컨테이너 안에서도 조회할 수 있어야 한다.
  IMDSv2를 사용하는 bridge 네트워크에서는 EC2 metadata hop limit 등 접근 설정을 확인한다.
- 컨테이너에서 SSM과 EC2 metadata endpoint에 접근할 수 있어야 한다.

앱 시작 시 파라미터 조회에 실패하면 기동을 중단한다. AWS 액세스 키를 이미지나 YML에 저장하지 않는다.
