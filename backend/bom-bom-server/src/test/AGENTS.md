# src/test 테스트 작성 규칙

이 파일은 `src/test` 아래 테스트 코드를 작성하거나 수정할 때 적용한다.
상세한 배경과 예시는 `docs/test-guideline-v1.md`를 참고한다.

## 0. 작업 원칙

- 테스트를 새로 작성하기 전에 같은 도메인의 기존 `*ControllerTest`, `*ServiceTest`, acceptance JSON dataset을 먼저 확인한다.
- 가장 가까운 기존 테스트의 구조, helper 스타일, JSON dataset 사용 방식을 따른다.
- 새 스타일을 만들지 말고 이 파일의 경계 선택 규칙에 맞춰 기존 스타일을 확장한다.
- 테스트 추가 요청을 받으면 먼저 HTTP API, Service 유스케이스, 도메인 정책 중 어디에 속하는지 판단한다.
- 코드 변경 후에는 가능하면 변경한 테스트 클래스만 먼저 실행한다.

## 1. 테스트 경계 선택

- HTTP API는 `@AcceptanceTest`로 작성한다.
  - 범위: `Controller ~ DB`
  - 검증: HTTP 상태, 응답 계약, 인증·인가, 요청값 검증, 최종 DB 상태
- 스케줄러, 이벤트 리스너, 배치처럼 HTTP 진입점이 없는 유스케이스는 `@IntegrationTest`로 작성한다.
  - 범위: `Service ~ DB`
  - 검증: 실행 결과, 트랜잭션 결과, 최종 DB 상태
- 도메인 정책은 순수 단위 테스트로 작성한다.
  - Spring Context를 띄우지 않는다.
  - 실제 도메인 객체와 경계값을 사용한다.
- Repository 테스트는 쿼리 자체가 복잡하거나 DB dialect 의존성이 큰 경우에만 작성한다.

## 2. HTTP API 인수 테스트 규칙

- 새 Controller 테스트는 `@AcceptanceTest("acceptance/{domain}/{use-case}.json")`를 사용한다.
- 요청은 실제 `RestAssured.given()`으로 보낸다.
- `MockMvc`, `RestAssuredMockMvc`를 새 Controller 테스트에 사용하지 않는다.
- 요청 body는 프로덕션 DTO가 아니라 `Map`으로 만든다.
- 응답은 `Map`, JSON path, HTTP 상태 코드로 검증한다.
- 내부 Service 메서드 호출 여부나 호출 횟수를 검증하지 않는다.
- API 하나마다 대표 성공 흐름, 인증·인가, 요청 검증, 실패·경계값을 우선 작성한다.
- 같은 정책을 여러 API가 공유하더라도 Controller 테스트는 API 계약을 기준으로 작성한다.
  - 공통 정책 자체는 HTTP 진입점이 없거나 API별 중복이 과도할 때만 Service 통합 테스트로 분리한다.

## 3. JSON dataset 규칙

- 인수 테스트 데이터는 `src/test/resources/acceptance/{domain}/{use-case}.json`에 둔다.
- Controller 인수 테스트는 `TestFixture`보다 JSON dataset을 우선 사용한다.
- JSON dataset은 API 시나리오의 사전 조건과 고정 ID를 문서처럼 보여주기 위한 것이다.
- `TestFixture`는 Service 통합 테스트나 도메인 단위 테스트에서 필요한 객체를 빠르게 만들 때 사용한다.
- 테스트에서 사용할 주요 ID는 JSON에 고정값으로 명시한다.
- 테스트 본문에서 데이터 생성 로직을 길게 만들지 않는다.
- 테스트가 기본 dataset을 변경하고 다음 테스트에서 원복이 필요하면 `@ResetsAcceptanceData`를 붙인다.
- 특정 테스트에만 필요한 데이터는 `@AdditionalAcceptanceDataSet`을 사용한다.
- `@AcceptanceTest`가 관리하는 데이터는 테스트에서 직접 전체 삭제하지 않는다.

## 4. 통합 테스트 데이터 규칙

- `@IntegrationTest`는 공통 정리 리스너를 사용한다.
- 테스트 격리를 위해 메서드 전체 `@Transactional` 롤백에 기대지 않는다.
- 테스트는 실제 커밋 결과와 최종 DB 상태를 검증한다.
- DB 초기화는 `TRUNCATE`가 아니라 공통 `CleanUp`/dataset loader의 `DELETE` 기반 정리를 따른다.
- 테스트 파일마다 `deleteAllInBatch()` 정리 체인을 만들지 않는다.
- 새 테스트에서 파일 전용 `@MockitoBean`을 추가하지 않는다.
- 외부 시스템은 공통 Fake/TestDouble을 우선 사용한다.
- 시간 제어는 공통 테스트 Clock 또는 테스트 대역을 사용하고 `LocalDate.now()`를 직접 쓰지 않는다.

## 5. 네이밍 규칙

- 테스트 메서드명은 한글로 작성한다.
- 변수명, 파라미터명, 헬퍼 메서드명은 영어로 작성한다.
- `@DisplayName`과 `displayName`은 사용하지 않는다.
- 헬퍼 메서드는 무엇을 하는지 바로 보이게 짧게 작성한다.
  - 예: `getArticles`, `createReview`, `authenticatedRequest`, `queryInt`

## 6. 금지 패턴

- 새 Controller 테스트에서 `MockMvc` 사용
- 새 Controller 테스트에서 `RestAssuredMockMvc` 사용
- Controller 테스트에서 프로덕션 DTO로 요청·응답 생성
- 내부 Service를 mock 처리하고 호출 횟수 검증
- 파일별 `@MockitoBean` 추가
- `@BeforeEach`에서 반복적인 전체 테이블 삭제
- 새 테스트에서 임의로 `TRUNCATE` 실행
- 테스트 격리 목적으로 클래스/메서드 전체에 `@Transactional` 추가
- 테스트 성공을 위해 프로덕션 코드를 우회하는 테스트 전용 분기 추가
- `Thread.sleep()`으로 비동기 대기
- 테스트 간 실행 순서 의존

## 7. 기본 예시

```java
@AcceptanceTest("acceptance/article/get-articles.json")
class ArticleControllerTest {

    private static final long MEMBER_ID = 1L;

    @Test
    void 아티클_목록_조회_성공() {
        Map<String, Object> response = getArticles(Map.of());

        assertThat(response.get("totalElements")).isEqualTo(11);
    }

    private static Map<String, Object> getArticles(Map<String, ?> query) {
        return RestAssured.given()
                .accept(ContentType.JSON)
                .header(AcceptanceTestHeaders.MEMBER_ID, MEMBER_ID)
                .queryParams(query)
                .when()
                .get("/api/v1/articles")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .jsonPath()
                .getMap("$");
    }
}
```

## 8. 작성 전 체크리스트

- [ ] 이 테스트가 HTTP API, Service 유스케이스, 도메인 정책 중 어디에 속하는지 정했다.
- [ ] 같은 도메인의 기존 테스트와 acceptance JSON dataset을 먼저 확인했다.
- [ ] HTTP API라면 `@AcceptanceTest`와 JSON dataset을 사용했다.
- [ ] Controller 테스트에서 DTO, MockMvc, 내부 Service mock을 사용하지 않았다.
- [ ] 실패 조건과 경계값을 최소 1개 이상 검토했다.
- [ ] 테스트명은 한글, 변수·파라미터·헬퍼명은 영어로 작성했다.
- [ ] 새 `@MockitoBean`을 추가하지 않았다.
- [ ] 테스트가 다른 테스트의 실행 순서나 남은 데이터에 의존하지 않는다.

## 9. 검증 명령

테스트 코드를 수정했다면 전체 테스트보다 변경한 테스트 클래스부터 실행한다.

```bash
./gradlew test --tests '패키지명.테스트클래스명' --no-daemon --console=plain
```

여러 Controller 테스트를 함께 바꿨다면 다음처럼 좁게 실행한다.

```bash
./gradlew test --tests '*ControllerTest' --no-daemon --console=plain
```
