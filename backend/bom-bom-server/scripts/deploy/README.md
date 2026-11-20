# Discord 웹훅 배포 설정

개발·운영 서버는 Discord 웹훅 URL을 GitHub Secret이나 Docker 이미지에서 읽지 않습니다. 배포 시 AWS Systems Manager Parameter Store에서 `SecureString`으로 조회해 서버의 권한 제한된 `.env` 파일을 거쳐 컨테이너 환경변수로 전달합니다. 리전은 `ap-northeast-2`입니다.

## Parameter Store 항목

| 환경 | 파라미터 경로 | 환경변수 |
| --- | --- | --- |
| 운영 | `/bom-bom/prod/DISCORD_WEBHOOK_NEW_MEMBER` | `DISCORD_WEBHOOK_NEW_MEMBER` |
| 운영 | `/bom-bom/prod/DISCORD_WEBHOOK_OPERATION_ERROR` | `DISCORD_WEBHOOK_OPERATION_ERROR` |
| 운영 | `/bom-bom/prod/DISCORD_WEBHOOK_INQUIRY` | `DISCORD_WEBHOOK_INQUIRY` |
| 개발 | `/bom-bom/dev/DISCORD_WEBHOOK_NEW_MEMBER` | `DISCORD_WEBHOOK_NEW_MEMBER` |
| 개발 | `/bom-bom/dev/DISCORD_WEBHOOK_OPERATION_ERROR` | `DISCORD_WEBHOOK_OPERATION_ERROR` |
| 개발 | `/bom-bom/dev/DISCORD_WEBHOOK_INQUIRY` | `DISCORD_WEBHOOK_INQUIRY` |

각 파라미터에는 해당 환경의 전체 Discord 웹훅 URL을 저장합니다. 파라미터 이름에는 `_URL`을 붙이지 않습니다. 개발과 운영의 웹훅은 각각 해당 환경의 경로에 등록하며, 서로 대체하지 않습니다.

## 애플리케이션 설정

GitHub Environment Secret `APPLICATION_DEV_PROPERTIES` 및 `APPLICATION_PROD_PROPERTIES`의 웹훅 속성은 URL 원문이 아니라 다음 환경변수 참조를 사용해야 합니다.

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

빌드 워크플로우는 세 참조가 설정에 있는지 확인하고, Discord 웹훅 URL이 직접 포함되어 있으면 실패합니다. 자동 테스트에는 `example.invalid` 더미 주소를 사용하며 실제 알림을 보내지 않습니다.

## IAM 권한과 배포 흐름

- `dev-new-1` 개발 배포 러너에는 `/bom-bom/dev/DISCORD_WEBHOOK_*`를 읽을 `ssm:GetParameter` 권한이 필요합니다.
- Blue-Green 운영 배포의 EC2 인스턴스 역할에는 `/bom-bom/prod/DISCORD_WEBHOOK_*`를 읽을 권한이 필요합니다.
- 수동 운영 배포의 `prod-sub`, `prod-main` 러너도 `/bom-bom/prod/DISCORD_WEBHOOK_*`를 읽을 수 있어야 합니다.
- 고객 관리형 KMS 키로 암호화했다면 각 AWS 권한 주체에 해당 키의 `kms:Decrypt` 권한도 부여합니다.

운영 CodeDeploy는 `scripts/deploy/start.sh`에서 SSM을 조회하고, 개발 및 수동 운영 배포는 해당 배포 러너에서 조회합니다. 세 값을 모두 가져오지 못하면 Compose가 컨테이너를 중단하기 전에 배포가 실패합니다. 웹훅 값은 Docker 이미지나 CodeDeploy 번들에 저장하지 않습니다.

## Docker 이미지에 포함되는 다른 설정

이 변경은 Discord 웹훅 URL을 이미지에서 제외합니다. 하지만 `APPLICATION_DEV_PROPERTIES`와 `APPLICATION_PROD_PROPERTIES`의 나머지 설정은 현재 빌드에서 JAR에 포함됩니다. 여기에 OAuth 클라이언트 시크릿, 관리자 비밀번호 등 다른 민감값이 남아 있다면 공개 Docker Hub 이미지에서 추출할 수 있습니다. 공개 이미지에 시크릿이 없다고 판단하기 전에 나머지 설정도 감사하고, 민감값을 런타임 SSM 설정으로 옮겨야 합니다.
