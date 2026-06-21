package me.bombom.api.v1.article.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.restassured.module.mockmvc.response.MockMvcResponse;
import io.restassured.module.mockmvc.specification.MockMvcRequestSpecification;
import java.util.List;
import java.util.Map;
import me.bombom.support.acceptance.AcceptanceTest;
import me.bombom.support.acceptance.ResetsAcceptanceData;
import me.bombom.support.acceptance.AcceptanceTestHeaders;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

@AcceptanceTest("acceptance/article/get-articles.json")
class ArticleControllerTest {

    private static final long MEMBER_ID = 1L;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("기본 아티클 목록 조회 성공")
    void 아티클_목록_조회_성공() {
        Map<String, Object> 조회결과 = 아티클_목록_조회(Map.of());

        assertSoftly(softly -> {
            softly.assertThat(조회결과.get("totalElements")).isEqualTo(11);
            softly.assertThat(조회결과.get("first")).isEqualTo(true);
            softly.assertThat(조회결과.get("last")).isEqualTo(false);
            softly.assertThat(조회결과.get("numberOfElements")).isEqualTo(10);
            softly.assertThat(조회결과.get("empty")).isEqualTo(false);
            softly.assertThat(목록(조회결과)).hasSize(10);
        });
    }

    @Test
    void 비즈레터_아티클_목록_조회() {
        Map<String, Object> 조회결과 = 아티클_목록_조회(Map.of("newsletterId", 3));

        assertSoftly(softly -> {
            softly.assertThat(조회결과.get("totalElements")).isEqualTo(2);
            softly.assertThat(목록(조회결과))
                    .extracting(아티클 -> 뉴스레터(아티클).get("name"))
                    .containsOnly("비즈레터");
        });
    }

    @Test
    void 우테코_아티클_목록_조회() {
        Map<String, Object> 조회결과 = 아티클_목록_조회(Map.of("newsletterId", 4));

        assertSoftly(softly -> {
            softly.assertThat(조회결과.get("totalElements")).isEqualTo(7);
            softly.assertThat(목록(조회결과))
                    .extracting(아티클 -> 뉴스레터(아티클).get("name"))
                    .containsOnly("우테코");
        });
    }

    @Test
    @DisplayName("검색 아티클 목록 조회 - 뉴스 키워드")
    void 뉴스_키워드_검색() {
        Map<String, Object> 조회결과 = 아티클_검색(Map.of("keyword", "뉴스"));

        assertSoftly(softly -> {
            softly.assertThat(조회결과.get("totalElements")).isEqualTo(2);
            softly.assertThat(목록(조회결과)).hasSize(2);
            softly.assertThat(목록(조회결과))
                    .extracting(아티클 -> 아티클.get("title"))
                    .containsOnly("뉴스");
        });
    }

    @Test
    @DisplayName("검색 아티클 목록 조회 - 레터 키워드")
    void 레터_키워드_검색() {
        Map<String, Object> 조회결과 = 아티클_검색(Map.of("keyword", "레터"));

        assertSoftly(softly -> {
            softly.assertThat(조회결과.get("totalElements")).isEqualTo(2);
            softly.assertThat(목록(조회결과)).hasSize(2);
            softly.assertThat(목록(조회결과))
                    .extracting(아티클 -> 아티클.get("title"))
                    .containsOnly("레터");
        });
    }

    @Test
    @DisplayName("존재하지 않는 키워드 검색 아티클 목록 조회")
    void 존재하지_않는_키워드_검색() {
        Map<String, Object> 조회결과 = 아티클_검색(Map.of("keyword", "존재하지않는키워드"));

        assertSoftly(softly -> {
            softly.assertThat(조회결과.get("totalElements")).isEqualTo(0);
            softly.assertThat(목록(조회결과)).isEmpty();
        });
    }

    @Test
    void 기본_정렬은_최신순이다() {
        Map<String, Object> 기본_조회결과 = 아티클_목록_조회(Map.of());
        Map<String, Object> 최신순_조회결과 = 아티클_목록_조회(Map.of("sorted", "desc"));

        assertSoftly(softly -> {
            softly.assertThat(기본_조회결과).isEqualTo(최신순_조회결과);
            softly.assertThat(목록(기본_조회결과).getFirst().get("arrivedDateTime"))
                    .isEqualTo("2025-07-15T09:55:00");
        });
    }

    @Test
    void 첫_페이지를_조회한다() {
        Map<String, Object> 조회결과 = 아티클_목록_조회(Map.of("page", 0, "size", 2));

        assertSoftly(softly -> {
            softly.assertThat(조회결과.get("totalElements")).isEqualTo(11);
            softly.assertThat(조회결과.get("totalPages")).isEqualTo(6);
            softly.assertThat(조회결과.get("number")).isEqualTo(0);
            softly.assertThat(조회결과.get("first")).isEqualTo(true);
            softly.assertThat(조회결과.get("last")).isEqualTo(false);
            softly.assertThat(목록(조회결과)).hasSize(2);
        });
    }

    @Test
    void 두_번째_페이지를_조회한다() {
        Map<String, Object> 조회결과 = 아티클_목록_조회(Map.of("page", 1, "size", 2));

        assertSoftly(softly -> {
            softly.assertThat(조회결과.get("totalElements")).isEqualTo(11);
            softly.assertThat(조회결과.get("totalPages")).isEqualTo(6);
            softly.assertThat(조회결과.get("number")).isEqualTo(1);
            softly.assertThat(조회결과.get("first")).isEqualTo(false);
            softly.assertThat(조회결과.get("last")).isEqualTo(false);
            softly.assertThat(목록(조회결과)).hasSize(2);
        });
    }

    @Test
    void 북마크_여부를_함께_반환한다() {
        Map<String, Object> 조회결과 = 아티클_목록_조회(Map.of("size", 2));

        assertSoftly(softly -> {
            softly.assertThat(목록(조회결과).get(0).get("isBookmarked")).isEqualTo(true);
            softly.assertThat(목록(조회결과).get(1).get("isBookmarked")).isEqualTo(false);
        });
    }

    @Test
    void 존재하지_않는_뉴스레터로_조회하면_404를_반환한다() {
        Map<String, Object> 오류응답 = 요청(
                "/api/v1/articles",
                Map.of("newsletterId", 999),
                true
        ).then()
                .statusCode(404)
                .contentType(ContentType.JSON)
                .extract()
                .jsonPath()
                .getMap("$");

        assertThat(오류응답.get("code")).isEqualTo("M003");
    }

    @Test
    void 검색_키워드가_없으면_400을_반환한다() {
        요청("/api/v1/articles/search", Map.of(), true)
                .then()
                .statusCode(400);
    }

    @Test
    void 검색_키워드가_공백이면_400을_반환한다() {
        요청("/api/v1/articles/search", Map.of("keyword", "   "), true)
                .then()
                .statusCode(400);
    }

    @Test
    void 검색_키워드가_한_글자면_400을_반환한다() {
        요청("/api/v1/articles/search", Map.of("keyword", "A"), true)
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("뉴스레터 키워드 복합 필터링 아티클 목록 조회")
    void 뉴스레터와_키워드로_복합_검색한다() {
        Map<String, Object> 조회결과 = 아티클_검색(Map.of(
                "newsletterId", 3,
                "keyword", "레터",
                "page", 0,
                "size", 10
        ));

        assertSoftly(softly -> {
            softly.assertThat(조회결과.get("totalElements")).isEqualTo(2);
            softly.assertThat(목록(조회결과))
                    .extracting(아티클 -> 아티클.get("title"))
                    .containsOnly("레터");
            softly.assertThat(목록(조회결과))
                    .extracting(아티클 -> 뉴스레터(아티클).get("name"))
                    .containsOnly("비즈레터");
        });
    }

    @Test
    @DisplayName("일반 목록 조회에서 keyword 파라미터는 무시됨")
    void 일반_목록_조회는_검색어를_무시한다() {
        Map<String, Object> 조회결과 = 아티클_목록_조회(Map.of("keyword", "아티클"));

        assertThat(조회결과.get("totalElements")).isEqualTo(11);
    }

    @Test
    void 인증되지_않은_사용자는_401을_반환한다() {
        Map<String, Object> 오류응답 = 요청("/api/v1/articles", Map.of(), false)
                .then()
                .statusCode(401)
                .contentType(ContentType.JSON)
                .extract()
                .jsonPath()
                .getMap("$");

        assertThat(오류응답.get("message")).isNotNull();
    }

    @Test
    void 아티클의_하이라이트_목록을_최신순으로_조회한다() {
        List<Map<String, Object>> 하이라이트 = 요청("/api/v1/articles/1/highlights", Map.of(), true)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .jsonPath()
                .getList("$");

        assertSoftly(softly -> {
            softly.assertThat(하이라이트).hasSize(2);
            softly.assertThat(하이라이트.get(0).get("text")).isEqualTo("두 번째 하이라이트");
            softly.assertThat(하이라이트.get(1).get("text")).isEqualTo("첫 번째 하이라이트");
            softly.assertThat(하이라이트.get(0).get("color")).isEqualTo("#4caf50");
            softly.assertThat(하이라이트.get(1).get("color")).isEqualTo("#ffeb3b");
        });
    }

    @Test
    void 아티클_상세를_조회한다() {
        Map<String, Object> 조회결과 = 성공_응답("/api/v1/articles/1", Map.of());

        assertSoftly(softly -> {
            softly.assertThat(조회결과.get("title")).isEqualTo("뉴스");
            softly.assertThat(조회결과.get("contents")).isEqualTo("<h1>아티클</h1>");
            softly.assertThat(뉴스레터(조회결과).get("name")).isEqualTo("뉴스픽");
        });
    }

    @Test
    void 존재하지_않는_아티클_상세는_404를_반환한다() {
        Map<String, Object> 오류응답 = 요청("/api/v1/articles/999", Map.of(), true)
                .then()
                .statusCode(404)
                .extract()
                .jsonPath()
                .getMap("$");

        assertThat(오류응답.get("code")).isEqualTo("M003");
    }

    @Test
    void 다른_사용자의_아티클_상세는_403을_반환한다() {
        요청("/api/v1/articles/12", Map.of(), true)
                .then()
                .statusCode(403);
    }

    @Test
    @ResetsAcceptanceData
    void 아티클을_읽으면_읽음_상태와_이력을_저장한다() {
        수정_요청("/api/v1/articles/1/read")
                .then()
                .statusCode(204);

        assertSoftly(softly -> {
            softly.assertThat(정수_조회("select is_read from article where id = 1")).isEqualTo(1);
            softly.assertThat(정수_조회("select count(*) from article_read_history where member_id = 1 and article_id = 1"))
                    .isEqualTo(1);
            softly.assertThat(정수_조회("select newsletter_id from article_read_history where member_id = 1 and article_id = 1"))
                    .isEqualTo(1);
            softly.assertThat(정수_조회("select category_id from article_read_history where member_id = 1 and article_id = 1"))
                    .isEqualTo(1);
        });
    }

    @Test
    @ResetsAcceptanceData
    void 이미_읽은_아티클은_읽음_이력을_중복_저장하지_않는다() {
        수정_요청("/api/v1/articles/1/read").then().statusCode(204);
        수정_요청("/api/v1/articles/1/read").then().statusCode(204);

        assertThat(정수_조회("select count(*) from article_read_history where member_id = 1 and article_id = 1"))
                .isEqualTo(1);
    }

    @Test
    void 존재하지_않는_아티클_읽음_요청은_404를_반환한다() {
        수정_요청("/api/v1/articles/999/read")
                .then()
                .statusCode(404);
    }

    @Test
    void 다른_사용자의_아티클_읽음_요청은_403을_반환한다() {
        수정_요청("/api/v1/articles/12/read")
                .then()
                .statusCode(403);
    }

    @Test
    void 뉴스레터별_아티클_통계를_조회한다() {
        Map<String, Object> 조회결과 = 성공_응답("/api/v1/articles/statistics/newsletters", Map.of());
        List<Map<String, Object>> 뉴스레터목록 = 통계_뉴스레터(조회결과);

        assertSoftly(softly -> {
            softly.assertThat(조회결과.get("totalCount")).isEqualTo(11);
            softly.assertThat(뉴스레터목록).extracting(뉴스레터 -> 뉴스레터.get("name"))
                    .containsExactly("우테코", "비즈레터", "뉴스픽", "IT타임즈");
            softly.assertThat(뉴스레터목록).extracting(뉴스레터 -> 뉴스레터.get("articleCount"))
                    .containsExactly(7, 2, 1, 1);
        });
    }

    @Test
    void 일치하지_않는_통계_검색은_빈_결과를_반환한다() {
        Map<String, Object> 조회결과 = 성공_응답(
                "/api/v1/articles/statistics/newsletters",
                Map.of("keyword", "존재하지않는키워드")
        );

        assertSoftly(softly -> {
            softly.assertThat(조회결과.get("totalCount")).isEqualTo(0);
            softly.assertThat(통계_뉴스레터(조회결과)).isEmpty();
        });
    }

    @Test
    @ResetsAcceptanceData
    void 아티클을_삭제하면_연관_북마크를_삭제하고_하이라이트를_보존한다() {
        삭제_요청(List.of(1, 2, 1))
                .then()
                .statusCode(204);

        assertSoftly(softly -> {
            softly.assertThat(정수_조회("select count(*) from article where id in (1, 2)")).isZero();
            softly.assertThat(정수_조회("select count(*) from bookmark where article_id in (1, 2)")).isZero();
            softly.assertThat(정수_조회("select count(*) from highlight where article_id = 0")).isEqualTo(3);
        });
    }

    @Test
    void 다른_사용자의_아티클이_포함된_삭제는_전체를_거부한다() {
        삭제_요청(List.of(1, 12))
                .then()
                .statusCode(403);

        assertThat(정수_조회("select count(*) from article where id in (1, 12)"))
                .isEqualTo(2);
    }

    private static Map<String, Object> 아티클_목록_조회(Map<String, ?> 쿼리) {
        return 성공_응답("/api/v1/articles", 쿼리);
    }

    private static Map<String, Object> 아티클_검색(Map<String, ?> 쿼리) {
        return 성공_응답("/api/v1/articles/search", 쿼리);
    }

    private static Map<String, Object> 성공_응답(String 경로, Map<String, ?> 쿼리) {
        return 요청(경로, 쿼리, true)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .jsonPath()
                .getMap("$");
    }

    private static MockMvcResponse 요청(String 경로, Map<String, ?> 쿼리, boolean 인증) {
        MockMvcRequestSpecification 요청 = RestAssuredMockMvc.given()
                .accept(ContentType.JSON)
                .queryParams(쿼리);
        if (인증) {
            요청.header(AcceptanceTestHeaders.MEMBER_ID, MEMBER_ID);
        }
        return 요청.when().get(경로);
    }

    private static MockMvcResponse 수정_요청(String 경로) {
        return 인증_요청().when().patch(경로);
    }

    private static MockMvcResponse 삭제_요청(List<Integer> 아티클아이디) {
        return 인증_요청()
                .contentType(ContentType.JSON)
                .body(Map.of("articleIds", 아티클아이디))
                .when()
                .post("/api/v1/articles/delete");
    }

    private static MockMvcRequestSpecification 인증_요청() {
        return RestAssuredMockMvc.given()
                .accept(ContentType.JSON)
                .header(AcceptanceTestHeaders.MEMBER_ID, MEMBER_ID);
    }

    private int 정수_조회(String sql) {
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> 목록(Map<String, Object> 페이지) {
        return (List<Map<String, Object>>) 페이지.get("content");
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> 통계_뉴스레터(Map<String, Object> 통계) {
        return (List<Map<String, Object>>) 통계.get("newsletters");
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> 뉴스레터(Map<String, Object> 아티클) {
        return (Map<String, Object>) 아티클.get("newsletter");
    }
}
