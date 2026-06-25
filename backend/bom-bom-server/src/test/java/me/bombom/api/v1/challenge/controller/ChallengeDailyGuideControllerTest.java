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
import me.bombom.support.time.MutableClock;
import me.bombom.support.acceptance.AcceptanceTest;
import me.bombom.support.acceptance.AcceptanceTestHeaders;
import me.bombom.support.acceptance.AdditionalAcceptanceDataSet;
import me.bombom.support.acceptance.ResetsAcceptanceData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

@AcceptanceTest("acceptance/challenge/daily-guide.json")
class ChallengeDailyGuideControllerTest {

    private static final long MEMBER_ID = 1L;
    private static final long NON_PARTICIPANT_MEMBER_ID = 3L;
    private static final long CHALLENGE_ID = 1L;
    private static final long FUTURE_CHALLENGE_ID = 2L;
    private static final long PARTICIPANT_ID = 1L;
    private static final LocalDate MONDAY = LocalDate.of(2026, 1, 26);
    private static final LocalDate SATURDAY = LocalDate.of(2026, 1, 31);
    private static final String COMMENT = "뉴스레터 읽기 팁을 공유합니다";

    @Autowired
    private MutableClock clock;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void 시간을_월요일로_고정한다() {
        clock.setDate(MONDAY);
    }

    @Test
    void 오늘의_데일리_가이드를_조회한다() {
        Map<String, Object> result = getTodayDailyGuide(CHALLENGE_ID, MEMBER_ID);

        assertSoftly(softly -> {
            softly.assertThat(result.get("dayIndex")).isEqualTo(6);
            softly.assertThat(result.get("type")).isEqualTo("COMMENT");
            softly.assertThat(result.get("imageUrl")).isEqualTo("https://example.com/daily-guide.webp");
            softly.assertThat(result.get("notice")).isEqualTo("오늘은 팁을 남겨주세요");
            softly.assertThat(result.get("commentEnabled")).isEqualTo(true);
            softly.assertThat(myComment(result).get("exists")).isEqualTo(false);
            softly.assertThat(myComment(result).get("content")).isNull();
            softly.assertThat(myComment(result).get("createdAt")).isNull();
        });
    }

    @Test
    @AdditionalAcceptanceDataSet("acceptance/challenge/daily-guide-comments.json")
    void 작성한_댓글과_함께_오늘의_데일리_가이드를_조회한다() {
        Map<String, Object> result = getTodayDailyGuide(CHALLENGE_ID, MEMBER_ID);

        assertSoftly(softly -> {
            softly.assertThat(myComment(result).get("exists")).isEqualTo(true);
            softly.assertThat(myComment(result).get("content")).isEqualTo(COMMENT);
            softly.assertThat(myComment(result).get("createdAt")).isNotNull();
        });
    }

    @Test
    void 참여하지_않은_회원은_오늘의_데일리_가이드를_조회할_수_없다() {
        request(NON_PARTICIPANT_MEMBER_ID)
                .when()
                .get("/api/v1/challenges/{challengeId}/daily-guides/today", CHALLENGE_ID)
                .then()
                .statusCode(404);
    }

    @Test
    void 존재하지_않는_챌린지의_오늘의_데일리_가이드는_조회할_수_없다() {
        request(MEMBER_ID)
                .when()
                .get("/api/v1/challenges/{challengeId}/daily-guides/today", 999)
                .then()
                .statusCode(404);
    }

    @Test
    void 진행_기간이_아닌_챌린지의_오늘의_데일리_가이드는_조회할_수_없다() {
        request(MEMBER_ID)
                .when()
                .get("/api/v1/challenges/{challengeId}/daily-guides/today", FUTURE_CHALLENGE_ID)
                .then()
                .statusCode(400);
    }

    @Test
    @ResetsAcceptanceData
    void 데일리_가이드에_댓글을_작성한다() {
        createDailyGuideComment(CHALLENGE_ID, 2, MEMBER_ID, COMMENT)
                .then()
                .statusCode(201);

        assertSoftly(softly -> {
            softly.assertThat(countDailyGuideComments()).isEqualTo(1);
            softly.assertThat(findDailyGuideCommentContent(2, PARTICIPANT_ID)).isEqualTo(COMMENT);
        });
    }

    @Test
    @AdditionalAcceptanceDataSet("acceptance/challenge/daily-guide-comments.json")
    void 이미_댓글을_작성한_가이드에는_다시_작성할_수_없다() {
        createDailyGuideComment(CHALLENGE_ID, 6, MEMBER_ID, "새 댓글")
                .then()
                .statusCode(400);
    }

    @Test
    void 댓글_작성이_비활성화된_가이드에는_댓글을_작성할_수_없다() {
        createDailyGuideComment(CHALLENGE_ID, 3, MEMBER_ID, COMMENT)
                .then()
                .statusCode(400);
    }

    @Test
    void 존재하지_않는_가이드에는_댓글을_작성할_수_없다() {
        createDailyGuideComment(CHALLENGE_ID, 5, MEMBER_ID, COMMENT)
                .then()
                .statusCode(404);
    }

    @Test
    void 아직_열리지_않은_가이드에는_댓글을_작성할_수_없다() {
        createDailyGuideComment(CHALLENGE_ID, 7, MEMBER_ID, COMMENT)
                .then()
                .statusCode(400);
    }

    @Test
    void 빈_내용으로는_댓글을_작성할_수_없다() {
        createDailyGuideComment(CHALLENGE_ID, 6, MEMBER_ID, "")
                .then()
                .statusCode(400);
    }

    @Test
    @ResetsAcceptanceData
    void 첫날_댓글을_작성하면_MINDSET_투두와_출석이_완료된다() {
        createDailyGuideComment(CHALLENGE_ID, 1, MEMBER_ID, "첫날 댓글")
                .then()
                .statusCode(201);

        assertSoftly(softly -> {
            softly.assertThat(existsDailyTodo(PARTICIPANT_ID, MONDAY, "READ")).isTrue();
            softly.assertThat(existsDailyTodo(PARTICIPANT_ID, MONDAY, "COMMENT")).isTrue();
            softly.assertThat(existsDailyTodo(PARTICIPANT_ID, MONDAY, "MINDSET")).isTrue();
            softly.assertThat(existsDailyResult(PARTICIPANT_ID, MONDAY)).isTrue();
            softly.assertThat(findCompletedDays(PARTICIPANT_ID)).isEqualTo(1);
        });
    }

    @Test
    @ResetsAcceptanceData
    void 첫날이_아니면_댓글을_작성해도_챌린지_투두가_완료되지_않는다() {
        createDailyGuideComment(CHALLENGE_ID, 2, MEMBER_ID, "둘째 날 댓글")
                .then()
                .statusCode(201);

        assertSoftly(softly -> {
            softly.assertThat(countDailyTodos(PARTICIPANT_ID, MONDAY)).isZero();
            softly.assertThat(existsDailyResult(PARTICIPANT_ID, MONDAY)).isFalse();
            softly.assertThat(findCompletedDays(PARTICIPANT_ID)).isZero();
        });
    }

    @Test
    @ResetsAcceptanceData
    @AdditionalAcceptanceDataSet("acceptance/challenge/daily-guide-mindset-todo.json")
    void 첫날_MINDSET_투두가_이미_있으면_중복으로_생성하지_않는다() {
        createDailyGuideComment(CHALLENGE_ID, 1, MEMBER_ID, "첫날 댓글")
                .then()
                .statusCode(201);

        assertThat(countDailyTodos(PARTICIPANT_ID, MONDAY, "MINDSET")).isEqualTo(1);
    }

    @Test
    @ResetsAcceptanceData
    @AdditionalAcceptanceDataSet("acceptance/challenge/daily-guide-result.json")
    void 첫날_출석이_이미_완료되었으면_출석과_완료일을_중복으로_반영하지_않는다() {
        createDailyGuideComment(CHALLENGE_ID, 1, MEMBER_ID, "첫날 댓글")
                .then()
                .statusCode(201);

        assertSoftly(softly -> {
            softly.assertThat(countDailyResults(PARTICIPANT_ID, MONDAY)).isEqualTo(1);
            softly.assertThat(findCompletedDays(PARTICIPANT_ID)).isZero();
            softly.assertThat(existsDailyTodo(PARTICIPANT_ID, MONDAY, "COMMENT")).isTrue();
            softly.assertThat(existsDailyTodo(PARTICIPANT_ID, MONDAY, "MINDSET")).isTrue();
        });
    }

    @Test
    @ResetsAcceptanceData
    void 주말에_첫날_댓글을_작성하면_READ를_제외한_투두와_출석이_완료된다() {
        clock.setDate(SATURDAY);

        createDailyGuideComment(CHALLENGE_ID, 1, MEMBER_ID, "주말 첫날 댓글")
                .then()
                .statusCode(201);

        assertSoftly(softly -> {
            softly.assertThat(existsDailyTodo(PARTICIPANT_ID, SATURDAY, "READ")).isFalse();
            softly.assertThat(existsDailyTodo(PARTICIPANT_ID, SATURDAY, "COMMENT")).isTrue();
            softly.assertThat(existsDailyTodo(PARTICIPANT_ID, SATURDAY, "MINDSET")).isTrue();
            softly.assertThat(existsDailyResult(PARTICIPANT_ID, SATURDAY)).isTrue();
            softly.assertThat(findCompletedDays(PARTICIPANT_ID)).isEqualTo(1);
        });
    }

    @Test
    void 주말에는_주말용_데일리_가이드를_조회한다() {
        clock.setDate(SATURDAY);

        Map<String, Object> result = getTodayDailyGuide(CHALLENGE_ID, MEMBER_ID);

        assertSoftly(softly -> {
            softly.assertThat(result.get("dayIndex")).isEqualTo(0);
            softly.assertThat(result.get("type")).isEqualTo("COMMENT");
            softly.assertThat(result.get("imageUrl")).isEqualTo("https://example.com/daily-guide.webp");
        });
    }

    @Test
    @AdditionalAcceptanceDataSet("acceptance/challenge/daily-guide-comments.json")
    void 데일리_가이드의_댓글_목록을_조회한다() {
        Map<String, Object> result = getDailyGuideComments(CHALLENGE_ID, 6, MEMBER_ID, null);

        assertSoftly(softly -> {
            softly.assertThat(content(result)).hasSize(2);
            softly.assertThat(result.get("totalElements")).isEqualTo(2);
            softly.assertThat(result.get("totalPages")).isEqualTo(1);
        });
    }

    @Test
    @AdditionalAcceptanceDataSet("acceptance/challenge/daily-guide-comments.json")
    void 데일리_가이드의_댓글_목록을_페이지_크기에_맞게_조회한다() {
        Map<String, Object> result = getDailyGuideComments(CHALLENGE_ID, 6, MEMBER_ID, 1);

        assertSoftly(softly -> {
            softly.assertThat(content(result)).hasSize(1);
            softly.assertThat(result.get("totalElements")).isEqualTo(2);
            softly.assertThat(result.get("totalPages")).isEqualTo(2);
        });
    }

    @Test
    void 존재하지_않는_챌린지의_댓글_목록은_조회할_수_없다() {
        request(MEMBER_ID)
                .when()
                .get("/api/v1/challenges/{challengeId}/daily-guides/{dayIndex}/comments", 999, 6)
                .then()
                .statusCode(404);
    }

    @Test
    void 챌린지_범위를_벗어난_일차의_댓글_목록은_조회할_수_없다() {
        request(MEMBER_ID)
                .when()
                .get("/api/v1/challenges/{challengeId}/daily-guides/{dayIndex}/comments", CHALLENGE_ID, 11)
                .then()
                .statusCode(400);
    }

    @Test
    void 존재하지_않는_가이드의_댓글_목록은_조회할_수_없다() {
        request(MEMBER_ID)
                .when()
                .get("/api/v1/challenges/{challengeId}/daily-guides/{dayIndex}/comments", CHALLENGE_ID, 5)
                .then()
                .statusCode(404);
    }

    @Test
    void 댓글이_없으면_빈_목록을_반환한다() {
        Map<String, Object> result = getDailyGuideComments(CHALLENGE_ID, 6, MEMBER_ID, null);

        assertSoftly(softly -> {
            softly.assertThat(content(result)).isEmpty();
            softly.assertThat(result.get("totalElements")).isEqualTo(0);
            softly.assertThat(result.get("totalPages")).isEqualTo(0);
        });
    }

    @Test
    void 참여하지_않은_회원은_데일리_가이드의_댓글_목록을_조회할_수_없다() {
        request(NON_PARTICIPANT_MEMBER_ID)
                .when()
                .get("/api/v1/challenges/{challengeId}/daily-guides/{dayIndex}/comments", CHALLENGE_ID, 6)
                .then()
                .statusCode(404);
    }

    @Test
    @AdditionalAcceptanceDataSet("acceptance/challenge/daily-guide-comments.json")
    void 데일리_가이드에_작성한_내_댓글을_조회한다() {
        Map<String, Object> result = getMyDailyGuideComment(CHALLENGE_ID, 6, MEMBER_ID);

        assertThat(result.get("comment")).isEqualTo(COMMENT);
    }

    @Test
    void 작성한_댓글이_없으면_내_댓글_내용은_null이다() {
        Map<String, Object> result = getMyDailyGuideComment(CHALLENGE_ID, 6, MEMBER_ID);

        assertThat(result.get("comment")).isNull();
    }

    @Test
    void 존재하지_않는_챌린지에서는_내_댓글을_조회할_수_없다() {
        request(MEMBER_ID)
                .when()
                .get("/api/v1/challenges/{challengeId}/daily-guides/{dayIndex}/my-comment", 999, 6)
                .then()
                .statusCode(404);
    }

    @Test
    void 참여하지_않은_회원은_내_댓글을_조회할_수_없다() {
        request(NON_PARTICIPANT_MEMBER_ID)
                .when()
                .get("/api/v1/challenges/{challengeId}/daily-guides/{dayIndex}/my-comment", CHALLENGE_ID, 6)
                .then()
                .statusCode(404);
    }

    @Test
    void 아직_열리지_않은_가이드의_내_댓글은_조회할_수_없다() {
        request(MEMBER_ID)
                .when()
                .get("/api/v1/challenges/{challengeId}/daily-guides/{dayIndex}/my-comment", CHALLENGE_ID, 7)
                .then()
                .statusCode(400);
    }

    private static Map<String, Object> getTodayDailyGuide(long challengeId, long memberId) {
        return request(memberId)
                .when()
                .get("/api/v1/challenges/{challengeId}/daily-guides/today", challengeId)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .jsonPath()
                .getMap("$");
    }

    private static Response createDailyGuideComment(
            long challengeId,
            int dayIndex,
            long memberId,
            String content
    ) {
        return request(memberId)
                .contentType(ContentType.JSON)
                .body(Map.of("content", content))
                .when()
                .post("/api/v1/challenges/{challengeId}/daily-guides/{dayIndex}/my-comment", challengeId, dayIndex);
    }

    private static Map<String, Object> getDailyGuideComments(
            long challengeId,
            int dayIndex,
            long memberId,
            Integer size
    ) {
        RequestSpecification request = request(memberId);
        if (size != null) {
            request.queryParam("size", size);
        }

        return request
                .when()
                .get("/api/v1/challenges/{challengeId}/daily-guides/{dayIndex}/comments", challengeId, dayIndex)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .jsonPath()
                .getMap("$");
    }

    private static Map<String, Object> getMyDailyGuideComment(long challengeId, int dayIndex, long memberId) {
        return request(memberId)
                .when()
                .get("/api/v1/challenges/{challengeId}/daily-guides/{dayIndex}/my-comment", challengeId, dayIndex)
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

    private int countDailyGuideComments() {
        Integer count = jdbcTemplate.queryForObject("select count(*) from challenge_daily_guide_comment", Integer.class);
        return count == null ? 0 : count;
    }

    private String findDailyGuideCommentContent(long guideId, long participantId) {
        return jdbcTemplate.queryForObject(
                "select content from challenge_daily_guide_comment where guide_id = ? and participant_id = ?",
                String.class,
                guideId,
                participantId
        );
    }

    private boolean existsDailyTodo(long participantId, LocalDate date, String todoType) {
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
        return count != null && count > 0;
    }

    private int countDailyTodos(long participantId, LocalDate date) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from challenge_daily_todo where participant_id = ? and todo_date = ?",
                Integer.class,
                participantId,
                date
        );
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

    @SuppressWarnings("unchecked")
    private static Map<String, Object> myComment(Map<String, Object> result) {
        return (Map<String, Object>) result.get("myComment");
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> content(Map<String, Object> page) {
        return (List<Map<String, Object>>) page.get("content");
    }
}
