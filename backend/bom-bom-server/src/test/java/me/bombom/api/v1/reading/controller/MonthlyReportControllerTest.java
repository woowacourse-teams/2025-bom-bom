package me.bombom.api.v1.reading.controller;

import static me.bombom.support.acceptance.AcceptanceTestHeaders.MEMBER_ID;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.List;
import java.util.Map;
import me.bombom.support.acceptance.AcceptanceTest;
import org.junit.jupiter.api.Test;

@AcceptanceTest({
        "acceptance/common/member.json",
        "acceptance/reading/monthly-report.json"
})
class MonthlyReportControllerTest {

    private static final long MEMBER_ID_VALUE = 1L;

    @Test
    void 월간_읽기_캘린더를_조회한다() {
        List<Map<String, Object>> response = getReadingCalendar(2026, 6);

        assertSoftly(softly -> {
            softly.assertThat(response).hasSize(30);
            softly.assertThat(response.get(0).get("date")).isEqualTo("2026-06-01");
            softly.assertThat(response.get(0).get("read")).isEqualTo(true);
            softly.assertThat(response.get(0).get("readCount")).isEqualTo(2);
            softly.assertThat(response.get(1).get("date")).isEqualTo("2026-06-02");
            softly.assertThat(response.get(1).get("read")).isEqualTo(true);
            softly.assertThat(response.get(1).get("readCount")).isEqualTo(1);
            softly.assertThat(response.get(2).get("date")).isEqualTo("2026-06-03");
            softly.assertThat(response.get(2).get("read")).isEqualTo(false);
            softly.assertThat(response.get(2).get("readCount")).isEqualTo(0);
        });
    }

    @Test
    void 월간_읽기_대시보드를_조회한다() {
        Map<String, Object> response = getReadingDashboard(2026, 6, 1);
        List<Map<String, Object>> frequentReadNewsletters = frequentReadNewsletters(response);

        assertSoftly(softly -> {
            softly.assertThat(response.get("readArticleCount")).isEqualTo(3);
            softly.assertThat(response.get("readArticleChangeRate")).isEqualTo(50.0F);
            softly.assertThat(response.get("readArticleChangeDirection")).isEqualTo("UP");
            softly.assertThat(response.get("bookmarkCount")).isEqualTo(2);
            softly.assertThat(frequentReadNewsletters).hasSize(1);
            softly.assertThat(frequentReadNewsletters.getFirst().get("rank")).isEqualTo(1);
            softly.assertThat(frequentReadNewsletters.getFirst().get("newsletterId")).isEqualTo(2);
            softly.assertThat(frequentReadNewsletters.getFirst().get("name")).isEqualTo("IT타임즈");
            softly.assertThat(frequentReadNewsletters.getFirst().get("readCount")).isEqualTo(2);
        });
    }

    private static List<Map<String, Object>> getReadingCalendar(int year, int month) {
        return authenticatedRequest()
                .queryParam("year", year)
                .queryParam("month", month)
                .when()
                .get("/api/v1/members/me/reading/calendar")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .jsonPath()
                .getList("$");
    }

    private static Map<String, Object> getReadingDashboard(int year, int month, int limit) {
        return authenticatedRequest()
                .queryParam("year", year)
                .queryParam("month", month)
                .queryParam("limit", limit)
                .when()
                .get("/api/v1/members/me/reading/dashboard")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .jsonPath()
                .getMap("$");
    }

    private static io.restassured.specification.RequestSpecification authenticatedRequest() {
        return RestAssured.given()
                .accept(ContentType.JSON)
                .header(MEMBER_ID, MEMBER_ID_VALUE);
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> frequentReadNewsletters(Map<String, Object> response) {
        return (List<Map<String, Object>>) response.get("frequentReadNewsletters");
    }
}
