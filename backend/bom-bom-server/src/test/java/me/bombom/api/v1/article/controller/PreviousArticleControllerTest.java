package me.bombom.api.v1.article.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.restassured.module.mockmvc.specification.MockMvcRequestSpecification;
import java.util.List;
import java.util.Map;
import me.bombom.support.acceptance.AcceptanceTest;
import me.bombom.support.acceptance.AcceptanceTestHeaders;
import org.junit.jupiter.api.Test;

@AcceptanceTest("acceptance/article/get-previous-articles.json")
class PreviousArticleControllerTest {

    private static final long SUBSCRIBED_MEMBER_ID = 1L;
    private static final long NOT_SUBSCRIBED_MEMBER_ID = 2L;

    @Test
    void 정책이_없으면_빈_목록을_반환한다() {
        List<Map<String, Object>> result = 지난아티클목록조회(Map.of("newsletterId", 1, "limit", 5));

        assertThat(result).isEmpty();
    }

    @Test
    void RECENT_ONLY_정책이면_최신_아티클을_제외하고_최근_아티클을_반환한다() {
        List<Map<String, Object>> result = 지난아티클목록조회(Map.of("newsletterId", 2, "limit", 3));

        assertSoftly(softly -> {
            softly.assertThat(result).hasSize(3);
            softly.assertThat(result).extracting(article -> article.get("articleId"))
                    .containsExactly(202, 203, 204);
        });
    }

    @Test
    void INACTIVE_정책이면_빈_목록을_반환한다() {
        List<Map<String, Object>> result = 지난아티클목록조회(Map.of("newsletterId", 3, "limit", 5));

        assertThat(result).isEmpty();
    }

    @Test
    void FIXED_ONLY_정책이면_고정_아티클만_반환한다() {
        List<Map<String, Object>> result = 지난아티클목록조회(Map.of("newsletterId", 4, "limit", 3));

        assertSoftly(softly -> {
            softly.assertThat(result).hasSize(3);
            softly.assertThat(result).extracting(article -> article.get("articleId"))
                    .containsExactly(401, 402, 403);
        });
    }

    @Test
    void FIXED_WITH_RECENT_정책이면_고정_아티클과_최근_아티클을_함께_반환한다() {
        List<Map<String, Object>> result = 지난아티클목록조회(Map.of("newsletterId", 5, "limit", 5));

        assertSoftly(softly -> {
            softly.assertThat(result).hasSize(5);
            softly.assertThat(result).extracting(article -> article.get("articleId"))
                    .containsExactly(501, 502, 512, 513, 514);
        });
    }

    @Test
    void 설정한_고정_아티클_개수보다_실제_고정_아티클이_적으면_실제_개수만큼_반환한다() {
        List<Map<String, Object>> result = 지난아티클목록조회(Map.of("newsletterId", 6, "limit", 5));

        assertSoftly(softly -> {
            softly.assertThat(result).hasSize(3);
            softly.assertThat(result).extracting(article -> article.get("articleId"))
                    .containsExactly(601, 602, 603);
        });
    }

    @Test
    void 구독중인_로그인_사용자가_지난_아티클_상세를_조회한다() {
        Map<String, Object> result = 지난아티클상세조회(701, SUBSCRIBED_MEMBER_ID);

        assertSoftly(softly -> {
            softly.assertThat(result.get("title")).isEqualTo("상세 조회 아티클");
            softly.assertThat(result.get("isSubscribed")).isEqualTo(true);
            softly.assertThat(result.get("exposureRatio")).isEqualTo(80);
            softly.assertThat(뉴스레터(result).get("name")).isEqualTo("상세조회뉴스레터");
        });
    }

    @Test
    void 구독하지_않은_로그인_사용자가_지난_아티클_상세를_조회한다() {
        Map<String, Object> result = 지난아티클상세조회(701, NOT_SUBSCRIBED_MEMBER_ID);

        assertSoftly(softly -> {
            softly.assertThat(result.get("title")).isEqualTo("상세 조회 아티클");
            softly.assertThat(result.get("isSubscribed")).isEqualTo(false);
        });
    }

    @Test
    void 비로그인_사용자가_지난_아티클_상세를_조회한다() {
        Map<String, Object> result = 비로그인_지난아티클상세조회(701);

        assertSoftly(softly -> {
            softly.assertThat(result.get("title")).isEqualTo("상세 조회 아티클");
            softly.assertThat(result.get("isSubscribed")).isEqualTo(false);
        });
    }

    @Test
    void 존재하지_않는_지난_아티클_상세는_404를_반환한다() {
        인증_요청(SUBSCRIBED_MEMBER_ID)
                .when()
                .get("/api/v1/articles/previous/99999")
                .then()
                .statusCode(404);
    }

    @Test
    void INACTIVE_정책인_지난_아티클_상세는_404를_반환한다() {
        인증_요청(SUBSCRIBED_MEMBER_ID)
                .when()
                .get("/api/v1/articles/previous/801")
                .then()
                .statusCode(404);
    }

    private static List<Map<String, Object>> 지난아티클목록조회(Map<String, ?> query) {
        return RestAssuredMockMvc.given()
                .accept(ContentType.JSON)
                .queryParams(query)
                .when()
                .get("/api/v1/articles/previous")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .jsonPath()
                .getList("$");
    }

    private static Map<String, Object> 지난아티클상세조회(long articleId, long memberId) {
        return 인증_요청(memberId)
                .when()
                .get("/api/v1/articles/previous/{articleId}", articleId)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .jsonPath()
                .getMap("$");
    }

    private static Map<String, Object> 비로그인_지난아티클상세조회(long articleId) {
        return RestAssuredMockMvc.given()
                .accept(ContentType.JSON)
                .when()
                .get("/api/v1/articles/previous/{articleId}", articleId)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .jsonPath()
                .getMap("$");
    }

    private static MockMvcRequestSpecification 인증_요청(long memberId) {
        return RestAssuredMockMvc.given()
                .accept(ContentType.JSON)
                .header(AcceptanceTestHeaders.MEMBER_ID, memberId);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> 뉴스레터(Map<String, Object> article) {
        return (Map<String, Object>) article.get("newsletter");
    }
}
