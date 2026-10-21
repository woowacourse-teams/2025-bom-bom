# 배포 시크릿 안내

운영 DB 접속 설정은 GitHub Secret이나 컨테이너 환경변수로 전달하지 않습니다. 메일 서버와 공유하는 AWS Systems Manager Parameter Store의 `/bom-bom/prod/` 경로에서 애플리케이션이 실행 중 읽습니다.

필요한 파라미터, IAM 권한, 배포 절차는 [FCM 서버 README](README.md#데이터베이스-설정)를 참고하세요. 예전 문서에 적힌 `/bom-bom/fcm/prod/` 전용 파라미터는 현재 구성에서 사용하지 않습니다.
