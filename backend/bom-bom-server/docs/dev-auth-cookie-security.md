# 개발 환경 인증 쿠키 검증

## 실행

현재 dev 배포는 `APPLICATION_DEV_PROPERTIES` 시크릿을 `src/main/resources/application.yml`에 넣어 빌드한다.
`docker-compose-dev.yml`에서 `SPRING_PROFILES_ACTIVE=dev`를 지정하며, dev 전용 properties가 기본 YAML의 보안 값을 덮어쓴다. 비밀정보 시크릿 수정은 필수가 아니다.
비밀정보 없는 `application-dev.properties`에 보안 기본값을 통합했다. 별도 보안 프로필은 없다.
공유한 설정을 실제 `application-dev.yml`로 관리한다면 `spring.profiles.active: dev`를 제거한다. 현재 배포처럼 기본 `application.yml`에 넣는 경우에는 이 제한이 적용되지 않는다.
활성 프로필은 아래 환경변수에서 지정한다(Spring Boot의 프로필 전용 파일에는 active를 둘 수 없다).

```bash
SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun
```

배포할 때도 활성 프로필을 `dev`로 지정한다.
공유된 개발 설정의 Apple 콜백 URL(`/login/sso/verify/success/apple`)을 그대로 사용한다.
JPA 엔티티·Repository·Service로 임시 요청을 저장하고, Spring Security 연동은 별도 OAuth2 저장소에서 담당한다.
Flyway가 `V39.1.4`의 `apple_authorization_request` 테이블을 생성해야 한다.

- API 세션: `JSESSIONID_DEV`, `Secure; HttpOnly; SameSite=Lax`.
- Apple 요청 확인용: `__Host-BOMBOM_APPLE_AUTH_DEV`, `Secure; HttpOnly; SameSite=None`, 호스트 전용, `Path=/`, 5분.
- `__Host-` 접두사로 다른 서브도메인의 Domain 쿠키 주입을 브라우저에서 차단한다.
- 임시 쿠키는 API 인증 권한을 갖지 않는다. 쿠키에는 무작위 식별자만 담고 서버에는 그 해시로 요청을 저장한다.
- Apple 콜백의 state와 쿠키를 확인한 뒤 DB DELETE 성공 여부로 한 번만 소비한다.
- 로그인 리다이렉트는 문자열 접두사 대신 scheme/host/port와 경로 경계를 검사한다.
- 탈퇴 재인증 시 인증된 회원 ID와 원래 탈퇴 대상 ID가 일치해야 한다.
- 네이티브 로그인과 가입 완료도 세션 ID를 교체하고 이전 CSRF 토큰을 폐기한다. 웹 OAuth에만 의존하지 않는다.
- 인증 요청, nonce 등은 서버에 보관하며 실제 토큰과 nonce 검증은 기존 Spring OAuth2/OIDC 처리를 사용한다.
- 기존 AppleCallbackController는 제거했고 Spring Security가 외부 Apple POST 콜백을 직접 처리하며, 검증한 요청의 리다이렉트·탈퇴 재인증 문맥만 새 세션에 복원한다.
- 로그인 취소를 포함해 처리 가능한 콜백에서 임시 쿠키를 삭제한다. 중단된 로그인 데이터는 만료 후 다음 로그인 시작 시 정리한다.
- 같은 브라우저에서 Apple 로그인을 여러 탭으로 동시에 시작하면 가장 나중에 발급한 임시 쿠키에 해당하는 요청만 완료할 수 있다.
- 이전 탭·변조된 콜백을 거부할 때는 최신 로그인 쿠키를 지우지 않는다. 검증한 요청을 소비했을 때만 삭제한다.
- MySQL의 뒤 공백 무시 규칙을 피하도록 state를 바이트 단위로 비교한다.

CORS 허용 목록은 `app.cors.allowed-origins`의 쉼표로 구분한 정확한 Origin이다.
기본값은 `frontend.base-url`이며, redirect URI 허용 목록은 CORS 허용 목록과 별개다.
dev 프로필은 dev/maeilmail/localhost:3000을 허용한다.
Apple Origin은 POST 콜백 두 경로에서만 허용하고 회원 API에서는 허용하지 않는다.

## CSRF와 클라이언트 배포

dev도 `app.security.csrf-enabled=false`를 유지한다. 프론트 수정 없이 기존 로그인·가입·회원 수정·로그아웃을 사용할 수 있다.
이번 서버 PR에는 프론트엔드 애플리케이션 변경을 포함하지 않는다.
이전 프론트 변경은 기존 `fix/dev-auth-cookie-security` 브랜치의 `808e1257` 커밋에 보존한다.

CSRF 토큰 검사는 아직 활성화하지 않았다. CORS 제한과 Lax 쿠키는 방어를 강화하지만 완전한 CSRF 방어를 대체하지 않는다.
특히 같은 사이트의 서브도메인과 상태를 변경하는 GET 요청은 별도 검토가 필요하다.
HTTPS dev 프론트엔드와 API는 같은 Site이므로 기존 쿠키 인증을 유지한다.
`http://localhost:3000`에서 HTTPS dev API로 직접 요청하는 경우는 다른 Site이므로 아래 로컬 개발 제약을 확인한다.

향후 CSRF 보호를 활성화하려면 쿠키 인증을 사용하는 웹·네이티브 클라이언트가
`GET /api/v1/auth/csrf`에서 받은 토큰과 헤더 이름을 변경 요청에 포함하도록 먼저 수정·배포해야 한다.
그 이후 `app.security.csrf-enabled=true`를 적용한다. 이 작업은 이번 서버 변경의 배포 전제 조건이 아니다.
토큰 응답은 `Cache-Control: no-store`이며 비활성 상태에서는 `enabled=false`, 토큰·헤더 이름은 null이다.
활성 상태에서는 Apple의 두 POST 콜백만 토큰 검사에서 제외하고 state/nonce/브라우저 연결 검증을 유지한다.

## 자동 검증

평소에는 HTTP/DB·단위 검사를 실행한다. 브라우저 테스트 코드는 보존하고 클래스의 `@Disabled`로 비활성화했다.

```bash
./scripts/check-auth-security.sh
```

브라우저를 실행하려면 `BrowserSecurityTest`의 `@Disabled`를 제거하고 테스트 설정 환경변수를 주입한 뒤 실행한다.
별도 태그나 Gradle 태스크는 사용하지 않는다.
보존된 브라우저 검사는 CSRF 연동 클라이언트용이며, 해당 소스와 Node 의존성이 설치된 프론트 체크아웃을 `BOMBOM_FRONTEND_ROOT`로 지정해야 한다.
도우미 스크립트는 백엔드 `src/test/resources/security/`에 보존한다.

```bash
BOMBOM_SECURITY_BROWSERS=chromium,webkit ./gradlew test --tests '*BrowserSecurityTest' --rerun-tasks
```

`@Disabled`가 남아 있으면 일반 테스트에 포함되어도 건너뛴다.
단위 테스트의 CORS·JWT 중복 검사는 실제 HTTP 검사로 통합했다.
직렬화 왕복은 실제 JPA 저장·조회로, 동시 단일 소비는 MySQL 동시 8개 요청으로 검증한다.

기존 비밀정보 주입 경로의 테스트 설정 파일과 초기화한 OpenAPI 하위 저장소가 필요하다.
일반 백엔드 테스트에는 Docker가 필요하다. 비활성 브라우저 검사에는 Node.js와 별도 프론트엔드 체크아웃의 의존성이 필요하다.
브라우저 검사에는 추가로 Playwright Chromium(또는 macOS Chrome)과 openssl이 필요하다.
Firefox·WebKit도 검증하려면 프론트엔드에서 `npx playwright install firefox webkit`을 실행한다.
로컬 macOS에서는 Firefox가 OS sandbox/렌더링 오류로 시작하지 못했으므로 Firefox는 미검증이다.
Chrome과 WebKit은 같은 HTTPS·쿠키·CSRF·POST 콜백·서브도메인 쿠키 주입 검사를 실행했다.
스크립트는 로컬 테스트에 누락된 AWS 리전·문의 설정에 더미 값을 지정하고 문의 스케줄을 비활성화한다.

- 백엔드 전체 단위 테스트와 관련 HTTP/DB 통합 테스트.
- 로컬 JWKS의 실제 RSA 서명, 잘못된 서명/issuer/audience/만료 토큰 거부.
- 커스텀 디코더에 Spring 기본 OIDC 검증을 적용해 exp/aud/sub/iat/nonce 누락, 미래 iat, 다중 audience의 azp 누락·불일치도 거부.
- 테스트 IdP가 500 오류로 잘못된 토큰 거부 테스트를 통과시키지 않도록 응답 생성 실패를 별도로 검사.
- 로컬 IdP의 서명된 ID 토큰으로 Apple OAuth 성공 → 새 인증 세션 → 회원 API → CSRF 갱신 흐름.
  기본 오류 검사는 공통 테스트 대역을 사용한다. 최초 동의 정보·실제 가입·재로그인 검사는 실제 AppleOAuth2Service와 성공 핸들러, DB를 사용한다.
  UTF-8 한글 이름·이메일이 가입 화면에 전달되고, user 없는 재로그인에서도 같은 회원 정보가 유지되는지 검증한다.
  user가 없거나 깨진 신규 요청은 ID 토큰 이메일과 대체 닉네임으로 가입 화면을 구성하는지 검증한다. 외부 Apple 계정 자체는 사용하지 않는다.
- Google은 로컬 토큰·사용자 정보 서버와 실제 Google 로그인 서비스/회원 저장소로 기존 GET 콜백·세션·CSRF 회전을 검증한다.
- 올바른 서명이어도 잘못된 nonce는 로그인 거부. state/쿠키가 틀리면 토큰 교환 전에 거부.
- 토큰 교환 오류·깨진 응답·ID 토큰 누락·잘못된 서명/발급자/대상/만료는 인증 실패하고 콜백을 재사용할 수 없다.
- 두 Apple POST 콜백 경로, 공급자 혼동 공격, 콜백 재사용 거부를 검증한다.
- 오래된 탭·공백/탭/유니코드 공백으로 변조된 state를 거부한 후 최신/원래 요청으로 로그인할 수 있는지 검증한다.
- Apple 콜백과 유사한 하위 경로와 POST 이외 변경 메서드는 CSRF 예외가 아님을 검증한다.
- DB 마이그레이션, state 대소문자 구분, 만료와 동시 8개 요청의 일회성 소비.
- 실제 HTTP CORS 및 CSRF 누락/변조/다른 세션/로그아웃 후 토큰 거부.
- 네이티브 로그인·실제 신규 가입 후 이전 세션/CSRF 거부, 새 세션의 인증과 변경·로그아웃을 검증한다. 외부 네이티브 토큰 검증은 공통 Apple 대역을 사용한다.
- 보존된 Disabled 브라우저 검사: 실제 백엔드와 MySQL 앞의 로컬 HTTPS 프록시 + Chromium 테스트.
  같은 Site의 프론트엔드, 다른 Site의 악성 페이지, 로컬 IdP를 분리한다.
  SameSite=None 대조 쿠키가 전송될 때 Lax 세션 쿠키는 빠지는지 확인한다.
  기존 None 로그인 쿠키와 같은 Site 서브도메인의 Lax 쿠키가 전송되는 경우에도 CORS가 응답 읽기를 차단하는지 확인한다.
  최신 프론트엔드 fetcher와 CSRF 모듈을 실제로 빌드해 브라우저에서 실행한다(전체 React UI 검증은 아님).
  Apple 형태의 cross-site POST 취소 응답과 임시 쿠키/서버 상태 정리까지 확인한다.
  같은 브라우저에서 서명된 토큰의 POST 성공 → 새 세션 → 실제 회원 API → 프론트 CSRF 재조회도 연결해 검증한다.

브라우저 테스트의 초기 인증은 테스트 코드에만 존재하는 로그인 fixture를 사용한다.
Apple 성공 흐름은 로컬 JWKS/토큰 서버와 공통 Apple 회원 조회·성공 핸들러 대역을 사용한다.
실제 Apple/Google 계정 로그인이나 Apple 토큰 발급을 재현하는 테스트는 아니다.
추가 브라우저 엔진은 `BOMBOM_SECURITY_BROWSERS=chromium,firefox,webkit`로 지정한다.
해당 Playwright 브라우저 설치가 필요하며, Firefox/WebKit의 OS 실행 실패를 보안 검증 통과로 취급하지 않는다.
로컬 IdP Origin은 브라우저 테스트의 CORS 설정에만 추가하며 운영 설정에는 추가하지 않는다.

## 검증 범위

- CSRF 비활성 상태: 실제 세션으로 토큰 없는 네이티브 로그인·가입·회원 수정·로그아웃과 외부 Origin 차단을 검증한다.
- CSRF 활성 상태도 별도 테스트 설정으로 유지하며 토큰 누락·변조·회전과 Apple 콜백 예외를 검증한다.
- 최종 전체 테스트: 882개 중 881개 통과, 브라우저 1개는 `@Disabled`로 건너뜀. 실패·오류 0.
- 단위 테스트 전용 실행: 222개 통과(전체 테스트와 중복 포함). 브라우저 도우미 Node 구문 검사와 셸 스크립트 구문 검사도 통과했다.
- 실제 Apple/Google 계정, 개발 서버 배포와 전체 React 화면 수동 검증은 별도 확인이 필요하다.
- 마이그레이션은 확인 시점의 `origin/main`, `origin/server-dev` 모두 `V39.1.3`까지여서 `V39.1.4`를 사용한다.

## 개발 서버 브라우저 검증

배포 후 기존 쿠키를 삭제하고 다시 로그인한다. 기존 None 쿠키는 설정 변경만으로 즉시 갱신되지 않을 수 있다.
네트워크 기록에서 로그인 전 과정의 로그를 보존한다. 코드·토큰·쿠키 값이 포함된 기록은 공개하지 않는다.

| 상황 | 기대 결과 |
| --- | --- |
| dev.bombom.news에서 회원 조회/수정 | 일반 세션 쿠키 포함, 정상 응답 |
| 외부 사이트에서 credentials: include로 회원 조회 | 일반 세션 쿠키 제외, 개인정보 읽기 실패 |
| 정상 API 사전 요청 | 허용된 Origin만 CORS 허용 |
| Google 로그인 | 신규·기존 사용자 및 새로고침 후 세션 유지 |
| Apple 로그인 시작 | 5분짜리 임시 쿠키 생성, None/Secure/HttpOnly 확인 |
| Apple 최초 동의 및 기존 사용자 로그인 | 외부 POST에 일반 Lax 쿠키가 없어도 임시 쿠키로 처리, 최종 로그인 성공 |
| Apple 로그인 취소 | 실패 리다이렉트, 인증된 세션 생성 안 됨 |
| 임시 쿠키 삭제·state 변조·5분 만료·콜백 재전송 | 로그인 실패 |
| 신규 Apple 회원 가입 | 이름·이메일 및 가입 대기 상태 유지 |
| Apple 재인증 후 탈퇴 | 기존 재인증 문맥 유지 및 정확한 대상 처리 |
| 로그아웃 후 회원 조회 | 인증 거부 |

503이나 단순 CORS 오류만으로 SameSite가 검증됐다고 판단하지 않는다.
외부 요청의 Cookie 헤더에서 일반 로그인 쿠키가 제외됐는지 따로 확인한다.

`http://localhost:3000`에서 `https://api-dev.bombom.news`로 직접 호출하면 cross-site이므로
Lax 세션 인증은 작동하지 않는다. CORS에 localhost를 허용해도 달라지지 않는다.
전체 로그인 검증은 HTTPS 개발 프론트엔드에서 진행하고, 로컬 개발은 동일 사이트 프록시 등 별도 연결 방식을 사용한다.

CORS와 SameSite는 서버 인증과 상태 변경 요청의 CSRF 검증을 대체하지 않는다.
이번 dev 프로필은 CORS 제한과 SameSite 강화를 적용하며 CSRF 토큰 검사는 비활성 상태다.
