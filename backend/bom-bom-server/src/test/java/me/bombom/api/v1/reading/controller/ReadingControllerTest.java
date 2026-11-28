package me.bombom.api.v1.reading.controller;

import static me.bombom.support.acceptance.AcceptanceTestHeaders.MEMBER_ID;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import me.bombom.support.acceptance.AcceptanceTest;
import me.bombom.support.acceptance.ResetsAcceptanceData;
import me.bombom.support.time.MutableClock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

@AcceptanceTest({
        "acceptance/common/member.json",
        "acceptance/reading/reading.json"
})
class ReadingControllerTest {

    private static final long MEMBER_ID_VALUE = 1L;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private Clock clock;

    @Autowired
    private MutableClock mutableClock;

    @Test
    void 인증된_사용자는_읽기_현황을_조회할_수_있다() {
        Map<String, Object> response = authenticatedGet("/api/v1/members/me/reading");

        assertSoftly(softly -> {
            softly.assertThat(response.get("streakReadDay")).isEqualTo(10);
            softly.assertThat(nested(response, "today").get("readCount")).isEqualTo(1);
            softly.assertThat(nested(response, "today").get("totalCount")).isEqualTo(3);
            softly.assertThat(nested(response, "weekly").get("readCount")).isEqualTo(3);
            softly.assertThat(nested(response, "weekly").get("goalCount")).isEqualTo(5);
        });
    }

    @Test
    @ResetsAcceptanceData
    void 인증된_사용자는_주간_목표를_수정할_수_있다() {
        Map<String, Object> response = RestAssured.given()
                .accept(ContentType.JSON)
                .header(MEMBER_ID, MEMBER_ID_VALUE)
                .queryParam("weeklyGoalCount", 8)
                .when()
                .patch("/api/v1/members/me/reading/progress/week/goal")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .jsonPath()
                .getMap("$");

        assertSoftly(softly -> {
            softly.assertThat(response.get("weeklyReadingId")).isEqualTo(1);
            softly.assertThat(response.get("weeklyGoalCount")).isEqualTo(8);
        });
    }

    @Test
    void 인증된_사용자는_월간_읽기수를_조회할_수_있다() {
        Map<String, Object> response = authenticatedGet("/api/v1/members/me/reading/month");

        assertThatReadCount(response, 7);
    }

    @Test
    void 월간_읽기_랭킹을_조회할_수_있다() {
        Map<String, Object> response = RestAssured.given()
                .accept(ContentType.JSON)
                .queryParam("limit", 10)
                .when()
                .get("/api/v1/members/me/reading/month/rank")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .jsonPath()
                .getMap("$");

        Map<String, Object> first = firstData(response);
        assertSoftly(softly -> {
            softly.assertThat(first.get("nickname")).isEqualTo("인수테스트회원");
            softly.assertThat(first.get("rank")).isEqualTo(1);
            softly.assertThat(first.get("monthlyReadCount")).isEqualTo(7);
        });
    }

    @Test
    void 연속_읽기_랭킹을_조회할_수_있다() {
        Map<String, Object> response = RestAssured.given()
                .accept(ContentType.JSON)
                .queryParam("limit", 10)
                .when()
                .get("/api/v1/members/me/reading/streak/rank")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .jsonPath()
                .getMap("$");

        Map<String, Object> first = firstData(response);
        assertSoftly(softly -> {
            softly.assertThat(first.get("nickname")).isEqualTo("인수테스트회원");
            softly.assertThat(first.get("rank")).isEqualTo(1);
            softly.assertThat(first.get("dayCount")).isEqualTo(10);
        });
    }

    @Test
    void 인증된_사용자는_내_월간_읽기_랭킹을_조회할_수_있다() {
        Map<String, Object> response = authenticatedGet("/api/v1/members/me/reading/month/rank/me");

        assertSoftly(softly -> {
            softly.assertThat(response.get("nickname")).isEqualTo("인수테스트회원");
            softly.assertThat(response.get("rank")).isEqualTo(1);
            softly.assertThat(response.get("monthlyReadCount")).isEqualTo(7);
            softly.assertThat(response.get("nextRankDifference")).isEqualTo(0);
        });
    }

    @Test
    void 인증된_사용자는_내_연속_읽기_랭킹을_조회할_수_있다() {
        Map<String, Object> response = authenticatedGet("/api/v1/members/me/reading/streak/rank/me");
        Map<String, Object> streakShield = nested(response, "streakShield");

        assertSoftly(softly -> {
            softly.assertThat(response.get("nickname")).isEqualTo("인수테스트회원");
            softly.assertThat(response.get("rank")).isEqualTo(1);
            softly.assertThat(response.get("dayCount")).isEqualTo(10);
            softly.assertThat(streakShield.get("status")).isEqualTo("AVAILABLE");
            softly.assertThat(streakShield.get("remainingCount")).isEqualTo(1);
            softly.assertThat(streakShield.get("monthlyLimit")).isEqualTo(1);
        });
    }

    @Test
    @ResetsAcceptanceData
    void 인증된_사용자는_사용_완료된_연속_읽기_보호막_현황을_조회할_수_있다() {
        jdbcTemplate.update("""
                UPDATE continue_reading_shield
                SET monthly_remaining_count = 0,
                    reward_remaining_count = 0
                WHERE member_id = ?
                """, MEMBER_ID_VALUE);

        Map<String, Object> response = authenticatedGet("/api/v1/members/me/reading/streak/rank/me");
        Map<String, Object> streakShield = nested(response, "streakShield");

        assertSoftly(softly -> {
            softly.assertThat(streakShield.get("status")).isEqualTo("USED");
            softly.assertThat(streakShield.get("remainingCount")).isEqualTo(0);
            softly.assertThat(streakShield.get("monthlyLimit")).isEqualTo(1);
        });
    }

    @Test
    @ResetsAcceptanceData
    void 인증된_사용자는_최근_연속_읽기_기록을_조회할_수_있다() {
        // 2026-11-27(금) 12:00 KST로 고정해 요일에 따라 결과가 달라지지 않게 한다
        mutableClock.setInstant(Instant.parse("2026-11-27T03:00:00Z"), ZoneId.of("Asia/Seoul"));
        LocalDate today = LocalDate.now(clock);
        LocalDate yesterday = today.minusDays(1);
        jdbcTemplate.update("UPDATE member SET created_at = ? WHERE id = ?",
                today.minusDays(30).atStartOfDay(), MEMBER_ID_VALUE);
        jdbcTemplate.update("""
                INSERT INTO article_read_history (member_id, article_id, newsletter_id, category_id, read_at, created_at, updated_at)
                VALUES (?, 1, 1, 1, ?, NOW(6), NOW(6))
                """, MEMBER_ID_VALUE, today.atTime(9, 0));
        jdbcTemplate.update("""
                INSERT INTO continue_reading_shield_history (member_id, type, reason, event_date, quantity, created_at, updated_at)
                VALUES (?, 'USE', 'DAILY_RESET_PROTECTION_USE', ?, 1, NOW(6), NOW(6))
                """, MEMBER_ID_VALUE, yesterday);
        jdbcTemplate.update("""
                INSERT INTO continue_reading_daily_result (member_id, result_date, result)
                VALUES (?, ?, 'NO_ARTICLE')
                """, MEMBER_ID_VALUE, today.minusDays(2));

        Map<String, Object> response = authenticatedGet("/api/v1/members/me/reading/streak/recent");
        Map<String, Object> streakShield = nested(response, "streakShield");
        List<Map<String, Object>> days = dayList(response);

        assertSoftly(softly -> {
            softly.assertThat(response.get("dayCount")).isEqualTo(10);
            softly.assertThat(response.get("rank")).isEqualTo(1);
            softly.assertThat(streakShield.get("status")).isEqualTo("AVAILABLE");
            softly.assertThat(streakShield.get("remainingCount")).isEqualTo(1);
            softly.assertThat(days).hasSize(7);
            softly.assertThat(days.getFirst().get("date")).isEqualTo(today.minusDays(6).toString());
            softly.assertThat(days.get(4)).containsEntry("date", today.minusDays(2).toString()).containsEntry("status", "NO_ARTICLE");
            softly.assertThat(days.get(5)).containsEntry("date", yesterday.toString()).containsEntry("status", "SHIELD");
            softly.assertThat(days.getLast()).containsEntry("date", today.toString()).containsEntry("status", "READ");
        });
    }

    @Test
    void 최근_연속_읽기_기록은_days만큼_조회한다() {
        mutableClock.setInstant(Instant.parse("2026-11-27T03:00:00Z"), ZoneId.of("Asia/Seoul"));
        Map<String, Object> response = RestAssured.given()
                .accept(ContentType.JSON)
                .header(MEMBER_ID, MEMBER_ID_VALUE)
                .queryParam("days", 31)
                .when()
                .get("/api/v1/members/me/reading/streak/recent")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getMap("$");

        // 기본 데이터셋의 today_reading.read_count가 1이라 오늘은 READ다
        assertSoftly(softly -> {
            softly.assertThat(dayList(response)).hasSize(31);
            softly.assertThat(dayList(response).getLast())
                    .containsEntry("date", LocalDate.now(clock).toString())
                    .containsEntry("status", "READ");
        });
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 32})
    void 최근_연속_읽기_기록의_days가_1에서_31_사이가_아니면_400이다(int days) {
        RestAssured.given()
                .accept(ContentType.JSON)
                .header(MEMBER_ID, MEMBER_ID_VALUE)
                .queryParam("days", days)
                .when()
                .get("/api/v1/members/me/reading/streak/recent")
                .then()
                .statusCode(400);
    }

    @Test
    void 인증되지_않은_사용자는_최근_연속_읽기_기록을_조회할_수_없다() {
        RestAssured.given()
                .accept(ContentType.JSON)
                .when()
                .get("/api/v1/members/me/reading/streak/recent")
                .then()
                .statusCode(401);
    }

    @Test
    void 인증되지_않은_사용자는_읽기_현황을_조회할_수_없다() {
        RestAssured.given()
                .accept(ContentType.JSON)
                .when()
                .get("/api/v1/members/me/reading")
                .then()
                .statusCode(401);
    }

    private static Map<String, Object> authenticatedGet(String path) {
        return RestAssured.given()
                .accept(ContentType.JSON)
                .header(MEMBER_ID, MEMBER_ID_VALUE)
                .when()
                .get(path)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .jsonPath()
                .getMap("$");
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> nested(Map<String, Object> source, String key) {
        return (Map<String, Object>) source.get(key);
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> dayList(Map<String, Object> response) {
        return (List<Map<String, Object>>) response.get("days");
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> firstData(Map<String, Object> response) {
        return ((List<Map<String, Object>>) response.get("data")).getFirst();
    }

    private static void assertThatReadCount(Map<String, Object> response, int readCount) {
        assertSoftly(softly -> softly.assertThat(response.get("readCount")).isEqualTo(readCount));
    }
}
