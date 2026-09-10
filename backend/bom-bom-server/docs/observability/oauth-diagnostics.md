# OAuth 로그인 실패 진단 (BOM-1233)

## 기록 위치와 범위

- `/oauth2/authorization/{provider}`, `/login/oauth2/code/{provider}`에만 진단 필터를 적용한다. Apple의 `form_post`도 포함한다. 네이티브 ID token 로그인은 별도 경로다.
- `DiagnosticAuthorizationRequestRepository`: 인증 요청 저장/조회/소비. 인증 판단은 기존 Spring 저장소에 위임한다. 세션에 별도로 저장하는 값은 진단용 지문뿐이다.
- `DiagnosticOAuth2TokenResponseClient`: Google 토큰 교환의 HTTP 상태, 수신 시각, 응답 유효기간, scope와 토큰 지문.
- `DiagnosticOAuth2UserService`: 실제 HTTP 전송 직전 Bearer 헤더의 유무/형식, 발급 토큰과 일치 여부, 남은 유효기간, UserInfo 응답 상태.
- `OAuth2LoginFailureHandler`: 위 결과와 허용된 오류 코드, 플랫폼/OS/앱 버전, 배포/인스턴스, trace sampled 여부를 한 JSON 메시지로 기록한다. 예외 원문/스택은 공급자 응답이나 자격증명을 포함할 수 있어 자동 첨부하지 않는다.
- `oauth_login_succeeded`도 남겨 동일 시도의 성공 후 중복 콜백을 비교한다.

JSON을 로그 **message 본문**에 직렬화하므로 파일과 OTEL 로그 수집 모두에서 남는다. 새 MDC 키나 고카디널리티 Loki label을 추가할 필요가 없다. raw token, code, state, Cookie/Authorization 원문, 프로필, 전체 URL/query, 원본 User-Agent는 기록하지 않는다. User-Agent에서는 알려진 플랫폼과 숫자 버전만 추출하며 이 값은 클라이언트가 보내는 참고 정보다.

## 배포 설정

1. `${PARAMETER_PATH}/OAUTH_DIAGNOSTICS_HMAC_KEY`에 환경별로 별도의 무작위 32바이트 이상 키를 문자열(hex 권장)로 등록한다. 운영 기본 경로는 `/bom-bom/prod/OAUTH_DIAGNOSTICS_HMAC_KEY`이다. 기존 client secret을 재사용하지 않는다.
2. `scripts/deploy/start.sh`가 키를 읽고 권한 600인 배포 `.env` 파일에 전달한다. 다른 배포 경로와 dev에서는 같은 이름의 환경변수를 제공한다. 키를 콘솔, 티켓, PR에 기재하지 않는다.
3. 동일 환경의 모든 인스턴스에 같은 키를 적용한다. `fingerprint_scope=shared` 및 `fingerprint_key_id` 일치를 확인한다. 누락되면 인증은 계속 동작하지만 프로세스 임시 키를 사용하며, `fingerprint_scope=process`로 표시한다. 이 경우 인스턴스/재배포를 넘는 지문 비교는 불가능하다.
4. 키가 변경되면 이전 로그인과 새 로그인 지문은 연결할 수 없다. `state_matches_saved`를 해석할 때도 키 식별자를 확인한다.
5. 기본 인스턴스 식별자는 Docker가 제공하는 `HOSTNAME`(컨테이너 ID)이며 필요하면 호스트별 `APP_INSTANCE_ID`로 재정의한다. 두 운영 서버가 같은 Compose 파일을 쓰므로 고정 인스턴스 이름을 넣지 않는다. Compose는 `APP_RELEASE`를 전달한다. 새 release 값이 실제 이미지 태그와 일치하는지 확인한다. 이 변경에는 운영 키 등록이나 서비스 재배포를 포함하지 않는다.

## 진단 순서

Loki의 JSON 로그 envelope 안 message를 파싱하는 예:

```logql
{service_name="bombom-server-prod"} |= "oauth_login_failed"
| json
| line_format "{{.message}}"
| json
```

OTEL 수집 경로가 본문을 그대로 보관하면 마지막 `| json` 한 번만 사용한다. 로그 파이프라인에 맞춰 확인한다.

1. `event=oauth_login_failed`를 찾고 `attempt_id`와 `fingerprint_key_id`가 같은 저장/토큰/성공 로그를 조회한다. 시간 근접성만으로 같은 사용자라고 판단하지 않는다.
2. `stage=token_exchange`, `token_exchange_success=false`: 토큰 교환부터 실패했다. UserInfo 401과 구분한다.
3. `stage=userinfo`: `authorization_present`, `authorization_scheme_valid`, `token_matches_issued`, `token_expired`를 확인한다. 키가 없거나 기록하지 못한 필드는 false가 아니라 **미확인**이다.
4. `token_age_ms`는 **토큰 응답 수신 이후** 경과 시간이고, 유효기간은 공급자 응답을 기준으로 추정한 값이다. Google 내부에서 취소됐는지는 알 수 없다.
5. `authorization_request_not_found`: `session_present`, `session_cookie_present`, `saved_attempt_present`, `state_matches_saved`, `authorization_request_matched`를 확인한다. `saved_attempt_present`는 진단용 동반 기록의 존재이며 Spring 인증 요청 자체의 존재를 단정하지 않는다. 같은 시도의 이전 소비/성공 로그가 있으면 중복 콜백 여부를 좁힐 수 있다.
6. `diagnostic_error=true`이면 일부 진단이 누락됐으므로 정상이라고 추정하지 않는다. 원문 예외 대신 기록된 예외 종류/단계로 진단 수집 자체를 조사한다.

만료 전 같은 토큰이 올바른 형식으로 전달됐는데도 Google이 `Invalid Credentials`만 반환하면 내부 거부 사유는 미확정이다. 관측한 요청 구성, 정확한 시각/엔드포인트/응답 상태를 근거로 공급자 추가 조사를 진행한다. Google 브라우저 500 화면은 별도 요청이므로 발생 시각/URL 호스트·경로와 앱 시도 기록 없이 이 401과 연결하지 않는다.

## 실패 trace 보존: 운영 적용 전 별도 확인

현재 운영 Compose의 `traceidratio=0.2`는 유지한다. 이 저장소에는 실제 Collector/Tempo 설정이 없으며 아래는 배포되지 않은 **적용 예시**다.

1. Collector 유입량, 메모리, 큐/전송 실패, Tempo 저장량과 보존 기간을 측정한다. 앱에서 20% → 100% 전송 시 trace 유입량은 대략 5배가 될 수 있다.
2. 모든 span이 같은 trace 단위로 같은 tail sampler에 도착하도록 라우팅을 확인한다.
3. Collector traces pipeline에 아래 정책을 연결한 뒤 해당 앱의 head sampler를 `always_on`으로 전환한다. **기존 20% head sampling을 남겨 둔 채 Collector만 변경하면 버려진 trace를 복구할 수 없다.**

```yaml
processors:
  tail_sampling:
    decision_wait: 10s
    policies:
      - name: oauth-failures
        type: string_attribute
        string_attribute:
          key: auth.outcome
          values: [failure]
      - name: normal-sample
        type: probabilistic
        probabilistic:
          sampling_percentage: 20
```

실제 Collector 버전에 맞춰 `num_traces`, 큐/메모리 제한, `decision_wait`와 exporter를 정한다. 다른 기존 오류/지연 보존 정책은 함께 유지한다. 처리 실패나 버퍼 초과, 저장 기간 만료로 인한 누락도 감시한다.

OAuth 실패는 302 리다이렉트로 끝나므로 HTTP 5xx만 보존하는 정책은 충분하지 않다. 코드가 `auth.outcome=failure`와 span ERROR 상태를 남긴다. head sampling에서 이미 제외됐으면 이 표시로 복구되지 않는다.

## 검증 및 완료 기준

- 단위/HTTP 클라이언트 테스트: 정상 발급 후 UserInfo 401, 토큰 교환 거부, 정상 프로필 반환, 다른 토큰, 헤더 누락/잘못된 형식, 만료, 세션 없음, state 불일치, 중복 콜백, 요청 덮어쓰기, 요청 간 정보 누출, 키 변경을 검증한다.
- 실패 핸들러는 민감정보가 포함된 예외에서도 안전한 JSON을 남기고 기존 `/login?error` 리다이렉트를 유지해야 한다.
- dev에서 Google 테스트 계정으로 성공과 취소, 중복 콜백을 확인한다. 실제 자격증명을 복사/재전송하는 방식으로 재현하지 않는다.
- Collector 적용 후 가짜 공급자 401과 세션 실패를 각각 여러 번 재현해 **모든 실패 로그의 trace ID가 조회되는지** 확인한다. `trace_sampled=true`만으로 Tempo 저장 성공을 판정하지 않는다.
- 키/Collector 설정, 실제 배포 및 외부 공급자 재현은 로컬 자동 테스트의 통과와 구분하여 기록한다.
