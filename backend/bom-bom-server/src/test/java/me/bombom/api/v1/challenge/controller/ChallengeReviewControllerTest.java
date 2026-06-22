package me.bombom.api.v1.challenge.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import io.restassured.http.ContentType;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import me.bombom.support.time.MutableClock;
import me.bombom.support.acceptance.AcceptanceTest;
import me.bombom.support.acceptance.AcceptanceTestHeaders;
import me.bombom.support.acceptance.AdditionalAcceptanceDataSet;
import me.bombom.support.acceptance.ResetsAcceptanceData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

@AcceptanceTest("acceptance/challenge/review.json")
class ChallengeReviewControllerTest {

    private static final long MEMBER_ID = 1L;
    private static final long OTHER_MEMBER_ID = 2L;
    private static final long CHALLENGE_ID = 1L;
    private static final long PARTICIPANT_ID = 1L;
    private static final LocalDate TODAY = LocalDate.of(2026, 1, 26);

    @Autowired
    private MutableClock clock;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void 시간을_고정한다() {
        clock.setDate(TODAY);
    }

    @Test
    @AdditionalAcceptanceDataSet("acceptance/challenge/review-visible.json")
    void 로그인한_사용자는_가시성_정책에_맞는_리뷰_목록을_조회한다() {
        Map<String, Object> result = getReviews(CHALLENGE_ID, MEMBER_ID, null, null);
        List<Map<String, Object>> content = content(result);

        assertSoftly(softly -> {
            softly.assertThat(result.get("totalElements")).isEqualTo(3);
            softly.assertThat(content).noneMatch(review -> review.get("comment").equals("타인 비공개"));
            softly.assertThat(content).noneMatch(review -> review.get("comment").equals("다른 챌린지 본인 공개"));
            softly.assertThat(content).anyMatch(review -> review.get("comment").equals("내 비공개")
                    && review.get("isMyReview").equals(true));
            softly.assertThat(content).anyMatch(review -> review.get("comment").equals("타인 공개")
                    && review.get("nickname").equals("제나")
                    && review.get("isMyReview").equals(false));
        });
    }

    @Test
    @AdditionalAcceptanceDataSet("acceptance/challenge/review-page.json")
    void 페이징_파라미터가_적용된다() {
        Map<String, Object> result = getReviews(CHALLENGE_ID, MEMBER_ID, 0, 2);

        assertSoftly(softly -> {
            softly.assertThat(content(result)).hasSize(2);
            softly.assertThat(result.get("totalElements")).isEqualTo(3);
            softly.assertThat(result.get("totalPages")).isEqualTo(2);
        });
    }

    @Test
    void 존재하지_않는_챌린지_조회_시_404_를_반환한다() {
        request(MEMBER_ID)
                .when()
                .get("/api/v1/challenges/{challengeId}/reviews", 999_999)
                .then()
                .statusCode(404);
    }

    @Test
    void 비인증_상태로_리뷰_목록을_조회하면_401_을_반환한다() {
        RestAssured.given()
                .accept(ContentType.JSON)
                .when()
                .get("/api/v1/challenges/{challengeId}/reviews", CHALLENGE_ID)
                .then()
                .statusCode(401);
    }

    @Test
    @AdditionalAcceptanceDataSet("acceptance/challenge/review-my.json")
    void getMyReview_내_리뷰가_존재하면_본문을_반환한다() {
        Map<String, Object> result = getMyReview(CHALLENGE_ID, MEMBER_ID);

        assertSoftly(softly -> {
            softly.assertThat(result.get("reviewId")).isEqualTo(1);
            softly.assertThat(result.get("nickname")).isEqualTo("나밍곰");
            softly.assertThat(result.get("comment")).isEqualTo("내 리뷰");
            softly.assertThat(result.get("isPrivate")).isEqualTo(true);
            softly.assertThat(result).doesNotContainKey("isMyReview");
        });
    }

    @Test
    @AdditionalAcceptanceDataSet("acceptance/challenge/review-other.json")
    void getMyReview_내_리뷰가_없으면_404_를_반환한다() {
        request(MEMBER_ID)
                .when()
                .get("/api/v1/challenges/{challengeId}/reviews/me", CHALLENGE_ID)
                .then()
                .statusCode(404);
    }

    @Test
    void getMyReview_존재하지_않는_챌린지면_404_를_반환한다() {
        request(MEMBER_ID)
                .when()
                .get("/api/v1/challenges/{challengeId}/reviews/me", 999_999)
                .then()
                .statusCode(404);
    }

    @Test
    @AdditionalAcceptanceDataSet("acceptance/challenge/review-other-challenge.json")
    void getMyReview_다른_챌린지의_내_리뷰만_있을_때_404_를_반환한다() {
        request(MEMBER_ID)
                .when()
                .get("/api/v1/challenges/{challengeId}/reviews/me", CHALLENGE_ID)
                .then()
                .statusCode(404);
    }

    @Test
    void getMyReview_비인증_상태이면_401_을_반환한다() {
        RestAssured.given()
                .accept(ContentType.JSON)
                .when()
                .get("/api/v1/challenges/{challengeId}/reviews/me", CHALLENGE_ID)
                .then()
                .statusCode(401);
    }

    @Test
    @ResetsAcceptanceData
    void createReview_정상_요청이면_리뷰가_저장되고_당일_출석이_인정된다() {
        createReview(CHALLENGE_ID, MEMBER_ID, "좋았어요", true)
                .then()
                .statusCode(201);

        assertSoftly(softly -> {
            softly.assertThat(countReviews()).isEqualTo(1);
            softly.assertThat(findReviewComment(CHALLENGE_ID, MEMBER_ID)).isEqualTo("좋았어요");
            softly.assertThat(findReviewIsPrivate(CHALLENGE_ID, MEMBER_ID)).isTrue();
            softly.assertThat(existsDailyResult(PARTICIPANT_ID, TODAY)).isTrue();
            softly.assertThat(countDailyTodos(PARTICIPANT_ID, TODAY, "REVIEW")).isEqualTo(1);
        });
    }

    @Test
    void createReview_비참여자_리뷰_작성_시도는_404_를_반환하고_출석도_인정되지_않는다_IDOR_방어() {
        createReview(CHALLENGE_ID, OTHER_MEMBER_ID, "좋았어요", false)
                .then()
                .statusCode(404);

        assertSoftly(softly -> {
            softly.assertThat(countReviews()).isZero();
            softly.assertThat(countDailyResults()).isZero();
            softly.assertThat(countDailyTodos()).isZero();
        });
    }

    @Test
    @AdditionalAcceptanceDataSet("acceptance/challenge/review-my.json")
    void createReview_이미_본인이_작성한_리뷰가_있으면_400_을_반환한다() {
        createReview(CHALLENGE_ID, MEMBER_ID, "중복 시도", false)
                .then()
                .statusCode(400);

        assertThat(countReviews()).isEqualTo(1);
    }

    @Test
    void createReview_존재하지_않는_챌린지면_404_를_반환한다() {
        createReview(999_999, MEMBER_ID, "좋았어요", false)
                .then()
                .statusCode(404);
    }

    @Test
    void createReview_비인증_상태이면_401_을_반환한다() {
        RestAssured.given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(Map.of("comment", "좋았어요", "isPrivate", false))
                .when()
                .post("/api/v1/challenges/{challengeId}/reviews", CHALLENGE_ID)
                .then()
                .statusCode(401);
    }

    @Test
    @ResetsAcceptanceData
    void createReview_동시_요청이_여러_건이어도_정확히_한_건만_저장되고_나머지는_400_을_반환한다() throws Exception {
        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);
        AtomicInteger createdCount = new AtomicInteger();
        AtomicInteger badRequestCount = new AtomicInteger();

        for (int index = 0; index < threadCount; index++) {
            executor.submit(() -> {
                try {
                    ready.countDown();
                    start.await();
                    int status = createReview(CHALLENGE_ID, MEMBER_ID, "동시 요청", false)
                            .andReturn()
                            .statusCode();
                    if (status == 201) {
                        createdCount.incrementAndGet();
                    } else if (status == 400) {
                        badRequestCount.incrementAndGet();
                    }
                } catch (Exception ignored) {
                } finally {
                    done.countDown();
                }
            });
        }

        ready.await();
        start.countDown();
        done.await(10, TimeUnit.SECONDS);
        executor.shutdownNow();

        assertSoftly(softly -> {
            softly.assertThat(createdCount.get()).isEqualTo(1);
            softly.assertThat(badRequestCount.get()).isEqualTo(threadCount - 1);
            softly.assertThat(countReviews()).isEqualTo(1);
        });
    }

    @Test
    void createReview_comment_가_빈_문자열이면_400_을_반환한다() {
        createReview(CHALLENGE_ID, MEMBER_ID, "", false)
                .then()
                .statusCode(400);

        assertThat(countReviews()).isZero();
    }

    @Test
    @ResetsAcceptanceData
    @AdditionalAcceptanceDataSet("acceptance/challenge/review-my.json")
    void updateReview_정상_요청이면_리뷰가_갱신된다() {
        updateReview(CHALLENGE_ID, 1, MEMBER_ID, "수정됨", true)
                .then()
                .statusCode(204);

        assertSoftly(softly -> {
            softly.assertThat(findReviewComment(1)).isEqualTo("수정됨");
            softly.assertThat(findReviewIsPrivate(1)).isTrue();
        });
    }

    @Test
    void updateReview_리뷰가_존재하지_않으면_404_를_반환한다() {
        updateReview(CHALLENGE_ID, 999_999, MEMBER_ID, "수정됨", true)
                .then()
                .statusCode(404);
    }

    @Test
    @AdditionalAcceptanceDataSet("acceptance/challenge/review-other-challenge.json")
    void updateReview_path_challengeId_와_review의_challengeId_가_불일치하면_404_를_반환한다() {
        updateReview(CHALLENGE_ID, 5, MEMBER_ID, "수정됨", true)
                .then()
                .statusCode(404);

        assertThat(findReviewComment(5)).isEqualTo("다른 챌린지 본인 리뷰");
    }

    @Test
    @AdditionalAcceptanceDataSet("acceptance/challenge/review-other.json")
    void updateReview_본인_리뷰가_아니면_404_를_반환한다_IDOR_방어() {
        updateReview(CHALLENGE_ID, 2, MEMBER_ID, "가로채기", true)
                .then()
                .statusCode(404);

        assertThat(findReviewComment(2)).isEqualTo("타인 공개");
    }

    @Test
    @AdditionalAcceptanceDataSet("acceptance/challenge/review-my.json")
    void updateReview_비인증_상태이면_401_을_반환한다() {
        RestAssured.given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(Map.of("comment", "수정됨", "isPrivate", true))
                .when()
                .put("/api/v1/challenges/{challengeId}/reviews/{reviewId}", CHALLENGE_ID, 1)
                .then()
                .statusCode(401);
    }

    @Test
    @AdditionalAcceptanceDataSet("acceptance/challenge/review-my.json")
    void updateReview_comment가_빈_문자열이면_400_을_반환한다() {
        updateReview(CHALLENGE_ID, 1, MEMBER_ID, "", true)
                .then()
                .statusCode(400);

        assertThat(findReviewComment(1)).isEqualTo("내 리뷰");
    }

    @Test
    @ResetsAcceptanceData
    void 리뷰_작성_시_진행도의_REVIEW_TODO가_미완료에서_완료로_변경된다() {
        Map<String, Object> before = getMyProgress(CHALLENGE_ID, MEMBER_ID);
        assertThat(hasTodoStatus(before, "REVIEW", "INCOMPLETE")).isTrue();
        assertThat(hasTodoStatus(before, "REVIEW", "COMPLETE")).isFalse();

        createReview(CHALLENGE_ID, MEMBER_ID, "잘 했어요", false)
                .then()
                .statusCode(201);

        Map<String, Object> after = getMyProgress(CHALLENGE_ID, MEMBER_ID);
        assertThat(hasTodoStatus(after, "REVIEW", "COMPLETE")).isTrue();
        assertThat(hasTodoStatus(after, "REVIEW", "INCOMPLETE")).isFalse();
    }

    @Test
    @ResetsAcceptanceData
    void 리뷰_작성만으로_출석이_인정된다() {
        createReview(CHALLENGE_ID, MEMBER_ID, "리뷰만 작성", false)
                .then()
                .statusCode(201);

        assertSoftly(softly -> {
            softly.assertThat(existsDailyResult(PARTICIPANT_ID, TODAY)).isTrue();
            softly.assertThat(findCompletedDays(PARTICIPANT_ID)).isEqualTo(1);
        });
    }

    @Test
    @ResetsAcceptanceData
    @AdditionalAcceptanceDataSet("acceptance/challenge/review-daily-result.json")
    void 코멘트와_리뷰_둘_다_작성해도_출석은_단_1회만_인정된다() {
        updateCompletedDays(PARTICIPANT_ID, 1);

        createReview(CHALLENGE_ID, MEMBER_ID, "리뷰도 작성", false)
                .then()
                .statusCode(201);

        assertSoftly(softly -> {
            softly.assertThat(countDailyResults(PARTICIPANT_ID, TODAY)).isEqualTo(1);
            softly.assertThat(findCompletedDays(PARTICIPANT_ID)).isEqualTo(1);
        });
    }

    @Test
    void 리뷰도_코멘트도_미작성이면_출석이_인정되지_않는다() {
        assertSoftly(softly -> {
            softly.assertThat(existsDailyResult(PARTICIPANT_ID, TODAY)).isFalse();
            softly.assertThat(findCompletedDays(PARTICIPANT_ID)).isZero();
        });
    }

    private static Map<String, Object> getReviews(
            long challengeId,
            long memberId,
            Integer page,
            Integer size
    ) {
        RequestSpecification request = request(memberId);
        if (page != null) {
            request.queryParam("page", page);
        }
        if (size != null) {
            request.queryParam("size", size);
        }

        return request
                .when()
                .get("/api/v1/challenges/{challengeId}/reviews", challengeId)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .jsonPath()
                .getMap("$");
    }

    private static Map<String, Object> getMyReview(long challengeId, long memberId) {
        return request(memberId)
                .when()
                .get("/api/v1/challenges/{challengeId}/reviews/me", challengeId)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .jsonPath()
                .getMap("$");
    }

    private static Response createReview(long challengeId, long memberId, String comment, boolean isPrivate) {
        return request(memberId)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "comment", comment,
                        "isPrivate", isPrivate
                ))
                .when()
                .post("/api/v1/challenges/{challengeId}/reviews", challengeId);
    }

    private static Response updateReview(
            long challengeId,
            long reviewId,
            long memberId,
            String comment,
            boolean isPrivate
    ) {
        return request(memberId)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "comment", comment,
                        "isPrivate", isPrivate
                ))
                .when()
                .put("/api/v1/challenges/{challengeId}/reviews/{reviewId}", challengeId, reviewId);
    }

    private static Map<String, Object> getMyProgress(long challengeId, long memberId) {
        return request(memberId)
                .when()
                .get("/api/v1/challenges/{challengeId}/progress/me", challengeId)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .jsonPath()
                .getMap("$");
    }

    private static RequestSpecification request(long memberId) {
        return RestAssured.given()
                .accept(ContentType.JSON)
                .header(AcceptanceTestHeaders.MEMBER_ID, memberId);
    }

    private int countReviews() {
        Integer count = jdbcTemplate.queryForObject("select count(*) from challenge_review", Integer.class);
        return count == null ? 0 : count;
    }

    private String findReviewComment(long reviewId) {
        return jdbcTemplate.queryForObject(
                "select comment from challenge_review where id = ?",
                String.class,
                reviewId
        );
    }

    private boolean findReviewIsPrivate(long reviewId) {
        Boolean isPrivate = jdbcTemplate.queryForObject(
                "select is_private from challenge_review where id = ?",
                Boolean.class,
                reviewId
        );
        return Boolean.TRUE.equals(isPrivate);
    }

    private String findReviewComment(long challengeId, long memberId) {
        return jdbcTemplate.queryForObject(
                "select comment from challenge_review where challenge_id = ? and member_id = ?",
                String.class,
                challengeId,
                memberId
        );
    }

    private boolean findReviewIsPrivate(long challengeId, long memberId) {
        Boolean isPrivate = jdbcTemplate.queryForObject(
                "select is_private from challenge_review where challenge_id = ? and member_id = ?",
                Boolean.class,
                challengeId,
                memberId
        );
        return Boolean.TRUE.equals(isPrivate);
    }

    private int countDailyTodos() {
        Integer count = jdbcTemplate.queryForObject("select count(*) from challenge_daily_todo", Integer.class);
        return count == null ? 0 : count;
    }

    private int countDailyTodos(long participantId, LocalDate date, String todoType) {
        Integer count = jdbcTemplate.queryForObject(
                """
                        select count(*)
                        from challenge_daily_todo cdt
                        join challenge_todo ct on ct.id = cdt.challenge_todo_id
                        where cdt.participant_id = ?
                          and cdt.todo_date = ?
                          and ct.todo_type = ?
                        """,
                Integer.class,
                participantId,
                date,
                todoType
        );
        return count == null ? 0 : count;
    }

    private int countDailyResults() {
        Integer count = jdbcTemplate.queryForObject("select count(*) from challenge_daily_result", Integer.class);
        return count == null ? 0 : count;
    }

    private boolean existsDailyResult(long participantId, LocalDate date) {
        return countDailyResults(participantId, date) > 0;
    }

    private int countDailyResults(long participantId, LocalDate date) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from challenge_daily_result where participant_id = ? and date = ?",
                Integer.class,
                participantId,
                date
        );
        return count == null ? 0 : count;
    }

    private int findCompletedDays(long participantId) {
        Integer count = jdbcTemplate.queryForObject(
                "select completed_days from challenge_participant where id = ?",
                Integer.class,
                participantId
        );
        return count == null ? 0 : count;
    }

    private void updateCompletedDays(long participantId, int completedDays) {
        jdbcTemplate.update(
                "update challenge_participant set completed_days = ? where id = ?",
                completedDays,
                participantId
        );
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> content(Map<String, Object> page) {
        return (List<Map<String, Object>>) page.get("content");
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> todayTodos(Map<String, Object> progress) {
        return (List<Map<String, Object>>) progress.get("todayTodos");
    }

    private static boolean hasTodoStatus(Map<String, Object> progress, String todoType, String status) {
        return todayTodos(progress).stream()
                .anyMatch(todo -> todo.get("challengeTodoType").equals(todoType)
                        && todo.get("challengeTodoStatus").equals(status));
    }
}
