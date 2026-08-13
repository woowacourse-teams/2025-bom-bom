# Article 단일 테이블 검색 전환 설계

## 배경

현재 아티클 검색은 최근 5일 데이터를 `recent_article`의 ngram FULLTEXT 검색으로 조회하고, 그 이전 데이터는 `article`의 부분 문자열 검색으로 조회한 뒤 합친다. 뉴스레터별 검색 통계도 같은 방식으로 두 테이블을 합산한다.

이번 BOM-1201의 목적은 `bom-bom-server`의 아티클 조회 기준을 `article` 하나로 통일하는 것이다. `mail-server`의 `recent_article` 저장 중단과 실제 테이블 삭제는 후속 작업에서 처리한다.

## 목표

- 검색 결과와 뉴스레터별 검색 통계가 전체 기간의 `article`만 조회한다.
- API 요청 및 응답 계약을 변경하지 않는다.
- 회원 탈퇴 시 해당 회원의 `article`과 기존 연관 데이터를 삭제한다.
- `bom-bom-server`에는 실제 `recent_article` 테이블을 매핑하는 `RecentArticle` 엔티티만 남긴다.

## 범위 밖

- `mail-server`의 `recent_article` 저장 로직 변경
- `recent_article` 테이블 삭제
- 기존 Flyway 마이그레이션 수정 또는 삭제
- `article` FULLTEXT 인덱스 추가
- API 경로, 요청 DTO, 응답 DTO 변경

## 현재 데이터 흐름

### 검색 결과

1. 최근 5일의 `recent_article` 검색 결과 수를 조회한다.
2. 5일 이전의 `article` 검색 결과 수를 조회한다.
3. `recent_article` 결과가 있으면 두 테이블을 `UNION ALL`로 합쳐 정렬하고 페이징한다.
4. `recent_article` 조회가 실패하면 `article` 전용 조회로 fallback한다.

### 뉴스레터별 검색 통계

1. 5일 이전 `article`을 제목 또는 본문 부분 문자열로 검색한다.
2. `recent_article`을 `MATCH ... AGAINST`로 검색한다.
3. 두 결과를 `UNION ALL`로 합친 뒤 뉴스레터별 개수를 계산한다.

## 목표 데이터 흐름

### 검색 결과

1. `memberId`, 검색어, 선택적 `newsletterId`로 전체 기간의 `article`을 조회한다.
2. 동일한 조건으로 전체 개수를 계산한다.
3. 기존 정렬 화이트리스트와 페이징을 적용한다.
4. `bookmark`, `newsletter`, `category` 정보를 기존 응답 형태로 조합한다.

제목과 본문 검색 조건은 다음과 같이 통일한다.

```sql
LOWER(article.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
OR LOWER(article.contents_text) LIKE LOWER(CONCAT('%', :keyword, '%'))
```

### 뉴스레터별 검색 통계

전체 기간의 `article`에 검색 결과와 동일한 검색어 조건을 적용하고 뉴스레터별로 집계한다. 검색 결과의 `totalElements`와 뉴스레터별 개수 합계가 같은 검색 의미를 사용해야 한다.

## 응답 계약

다음 항목은 변경하지 않는다.

- API 경로와 HTTP status
- 요청 파라미터와 검증 규칙
- `Page<ArticleResponse>` 구조
- `content`, `totalElements` 및 페이징 메타데이터
- `ArticleNewsletterStatisticsResponse` 구조
- 응답의 `articleId`: 계속 원본 `article.id`를 사용한다.
- 정렬, 뉴스레터 필터, 북마크 여부 표현

다만 최근 데이터의 검색 방식이 ngram FULLTEXT에서 부분 문자열 검색으로 바뀌므로 특정 검색어의 결과 집합과 개수는 달라질 수 있다. 이는 API 계약 변경이 아니라 검색 의미의 통일에 따른 동작 변경이다.

## 컴포넌트 변경

### `ArticleRepositoryImpl`

- 최근 5일 경계와 `RECENT_DAYS`를 제거한다.
- `recent_article` 개수 조회를 제거한다.
- 두 테이블을 합치는 `UNION ALL` 조회를 제거한다.
- 검색 결과와 전체 개수를 전체 기간의 `article`에서 계산한다.
- 뉴스레터별 키워드 통계를 `article` 단일 집계로 변경한다.
- dual-query 실패를 전제로 한 fallback과 관련 로그를 제거한다.

### `ArticleService`

- `RecentArticleRepository` 의존성을 제거한다.
- 회원 탈퇴 정리에서 `recent_article` 삭제 호출을 제거한다.
- 오래된 `recent_article` 삭제 메서드를 제거한다.
- 기존 `articleRepository.bulkDeleteAllByMemberId(memberId)` 및 연관 데이터 삭제 순서는 유지한다.

### `ArticleScheduler`

- 02:20 `recent_article` 정리 스케줄과 ShedLock 설정을 제거한다.
- 이전 아티클 이동, 초과 아티클 정리 등 `article` 및 `previous_article` 관련 스케줄은 유지한다.

### `RecentArticle`

- `RecentArticle` 엔티티는 남긴다.
- `RecentArticleRepository`는 제거한다.
- 테스트 전용 `RecentArticle` fixture와 검색 테스트 의존성은 제거한다.
- 기존 `recent_article` 생성·변경 Flyway 마이그레이션은 이력 보존을 위해 유지한다.

## 회원 탈퇴

`WithdrawDataCleanupService`가 `ArticleService.deleteAllByMemberId(memberId)`를 호출하는 기존 흐름을 유지한다. 통합 테스트에서 다음을 검증한다.

- 탈퇴 회원의 `article`이 모두 삭제된다.
- 다른 회원의 `article`은 유지된다.
- 기존 Article 연관 데이터 정리가 계속 성공한다.

이번 브랜치부터 탈퇴 시 `recent_article`은 삭제하지 않는다. `mail-server` 쓰기 중단과 테이블 삭제가 완료되기 전까지 탈퇴 회원의 `recent_article` 행이 남을 수 있으므로 후속 작업의 배포 지연을 운영 위험으로 관리한다.

## 오류 처리

`recent_article` 조회와 `UNION ALL`이 없어지므로 관련 fallback을 제거한다. 정렬 필드 검증과 DB 조회 예외 전파 방식은 기존 `article` 전용 조회 동작을 유지한다.

## 테스트 전략

### 검색 통합 테스트

- 최근 시각과 과거 시각의 `article`이 모두 검색된다.
- 제목과 `contents_text`의 부분 문자열이 검색된다.
- 검색어 앞뒤 공백 처리가 유지된다.
- `newsletterId` 필터가 유지된다.
- 정렬과 페이징이 유지된다.
- `totalElements`가 실제 검색 결과와 일치한다.
- 뉴스레터별 검색 통계 합계가 같은 조건의 검색 결과 개수와 일치한다.
- `recent_article` fixture 없이 모든 검색 테스트가 통과한다.

### 회원 탈퇴 통합 테스트

- 탈퇴 회원과 다른 회원의 `article`을 각각 저장한다.
- 탈퇴 정리를 실행한다.
- 탈퇴 회원의 `article`만 삭제됐는지 검증한다.
- 기존 연관 데이터 삭제 검증을 유지한다.

### 정적 확인

- `ArticleRepositoryImpl`에 `recent_article`, `UNION`, `MATCH ... AGAINST`, 5일 검색 경계가 남지 않는다.
- `RecentArticleRepository` 참조가 남지 않는다.
- `src/main/java`의 `RecentArticle` 참조는 엔티티 선언만 남는다.

## 성능 검증

`article`에는 FULLTEXT 인덱스가 없으므로 `%LIKE%` 검색은 회원별 후보 행을 스캔할 수 있다. 이번 작업에서는 새 인덱스를 추가하지 않는다.

배포 전 운영 유사 데이터로 다음을 확인한다.

- 검색 결과 쿼리와 전체 개수 쿼리의 `EXPLAIN ANALYZE`
- 뉴스레터 필터 유무에 따른 실행 계획
- 첫 페이지와 깊은 페이지의 응답 시간
- 제목 검색과 본문 검색의 결과 정확도
- DB CPU, rows examined, 쿼리 실행 시간 변화

고정 SLA 신설은 이번 범위에 포함하지 않는다. 측정한 실행 계획과 응답 시간을 현재 운영 검색 지표와 함께 PR에 기록하고, 회귀가 확인되면 이 티켓에 FULLTEXT 인덱스를 즉시 추가하지 않고 별도 검색 인덱스 설계로 분리한다.

## 배포와 후속 작업

이번 BOM-1201 배포 후 `bom-bom-server`는 `recent_article`을 조회하거나 관리하지 않는다. 그러나 `mail-server`는 계속 테이블에 데이터를 쓸 수 있으므로 테이블 행과 FULLTEXT 인덱스는 후속 배포까지 증가한다.

후속 작업은 다음 순서를 지킨다.

1. `mail-server`의 `recent_article` 저장 이벤트, Service, Repository 및 관련 테스트를 제거한다.
2. 모든 `mail-server` 인스턴스가 새 버전으로 교체되어 INSERT가 중단됐는지 확인한다.
3. 잔여 코드와 운영 쿼리에서 `recent_article` 참조가 없는지 확인한다.
4. `bom-bom-server`에서 `RecentArticle` 엔티티를 제거한 버전을 먼저 배포한다.
5. 모든 `bom-bom-server` 인스턴스에서 엔티티 제거가 확인된 뒤 별도 Flyway 마이그레이션으로 `recent_article` 테이블을 삭제한다.

## 완료 조건

- 검색 결과와 검색 통계가 `article`만 조회한다.
- API 요청 및 응답 구조가 유지된다.
- 회원 탈퇴 시 해당 회원의 `article`이 삭제되고 다른 회원 데이터는 유지된다.
- 실제 DB 테이블과 `RecentArticle` 엔티티를 제외한 `bom-bom-server`의 `RecentArticle` 로직이 제거된다.
- 관련 통합 테스트가 통과한다.
- 운영 유사 데이터의 검색 실행 계획과 응답 시간이 기록된다.
