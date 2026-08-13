# Article 단일 테이블 검색 전환 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** `bom-bom-server`의 아티클 검색과 키워드 통계가 전체 기간의 `article`만 조회하고, 회원 탈퇴 시 해당 회원의 `article`을 삭제하도록 보장한다.

**Architecture:** Controller와 Service의 요청·응답 인터페이스는 유지하고 `ArticleRepositoryImpl` 내부의 최근 5일 분기와 `recent_article` UNION만 제거한다. 실제 `recent_article` 테이블과 `RecentArticle` 엔티티는 남기되, 운영 서버의 Repository·Service·Scheduler·테스트 의존성은 제거한다.

**Tech Stack:** Java 21, Spring Boot, Spring Data JPA, QueryDSL, MySQL 8.4 Testcontainers, JUnit 5, AssertJ

## Global Constraints

- API 경로, 요청 DTO, 응답 DTO와 `Page<ArticleResponse>` 구조를 변경하지 않는다.
- 검색 결과의 `articleId`는 원본 `article.id`를 반환한다.
- 제목과 `contents_text`에 기존 `LOWER(...) LIKE '%keyword%'` 부분 문자열 조건을 적용한다.
- 기존 정렬 화이트리스트, 페이징, 뉴스레터 필터, 북마크 여부를 유지한다.
- 실제 `recent_article` 테이블, 기존 Flyway 마이그레이션, `RecentArticle` 엔티티는 유지한다.
- `mail-server` 코드는 변경하지 않는다.
- 기존 한글 테스트 이름, Given-When-Then, `assertSoftly` 코드 스타일을 유지한다.

---

### Task 1: 검색 결과와 뉴스레터 통계를 `article` 단일 조회로 전환

**Files:**
- Modify: `src/test/java/me/bombom/api/v1/article/service/ArticleServiceTest.java`
- Modify: `src/main/java/me/bombom/api/v1/article/repository/ArticleRepositoryImpl.java`

**Interfaces:**
- Consumes: `ArticleService.getArticlesBySearch(Member, ArticleSearchOptionsRequest, Pageable)`와 `ArticleRepository.countPerNewsletter(Long, String)`
- Produces: 변경 없는 `Page<ArticleResponse>`와 `List<ArticleCountPerNewsletterResponse>`

- [ ] **Step 1: 전체 기간의 `article` 검색을 요구하는 실패 테스트 작성**

```java
@Test
void 검색은_도착_시각과_관계없이_article_테이블만_조회한다() {
    // given
    Newsletter targetNewsletter = newsletters.getFirst();
    List<Article> targetArticles = List.of(
            TestFixture.createArticle(
                    "단일 테이블 검색 과거",
                    member.getId(),
                    targetNewsletter.getId(),
                    OLD_ARTICLE_TIME
            ),
            TestFixture.createArticle(
                    "단일 테이블 검색 최근",
                    member.getId(),
                    targetNewsletter.getId(),
                    RECENT_ARTICLE_TIME
            )
    );
    articleRepository.saveAll(targetArticles);

    // when
    Page<ArticleResponse> result = articleService.getArticlesBySearch(
            member,
            ArticleSearchOptionsRequest.of(targetNewsletter.getId(), "단일 테이블 검색"),
            PageRequest.of(0, 1)
    );

    // then
    assertSoftly(softly -> {
        softly.assertThat(result.getTotalElements()).isEqualTo(2);
        softly.assertThat(result.getContent()).hasSize(1);
        softly.assertThat(result.getContent().getFirst().articleId())
                .isIn(targetArticles.get(0).getId(), targetArticles.get(1).getId());
    });
}
```

- [ ] **Step 2: 검색 테스트가 기존 5일 분리 때문에 실패하는지 확인**

Run:

```bash
./gradlew test --tests 'me.bombom.api.v1.article.service.ArticleServiceTest.검색은_도착_시각과_관계없이_article_테이블만_조회한다' -PreuseTestcontainers=true --console=plain
```

Expected: `totalElements`가 `2`가 아니어서 FAIL

- [ ] **Step 3: 뉴스레터 통계도 전체 기간의 `article`을 요구하는 실패 테스트 작성**

```java
@Test
void 키워드_통계는_도착_시각과_관계없이_article_테이블만_집계한다() {
    // given
    Newsletter targetNewsletter = newsletters.getFirst();
    articleRepository.saveAll(List.of(
            TestFixture.createArticle(
                    "단일 통계 과거",
                    member.getId(),
                    targetNewsletter.getId(),
                    OLD_ARTICLE_TIME
            ),
            TestFixture.createArticle(
                    "단일 통계 최근",
                    member.getId(),
                    targetNewsletter.getId(),
                    RECENT_ARTICLE_TIME
            )
    ));

    // when
    ArticleNewsletterStatisticsResponse result = articleService.getArticleNewsletterStatistics(
            member,
            "단일 통계"
    );

    // then
    assertSoftly(softly -> {
        softly.assertThat(result.totalCount()).isEqualTo(2);
        softly.assertThat(result.newsletters())
                .filteredOn(statistic -> statistic.id().equals(targetNewsletter.getId()))
                .extracting(ArticleCountPerNewsletterResponse::articleCount)
                .containsExactly(2);
    });
}
```

- [ ] **Step 4: 통계 테스트가 기존 최근 데이터 누락 때문에 실패하는지 확인**

Run:

```bash
./gradlew test --tests 'me.bombom.api.v1.article.service.ArticleServiceTest.키워드_통계는_도착_시각과_관계없이_article_테이블만_집계한다' -PreuseTestcontainers=true --console=plain
```

Expected: `totalCount`가 `2`가 아니어서 FAIL

- [ ] **Step 5: 검색 결과를 `article` 전용 쿼리로 단순화**

`findArticlesBySearch`는 다음 구조를 사용한다.

```java
@Override
public Page<ArticleResponse> findArticlesBySearch(
        Long memberId,
        ArticleSearchOptionsRequest options,
        Pageable pageable
) {
    JPAQuery<Long> totalQuery = getTotalQueryForSearch(memberId, options);
    List<ArticleResponse> content = findArticlesFromArticleOnly(memberId, options, pageable);
    return PageableExecutionUtils.getPage(content, pageable, totalQuery::fetchOne);
}
```

`getTotalQueryForSearch`에서 날짜 조건 없이 동일 검색 조건을 사용한다.

```java
private JPAQuery<Long> getTotalQueryForSearch(Long memberId, ArticleSearchOptionsRequest options) {
    return jpaQueryFactory.select(article.count())
            .from(article)
            .join(newsletter).on(article.newsletterId.eq(newsletter.id))
            .join(category).on(newsletter.categoryId.eq(category.id))
            .where(createMemberWhereClause(memberId))
            .where(createKeywordWhereClause(options.keyword()))
            .where(createNewsletterIdWhereClause(options.newsletterId()));
}
```

- [ ] **Step 6: 키워드 통계를 `article` 단일 SQL로 변경**

```sql
SELECT
    n.id AS newsletterId,
    n.name AS name,
    COALESCE(n.image_url, '') AS imageUrl,
    COUNT(a.id) AS articleCount
FROM article a
JOIN newsletter n ON n.id = a.newsletter_id
WHERE a.member_id = :memberId
  AND (
        LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
     OR LOWER(a.contents_text) LIKE LOWER(CONCAT('%', :keyword, '%'))
  )
GROUP BY n.id, n.name, n.image_url
```

`RECENT_DAYS`, 최근 개수 조회, `UNION ALL`, `MATCH ... AGAINST`, fallback과 날짜 인자를 제거한다.

- [ ] **Step 7: 검색과 통계 테스트 통과 확인**

Run:

```bash
./gradlew test --tests 'me.bombom.api.v1.article.service.ArticleServiceTest' --tests 'me.bombom.api.v1.article.controller.ArticleControllerTest' -PreuseTestcontainers=true --console=plain
```

Expected: PASS

- [ ] **Step 8: 검색 전환 커밋**

```bash
git add -- src/main/java/me/bombom/api/v1/article/repository/ArticleRepositoryImpl.java src/test/java/me/bombom/api/v1/article/service/ArticleServiceTest.java
git commit -m "refactor: 아티클 검색을 단일 테이블 조회로 변경"
```

---

### Task 2: 운영 서버의 `RecentArticle` 관리 로직 제거

**Files:**
- Delete: `src/main/java/me/bombom/api/v1/article/repository/RecentArticleRepository.java`
- Modify: `src/main/java/me/bombom/api/v1/article/service/ArticleService.java`
- Modify: `src/main/java/me/bombom/api/v1/article/scheduler/ArticleScheduler.java`
- Modify: `src/test/java/me/bombom/api/v1/article/service/ArticleServiceTest.java`
- Modify: `src/test/java/me/bombom/api/v1/TestFixture.java`
- Preserve: `src/main/java/me/bombom/api/v1/article/domain/RecentArticle.java`

**Interfaces:**
- Consumes: 기존 `ArticleService.deleteAllByMemberId(Long)`
- Produces: `article`과 Article 연관 데이터만 정리하는 동일 메서드; 삭제되는 `cleanupOldRecentArticles()`는 대체 인터페이스를 만들지 않는다.

- [ ] **Step 1: 테스트의 `RecentArticle` fixture 의존 제거**

`ArticleServiceTest`에서 `RecentArticle`, `RecentArticleRepository`, `recentArticleRepository`와 과거 dual-table 전용 테스트를 제거한다. `TestFixture`에서 다음 메서드와 `RecentArticle` import를 제거한다.

```java
public static RecentArticle createRecentArticle(...)
```

- [ ] **Step 2: `ArticleService`의 `RecentArticleRepository` 의존 제거**

`recentArticleRepository` 필드, 탈퇴 정리의 `bulkDeleteAllByMemberId(memberId)`, 다음 메서드를 제거한다.

```java
public int cleanupOldRecentArticles()
```

`articleRepository.bulkDeleteAllByMemberId(memberId)`와 나머지 Article 연관 데이터 삭제는 유지한다.

- [ ] **Step 3: `recent_article` 정리 스케줄 제거**

`ArticleScheduler`에서 `DAILY_2_20_AM_CRON`, `cleanup_old_recent_articles` ShedLock과 `cleanupOldRecentArticles()`를 제거한다.

- [ ] **Step 4: `RecentArticleRepository` 삭제**

`src/main/java/me/bombom/api/v1/article/repository/RecentArticleRepository.java` 파일을 삭제한다. `RecentArticle` 엔티티와 Flyway 마이그레이션은 수정하지 않는다.

- [ ] **Step 5: 컴파일과 Article 통합 테스트 확인**

Run:

```bash
./gradlew compileJava
./gradlew test --tests 'me.bombom.api.v1.article.service.ArticleServiceTest' --tests 'me.bombom.api.v1.article.controller.ArticleControllerTest' -PreuseTestcontainers=true --console=plain
```

Expected: PASS

- [ ] **Step 6: 남은 참조를 정적으로 확인**

Run:

```bash
rg -n "RecentArticle|recent_article|cleanup_old_recent_articles|cleanupOldRecentArticles" src/main/java src/test/java
```

Expected: `src/main/java`에서는 `RecentArticle` 엔티티 선언만, `recent_article`은 기존 Flyway 마이그레이션만 남고 `src/test/java`에서는 결과 없음

---

### Task 3: 회원 탈퇴의 `article` 삭제 회귀 방지

**Files:**
- Modify: `src/test/java/me/bombom/api/v1/withdraw/service/WithdrawDataCleanupServiceTest.java`

**Interfaces:**
- Consumes: `WithdrawDataCleanupService.cleanupByMemberId(Long)`
- Produces: 탈퇴 회원의 `article`만 삭제하고 다른 회원의 `article`을 보존하는 DB 상태

- [ ] **Step 1: 회원별 Article 삭제 상태를 검증하는 통합 테스트 보강**

`ArticleRepository`를 주입하고 기존 `탈퇴_회원의_모든_도메인_데이터가_삭제된다` 시나리오에 다음 데이터를 추가한다.

```java
Member otherMember = memberRepository.save(TestFixture.uniqueMemberFixture());
Article withdrawerArticle = articleRepository.save(TestFixture.createArticle(
        "탈퇴 회원 아티클",
        memberId,
        1L,
        LocalDateTime.now()
));
Article othersArticle = articleRepository.save(TestFixture.createArticle(
        "다른 회원 아티클",
        otherMember.getId(),
        1L,
        LocalDateTime.now()
));
```

최종 상태를 다음처럼 검증한다.

```java
softly.assertThat(articleRepository.findById(withdrawerArticle.getId())).isEmpty();
softly.assertThat(articleRepository.findById(othersArticle.getId())).isPresent();
```

- [ ] **Step 2: 탈퇴 통합 테스트 통과 확인**

Run:

```bash
./gradlew test --tests 'me.bombom.api.v1.withdraw.service.WithdrawDataCleanupServiceTest' -PreuseTestcontainers=true --console=plain
```

Expected: PASS

- [ ] **Step 3: 전체 관련 회귀 테스트 실행**

Run:

```bash
./gradlew test --tests 'me.bombom.api.v1.article.service.ArticleServiceTest' --tests 'me.bombom.api.v1.article.controller.ArticleControllerTest' --tests 'me.bombom.api.v1.withdraw.service.WithdrawDataCleanupServiceTest' -PreuseTestcontainers=true --console=plain
```

Expected: PASS

- [ ] **Step 4: 런타임 참조 제거와 탈퇴 검증 커밋**

```bash
git add -- src/main/java/me/bombom/api/v1/article/repository/RecentArticleRepository.java src/main/java/me/bombom/api/v1/article/service/ArticleService.java src/main/java/me/bombom/api/v1/article/scheduler/ArticleScheduler.java src/test/java/me/bombom/api/v1/TestFixture.java src/test/java/me/bombom/api/v1/article/service/ArticleServiceTest.java src/test/java/me/bombom/api/v1/withdraw/service/WithdrawDataCleanupServiceTest.java docs/superpowers/plans/2026-08-13-article-only-search.md
git commit -m "refactor: RecentArticle 관리 로직 제거"
```

---

### Task 4: 최종 계약과 변경 범위 검증

**Files:**
- Inspect: `src/main/java/me/bombom/api/v1/article/controller/ArticleController.java`
- Inspect: `src/main/java/me/bombom/api/v1/article/dto/response/ArticleResponse.java`
- Inspect: `src/main/java/me/bombom/api/v1/article/domain/RecentArticle.java`

**Interfaces:**
- Consumes: 완료된 Task 1~3의 코드와 테스트 결과
- Produces: API 계약 유지와 범위 준수를 보여주는 검증 기록

- [ ] **Step 1: API 계약 파일에 변경이 없는지 확인**

Run:

```bash
git diff server-dev...HEAD -- src/main/java/me/bombom/api/v1/article/controller/ArticleController.java src/main/java/me/bombom/api/v1/article/dto/request/ArticleSearchOptionsRequest.java src/main/java/me/bombom/api/v1/article/dto/response/ArticleResponse.java
```

Expected: 출력 없음

- [ ] **Step 2: 엔티티와 마이그레이션 보존 확인**

Run:

```bash
test -f src/main/java/me/bombom/api/v1/article/domain/RecentArticle.java
test -f src/main/resources/db/migration/V12.0.0__create_recent_article_table_with_ngram_index.sql
test -f src/main/resources/db/migration/V20.0.0__add_article_id_to_recent_article.sql
```

Expected: exit 0

- [ ] **Step 3: 변경 파일과 공백 오류 확인**

Run:

```bash
git diff --check
git status --short
git diff --stat server-dev...HEAD
```

Expected: `git diff --check` 출력 없음, 사용자 소유 untracked 파일은 변경되지 않음
