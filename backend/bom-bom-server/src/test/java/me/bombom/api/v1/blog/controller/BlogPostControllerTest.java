package me.bombom.api.v1.blog.controller;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.List;
import java.util.Map;
import me.bombom.support.acceptance.AcceptanceTest;
import me.bombom.support.acceptance.AcceptanceTestHeaders;
import me.bombom.support.acceptance.AdditionalAcceptanceDataSet;
import org.junit.jupiter.api.Test;

@AcceptanceTest("acceptance/blog/blog-post.json")
class BlogPostControllerTest {

    private static final long ADMIN_MEMBER_ID = 102L;
    private static final long PUBLIC_POST_ID = 1L;
    private static final long PRIVATE_POST_ID = 2L;
    private static final long DRAFT_POST_ID = 3L;

    @Test
    void 익명_사용자는_공개된_블로그_목록만_조회한다() {
        Map<String, Object> result = getBlogPosts(null, null, null);

        assertSoftly(softly -> {
            softly.assertThat(result.get("totalElements")).isEqualTo(1);
            softly.assertThat(content(result).getFirst().get("title")).isEqualTo("공개 글");
            softly.assertThat(content(result).getFirst().get("thumbnailImageUrl"))
                    .isEqualTo("https://cdn.bombom.me/public.png");
        });
    }

    @Test
    void 관리자는_비공개_블로그를_목록에서_조회한다() {
        Map<String, Object> result = getBlogPosts(ADMIN_MEMBER_ID, null, null);

        assertSoftly(softly -> {
            softly.assertThat(result.get("totalElements")).isEqualTo(2);
            softly.assertThat(content(result).get(0).get("title")).isEqualTo("공개 글");
            softly.assertThat(content(result).get(1).get("title")).isEqualTo("비공개 글");
        });
    }

    @Test
    @AdditionalAcceptanceDataSet("acceptance/blog/paging.json")
    void 블로그_목록에_페이징을_적용한다() {
        Map<String, Object> result = getBlogPosts(null, 0, 1);

        assertSoftly(softly -> {
            softly.assertThat(result.get("totalElements")).isEqualTo(2);
            softly.assertThat(result.get("totalPages")).isEqualTo(2);
            softly.assertThat(content(result)).hasSize(1);
        });
    }

    @Test
    void 익명_사용자가_공개_블로그_상세를_조회한다() {
        Map<String, Object> result = getBlogPost(PUBLIC_POST_ID, null);

        assertSoftly(softly -> {
            softly.assertThat(result.get("title")).isEqualTo("공개 글");
            softly.assertThat(result.get("content")).isEqualTo("공개 글 본문");
            softly.assertThat(result.get("categoryName")).isEqualTo("테크");
            softly.assertThat(hashTags(result)).hasSize(2);
        });
    }

    @Test
    void 익명_사용자는_비공개_블로그_상세를_조회할_수_없다() {
        RestAssured.given()
                .accept(ContentType.JSON)
                .when()
                .get("/api/v1/blog/posts/{postId}", PRIVATE_POST_ID)
                .then()
                .statusCode(403);
    }

    @Test
    void 관리자는_비공개_블로그_상세를_조회한다() {
        Map<String, Object> result = getBlogPost(PRIVATE_POST_ID, ADMIN_MEMBER_ID);

        assertSoftly(softly -> softly.assertThat(result.get("title")).isEqualTo("비공개 글"));
    }

    @Test
    void 발행되지_않은_블로그_상세는_404를_반환한다() {
        RestAssured.given()
                .accept(ContentType.JSON)
                .header(AcceptanceTestHeaders.MEMBER_ID, ADMIN_MEMBER_ID)
                .when()
                .get("/api/v1/blog/posts/{postId}", DRAFT_POST_ID)
                .then()
                .statusCode(404);
    }

    @Test
    @AdditionalAcceptanceDataSet("acceptance/blog/additional-category.json")
    void 블로그_카테고리_목록을_조회한다() {
        List<Map<String, Object>> result = RestAssured.given()
                .accept(ContentType.JSON)
                .when()
                .get("/api/v1/blog/categories")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .jsonPath()
                .getList("$");

        assertSoftly(softly -> {
            softly.assertThat(result.get(0).get("categoryName")).isEqualTo("테크");
            softly.assertThat(result.get(1).get("categoryName")).isEqualTo("라이프");
            softly.assertThat(result.get(1).get("id")).isEqualTo(2);
        });
    }

    private static Map<String, Object> getBlogPosts(Long memberId, Integer page, Integer size) {
        var request = RestAssured.given()
                .accept(ContentType.JSON);
        if (memberId != null) {
            request.header(AcceptanceTestHeaders.MEMBER_ID, memberId);
        }
        if (page != null) {
            request.queryParam("page", page);
        }
        if (size != null) {
            request.queryParam("size", size);
        }
        return request.when()
                .get("/api/v1/blog/posts")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .jsonPath()
                .getMap("$");
    }

    private static Map<String, Object> getBlogPost(long postId, Long memberId) {
        var request = RestAssured.given()
                .accept(ContentType.JSON);
        if (memberId != null) {
            request.header(AcceptanceTestHeaders.MEMBER_ID, memberId);
        }
        return request.when()
                .get("/api/v1/blog/posts/{postId}", postId)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .jsonPath()
                .getMap("$");
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> content(Map<String, Object> page) {
        return (List<Map<String, Object>>) page.get("content");
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> hashTags(Map<String, Object> blogPost) {
        return (List<Map<String, Object>>) blogPost.get("hashTags");
    }
}
