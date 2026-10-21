# Discord 웹훅 실행 시 주입

`APPLICATION_DEV_PROPERTIES`, `APPLICATION_TEST_PROPERTIES`, `APPLICATION_PROD_PROPERTIES` GitHub Secret의
설정은 URL 원문 대신 다음 환경변수 참조를 사용합니다. 빌드에서는 이 내용을
그대로 `application.yml`에 저장하고, Spring이 시작될 때 값을 읽습니다.

```yaml
discord:
  webhook:
    new_member:
      url: ${DISCORD_WEBHOOK_NEW_MEMBER}
    operationError:
      url: ${DISCORD_WEBHOOK_OPERATION_ERROR}
    inquiry:
      url: ${DISCORD_WEBHOOK_INQUIRY}
```

## 개발·수동 운영 배포

컨테이너 실행 단계에서 다음 GitHub Actions Secrets를 읽습니다.

| 컨테이너 환경변수 | 개발 배포 GitHub Secret | 수동 운영 배포 GitHub Secret |
| --- | --- | --- |
| `DISCORD_WEBHOOK_NEW_MEMBER` | `DISCORD_WEBHOOK_NEW_MEMBER_URL` | `DISCORD_WEBHOOK_NEW_MEMBER_URL` |
| `DISCORD_WEBHOOK_OPERATION_ERROR` | `DISCORD_WEBHOOK_OPERATION_ERROR_URL` | `DISCORD_WEBHOOK_OPERATION_ERROR_URL` |
| `DISCORD_WEBHOOK_INQUIRY` | `DISCORD_WEBHOOK_INQUIRY_DEV_URL` | `DISCORD_WEBHOOK_INQUIRY_URL` |

개발 서버에서 사용할 별도 문의 웹훅 URL을 `DISCORD_WEBHOOK_INQUIRY_DEV_URL`에
등록합니다. 개발·운영 YAML은 모두 `${DISCORD_WEBHOOK_INQUIRY}`를 참조하며,
배포 단계에서 환경에 맞는 값을 전달합니다. 개발 전용 Secret이 없으면 운영 URL로
대체하지 않고 기존 컨테이너를 중지하기 전에 배포를 실패시킵니다.

## 자동 테스트

개발 설정과 테스트 설정은 별도 파일입니다. `APPLICATION_TEST_PROPERTIES`는
`src/test/resources/application.yml`에 저장하고, 개발·운영 설정은 해당 빌드의
`src/main/resources/application.yml`에 저장합니다.

테스트 YAML의 `discord.webhook.inquiry.url`도 `${DISCORD_WEBHOOK_INQUIRY}`를
참조합니다. 모든 테스트 실행 단계는 이 환경변수에 개발 서버와 동일한
`DISCORD_WEBHOOK_INQUIRY_DEV_URL` Secret을 전달합니다. 테스트용 문의 URL을
더미 값으로 대체하지 않으며, Secret이 없으면 테스트 실행 전에 실패합니다.

테스트의 회원 가입·운영 오류 웹훅 환경변수는 `example.invalid` 더미 값을 유지합니다.

## Blue-Green 운영 배포

CodeDeploy의 `start.sh`가 Apple 키와 동일하게 AWS SSM Parameter Store에서
값을 읽어 Docker Compose에 전달합니다. 배포 전에 다음 SecureString 파라미터를
등록하고, EC2 인스턴스 역할에 해당 파라미터 읽기와 필요한 KMS 복호화 권한을 부여합니다.

- `/bom-bom/prod/DISCORD_WEBHOOK_NEW_MEMBER`
- `/bom-bom/prod/DISCORD_WEBHOOK_OPERATION_ERROR`
- `/bom-bom/prod/DISCORD_WEBHOOK_INQUIRY`

경로 접두사는 `PARAMETER_PATH`로 변경할 수 있습니다. GitHub Secret을 바꾸어도
SSM 값은 자동으로 갱신되지 않습니다. URL은 `.env`나 CodeDeploy 번들에 쓰지 않으며,
조회 실패 또는 빈 값이면 기존 컨테이너를 중지하기 전에 배포를 실패시킵니다.

기존 이미지에 포함된 URL은 이 변경으로 제거되지 않습니다. 노출된 웹훅을 교체하고,
환경변수 참조를 사용하는 설정으로 이미지를 다시 빌드해 배포해야 합니다.
