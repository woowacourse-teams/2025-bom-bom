package me.bombom.api.v1.bookmark.controller;

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
class BookmarkControllerTest {

    private static final long MEMBER_ID = 1L;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void 북마크_목록을_조회한다() {
        Map<String, Object> result = getBookmarks(Map.of());

        assertSoftly(softly -> {
            softly.assertThat(result.get("totalElements")).isEqualTo(1);
            softly.assertThat(content(result).getFirst().get("title")).isEqualTo("뉴스");
        });
    }

    @Test
    void 북마크_상태를_조회한다() {
        Map<String, Object> result = getBookmarkStatus(1);

        assertThat(result.get("bookmarkStatus")).isEqualTo(true);
    }

    @Test
    @ResetsAcceptanceData
    void 북마크를_추가한다() {
        postBookmark(2).then().statusCode(204);

        assertThat(countBookmarks(1, 2)).isEqualTo(1);
    }

    @Test
    @ResetsAcceptanceData
    void 북마크를_삭제한다() {
        deleteBookmark(1).then().statusCode(204);

        assertThat(countBookmarks(1, 1)).isZero();
    }

    @Test
    void 뉴스레터별_북마크_통계를_조회한다() {
        Map<String, Object> result = getBookmarkNewsletterStatistics();

        assertThat(result.get("totalCount")).isEqualTo(1);
    }

    @Test
    @ResetsAcceptanceData
    void 뉴스레터로_북마크_목록을_필터링한다() {
        postBookmark(2).then().statusCode(204);

        Map<String, Object> result = getBookmarks(Map.of("newsletterId", 2));

        assertSoftly(softly -> {
            softly.assertThat(result.get("totalElements")).isEqualTo(1);
            softly.assertThat(content(result).getFirst().get("articleId")).isEqualTo(2);
        });
    }

    @Test
    @ResetsAcceptanceData
    void 같은_아티클을_중복_북마크해도_한_건만_저장한다() {
        postBookmark(2).then().statusCode(204);
        postBookmark(2).then().statusCode(204);

        assertThat(countBookmarks(1, 2)).isEqualTo(1);
    }

    @Test
    void 다른_사용자의_아티클은_북마크할_수_없다() {
        postBookmark(12).then().statusCode(403);
    }

    @Test
    void 다른_사용자의_아티클_북마크는_삭제할_수_없다() {
        deleteBookmark(12).then().statusCode(403);
    }

    private static Map<String, Object> getBookmarks(Map<String, ?> query) {
        return authenticatedRequest()
                .queryParams(query)
                .when()
                .get("/api/v1/bookmarks")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .jsonPath()
                .getMap("$");
    }

    private static Map<String, Object> getBookmarkStatus(long articleId) {
        return authenticatedRequest()
                .when()
                .get("/api/v1/bookmarks/status/articles/{articleId}", articleId)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .jsonPath()
                .getMap("$");
    }

    private static Map<String, Object> getBookmarkNewsletterStatistics() {
        return authenticatedRequest()
                .when()
                .get("/api/v1/bookmarks/statistics/newsletters")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .jsonPath()
                .getMap("$");
    }

    private static MockMvcResponse postBookmark(long articleId) {
        return authenticatedRequest()
                .when()
                .post("/api/v1/bookmarks/articles/{articleId}", articleId);
    }

    private static MockMvcResponse deleteBookmark(long articleId) {
        return authenticatedRequest()
                .when()
                .delete("/api/v1/bookmarks/articles/{articleId}", articleId);
    }

    private static MockMvcRequestSpecification authenticatedRequest() {
        return RestAssuredMockMvc.given()
                .accept(ContentType.JSON)
                .header(AcceptanceTestHeaders.MEMBER_ID, MEMBER_ID);
    }

    private int countBookmarks(long memberId, long articleId) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from bookmark where member_id = ? and article_id = ?",
                Integer.class,
                memberId,
                articleId
        );
        return count == null ? 0 : count;
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> content(Map<String, Object> page) {
        return (List<Map<String, Object>>) page.get("content");
    }
}
