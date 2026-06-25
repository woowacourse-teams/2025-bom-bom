package me.bombom.api.v1.reading.controller;

import static me.bombom.support.acceptance.AcceptanceTestHeaders.MEMBER_ID;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.List;
import java.util.Map;
import me.bombom.support.acceptance.AcceptanceTest;
import me.bombom.support.acceptance.ResetsAcceptanceData;
import org.junit.jupiter.api.Test;

@AcceptanceTest({
        "acceptance/common/member.json",
        "acceptance/reading/reading.json"
})
class ReadingControllerTest {

    private static final long MEMBER_ID_VALUE = 1L;

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

        assertSoftly(softly -> {
            softly.assertThat(response.get("nickname")).isEqualTo("인수테스트회원");
            softly.assertThat(response.get("rank")).isEqualTo(1);
            softly.assertThat(response.get("dayCount")).isEqualTo(10);
        });
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
    private static Map<String, Object> firstData(Map<String, Object> response) {
        return ((List<Map<String, Object>>) response.get("data")).getFirst();
    }

    private static void assertThatReadCount(Map<String, Object> response, int readCount) {
        assertSoftly(softly -> softly.assertThat(response.get("readCount")).isEqualTo(readCount));
    }
}
