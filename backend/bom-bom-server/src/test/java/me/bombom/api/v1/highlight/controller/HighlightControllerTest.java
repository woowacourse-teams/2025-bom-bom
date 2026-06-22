package me.bombom.api.v1.highlight.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.restassured.module.mockmvc.response.MockMvcResponse;
import io.restassured.module.mockmvc.specification.MockMvcRequestSpecification;
import java.util.List;
import java.util.Map;
import me.bombom.support.acceptance.AcceptanceTest;
import me.bombom.support.acceptance.AcceptanceTestHeaders;
import me.bombom.support.acceptance.ResetsAcceptanceData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

@AcceptanceTest("acceptance/article/get-articles.json")
class HighlightControllerTest {

    private static final long MEMBER_ID = 1L;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @ResetsAcceptanceData
    void 하이라이트_생성_성공() {
        createHighlight(2, "하이라이트할 텍스트")
                .then()
                .statusCode(201);
    }

    @Test
    @ResetsAcceptanceData
    void 하이라이트_수정_성공() {
        Map<String, Object> result = updateHighlight(1, Map.of("color", "#4caf50"))
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .jsonPath()
                .getMap("$");

        assertThat(result.get("color")).isEqualTo("#4caf50");
    }

    @Test
    void 하이라이트_수정_포맷에_맞지_않는_color_입력() {
        updateHighlight(1, Map.of(
                "color", "4caf50",
                "memo", "수정된 메모"
        )).then()
                .statusCode(400);
    }

    @Test
    void 아티클로_하이라이트를_필터링해_최신순으로_조회한다() {
        Map<String, Object> result = getHighlights(Map.of("articleId", 1));
        List<Map<String, Object>> content = content(result);

        assertSoftly(softly -> {
            softly.assertThat(result.get("totalElements")).isEqualTo(2);
            softly.assertThat(content.get(0).get("text")).isEqualTo("두 번째 하이라이트");
            softly.assertThat(content.get(1).get("text")).isEqualTo("첫 번째 하이라이트");
        });
    }

    @Test
    void 뉴스레터로_하이라이트를_필터링한다() {
        Map<String, Object> result = getHighlights(Map.of("newsletterId", 3, "size", 2));

        assertSoftly(softly -> {
            softly.assertThat(result.get("totalElements")).isEqualTo(3);
            softly.assertThat(content(result)).hasSize(2);
        });
    }

    @Test
    @ResetsAcceptanceData
    void 중복된_위치에_하이라이트를_생성해도_건수가_증가하지_않는다() {
        int beforeCount = countHighlights();

        createHighlight(1, "중복 위치")
                .then()
                .statusCode(201);

        assertThat(countHighlights()).isEqualTo(beforeCount);
    }

    @Test
    void 존재하지_않는_아티클에는_하이라이트를_생성할_수_없다() {
        createHighlight(99999, "존재하지 않는 글")
                .then()
                .statusCode(404);
    }

    @Test
    @ResetsAcceptanceData
    void 하이라이트를_삭제한다() {
        deleteHighlight(1)
                .then()
                .statusCode(204);

        assertThat(countHighlightsById(1)).isZero();
    }

    @Test
    void 존재하지_않는_하이라이트를_수정하면_404를_반환한다() {
        updateHighlight(99999, Map.of("color", "#9c27b0"))
                .then()
                .statusCode(404);
    }

    @Test
    void 뉴스레터별_하이라이트_통계를_조회한다() {
        Map<String, Object> result = authenticatedRequest()
                .when()
                .get("/api/v1/highlights/statistics/newsletters")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .jsonPath()
                .getMap("$");

        assertThat(result.get("totalCount")).isEqualTo(6);
    }

    private static Map<String, Object> getHighlights(Map<String, ?> query) {
        return authenticatedRequest()
                .queryParams(query)
                .when()
                .get("/api/v1/highlights")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .jsonPath()
                .getMap("$");
    }

    private static MockMvcResponse createHighlight(long articleId, String text) {
        return authenticatedRequest()
                .contentType(ContentType.JSON)
                .body(highlightCreateRequest(articleId, text))
                .when()
                .post("/api/v1/highlights");
    }

    private static MockMvcResponse updateHighlight(long highlightId, Map<String, ?> body) {
        return authenticatedRequest()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .patch("/api/v1/highlights/{id}", highlightId);
    }

    private static MockMvcResponse deleteHighlight(long highlightId) {
        return authenticatedRequest()
                .when()
                .delete("/api/v1/highlights/{id}", highlightId);
    }

    private static Map<String, Object> highlightCreateRequest(long articleId, String text) {
        return Map.of(
                "location", Map.of(
                        "startOffset", 0,
                        "startXPath", "div[0]/p[0]",
                        "endOffset", 10,
                        "endXPath", "div[0]/p[0]"
                ),
                "articleId", articleId,
                "color", "#ffeb3b",
                "text", text,
                "memo", "메모 내용"
        );
    }

    private static MockMvcRequestSpecification authenticatedRequest() {
        return RestAssuredMockMvc.given()
                .accept(ContentType.JSON)
                .header(AcceptanceTestHeaders.MEMBER_ID, MEMBER_ID);
    }

    private int countHighlights() {
        Integer count = jdbcTemplate.queryForObject("select count(*) from highlight", Integer.class);
        return count == null ? 0 : count;
    }

    private int countHighlightsById(long highlightId) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from highlight where id = ?",
                Integer.class,
                highlightId
        );
        return count == null ? 0 : count;
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> content(Map<String, Object> page) {
        return (List<Map<String, Object>>) page.get("content");
    }
}
