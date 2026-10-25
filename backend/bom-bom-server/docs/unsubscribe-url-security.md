# 구독취소 URL 위협 검사

서버는 Lambda 호출 직전에 Google Web Risk Lookup API를 조회한다.
호스트 허용 목록(`UNSUBSCRIBE_ALLOWED_HOSTS`)은 더 이상 사용하지 않는다.

- 별도 URL 정책/호스트 허용 목록 검사는 수행하지 않는다.
- `SOCIAL_ENGINEERING`, `MALWARE`, `UNWANTED_SOFTWARE`를 한 요청으로 조회한다.
- 정상적인 `{}` 응답(위협 미탐지)만 Lambda에 전달한다. 원본 URL·토큰은 변경하지 않는다.
- 위협 탐지는 403 결과로 기존 영구 실패 처리에 연결한다.
- 키가 없거나 공백이면 외부 조회를 건너뛰고 기존처럼 Lambda로 전달한다. 이 환경에서는 피싱 검사가 적용되지 않는다.
- 키가 설정된 상태의 HTTP 오류, 시간 초과, 잘못된 응답은 503 결과로 기존 제한 횟수 재시도에 연결한다.
- 기본 연결 제한은 3초, 읽기 제한은 5초다. 대상 사이트를 검사용으로 직접 방문하지 않는다.
- API 키는 헤더로 전송하며, 검사 실패 예외에 요청 URL·응답 본문·원인 예외를 포함하지 않는다.

## Google Cloud 등록

1. [프로젝트 선택/생성](https://console.cloud.google.com/projectselector2/home/dashboard)에서 사용할 프로젝트를 선택한다. FCM 프로젝트와 같을 필요는 없다.
2. 해당 프로젝트에 결제 계정을 연결한다.
3. [Web Risk API 라이브러리](https://console.cloud.google.com/apis/library/webrisk.googleapis.com)에서 선택한 프로젝트를 확인하고 **사용**을 누른다.
4. [사용자 인증 정보](https://console.cloud.google.com/apis/credentials)에서 **사용자 인증 정보 만들기 → API 키**를 선택한다.
5. 키 편집 화면에서 **API 제한 → 키 제한 → Web Risk API**만 선택하고 저장한다. 고정된 서버 송신 IP를 사용하면 애플리케이션 제한을 IP 주소로 설정할 수 있다. 브라우저 referrer 제한은 서버 호출에 맞지 않는다.
6. 배포 환경에 `GOOGLE_WEB_RISK_API_KEY`를 추가하고 실제 서버 프로세스/컨테이너에 전달한 뒤 재시작한다. 호스트에만 변수를 설정하면 컨테이너에는 전달되지 않을 수 있다.

환경변수만으로 설정 가능하다. YAML로 관리할 때는 실제 키 대신 다음 참조를 사용한다.

```yaml
google:
  web-risk:
    api-key: ${GOOGLE_WEB_RISK_API_KEY:}
    connect-timeout: 3s
    read-timeout: 5s
```

FCM 서비스 계정 JSON이나 reCAPTCHA secret-key를 재사용하지 않는다. 키를 Git이나 채팅에 올리지 않는다.
키를 설정하지 않아도 서버와 자동 구독취소는 기존처럼 동작한다.
피싱 검사를 활성화하려면 키·API 활성화·결제 연결을 완료한 뒤 재시작한다.

Lookup API `uris.search`는 월 100,000회까지 무료이며, 초과 사용은 공식 요금표를 확인한다.
예산 알림은 지출을 자동 차단하지 않는다. 필요하면 콘솔에서 지원하는 요청 할당량도 제한한다.

## 보호 범위와 개인정보

미탐지는 안전 보장이 아니다. 신규 피싱 사이트와 허용된 최초 URL 이후의 리다이렉트·페이지 리소스 요청은
서버의 최초 조회만으로 완전히 차단할 수 없다. IP 주소·로컬 주소·사설 IP로 해석되는 도메인을 별도로 차단하는 정책도 없다.
완전한 접속 제한에는 Lambda/네트워크 계층의 검증이 추가로 필요하다.

Lookup API에는 개인별 토큰이나 이메일이 포함될 수 있는 전체 URL이 Google로 전달된다.
토큰을 제거하면 조회 대상이 바뀌므로 임의로 제거하지 않는다. 신규 공개 스캔 제출 API는 사용하지 않는다.
운영 요청 로깅/APM에서도 이 URL과 인증 헤더가 기록되지 않게 설정한다.

## 검증

실제 키 없이 HTTP 응답 대역으로 쿼리 인코딩, 세 가지 위협, 오류·비정상 응답과 키 누락을 검증한다.
Lambda 경계에서 정상 URL 전달, 위협/오류 시 호출 차단도 확인한다.
실제 Google Cloud 인증·할당량·네트워크 연결 검증은 운영 키 설정 후 별도로 필요하다.

- [Lookup API](https://docs.cloud.google.com/web-risk/docs/lookup-api)
- [공식 등록 예제](https://docs.cloud.google.com/web-risk/docs/using-sample-app)
- [API 키 관리](https://docs.cloud.google.com/docs/authentication/api-keys)
- [요금표](https://cloud.google.com/web-risk/pricing)

## 추가 검증 (2026-10-25)

- 관련 5개 테스트 클래스 총 48개 통과: 클라이언트 31개, Spring 설정 주입 3개,
  Web Risk → Playwright → UnsubscribeAgent 연결 8개, 기존 Playwright/Agent 6개.
- Google의 401/403/429/5xx 오류는 재시도 대상이며 Lambda 호출이 없는 것을 확인했다.
- dev 배포는 별도 GitHub Secret `GOOGLE_WEB_RISK_API_KEY`를 `.env`로 전달하고
  Compose가 컨테이너에 주입한다. 키 유무 두 경우 모두 Compose 설정 변환을 검증했다.
- 이는 실제 Google API 인증이나 dev 서버 배포 실행을 검증한 결과는 아니다.
