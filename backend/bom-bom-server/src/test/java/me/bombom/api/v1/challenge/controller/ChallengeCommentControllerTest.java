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
import me.bombom.support.acceptance.ResetsAcceptanceData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

@AcceptanceTest("acceptance/challenge/comment.json")
class ChallengeCommentControllerTest {

    private static final long MEMBER_ID = 1L;
    private static final LocalDate TODAY = LocalDate.of(2026, 1, 9);

    @Autowired
    private MutableClock clock;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        clock.setDate(TODAY);
    }

    @Test
    void 챌린지_팀_댓글을_기간으로_필터링해_조회한다() {
        Map<String, Object> result = getComments(Map.of(
                "start", TODAY.minusDays(1).toString(),
                "end", TODAY.plusDays(1).toString()
        ));

        assertSoftly(softly -> {
            softly.assertThat(content(result).getFirst().get("comment")).isEqualTo("comment");
            softly.assertThat(result.get("totalElements")).isEqualTo(1);
        });
    }

    @Test
    void id가_1_미만이면_400을_응답한다() {
        authenticatedRequest()
                .queryParams(Map.of("start", TODAY.toString(), "end", TODAY.toString()))
                .when()
                .get("/api/v1/challenges/{challengeId}/comments", 0)
                .then()
                .statusCode(400);
    }

    @Test
    void 챌린지_코멘트_후보_아티클을_조회한다() {
        List<Map<String, Object>> result = getCandidateArticles(TODAY);

        assertSoftly(softly -> {
            softly.assertThat(result).isNotEmpty();
            softly.assertThat(result.getFirst().get("articleId")).isEqualTo(2);
            softly.assertThat(result.getFirst().get("newsletterName")).isEqualTo("뉴스픽");
            softly.assertThat(result.getFirst().get("articleTitle")).isEqualTo("하이라이트 아티클");
        });
    }

    @Test
    @ResetsAcceptanceData
    void 챌린지_코멘트를_생성한다() {
        createComment(1, "챌린지 한 줄 코멘트로 20자 이상의 댓글을 작성했습니다.")
                .then()
                .statusCode(201);

        assertThat(countComments()).isEqualTo(2);
    }

    @Test
    void 코멘트가_20자_미만이면_400을_응답한다() {
        createComment(1, "너무 짧은 댓글")
                .then()
                .statusCode(400);
    }

    @Test
    void 하이라이트가_8퍼센트를_넘으면_잘라서_응답한다() {
        Map<String, Object> result = getArticleHighlights(2);

        assertThat(content(result).getFirst().get("text")).isEqualTo("ABCDEFGH...");
    }

    @Test
    @ResetsAcceptanceData
    void 챌린지_코멘트를_수정한다() {
        updateComment(1, "수정된 챌린지 한 줄 코멘트를 20자 이상 작성합니다.")
                .then()
                .statusCode(204);

        assertThat(findComment(1)).isEqualTo("수정된 챌린지 한 줄 코멘트를 20자 이상 작성합니다.");
    }

    @Test
    @ResetsAcceptanceData
    void 챌린지_코멘트에_좋아요를_추가하면_집계된다() {
        Map<String, Object> result = addLike(1, 1)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .jsonPath()
                .getMap("$");

        assertSoftly(softly -> {
            softly.assertThat(result.get("likeCount")).isEqualTo(1);
            softly.assertThat(findLikeCount(1)).isEqualTo(1);
            softly.assertThat(countLikes()).isEqualTo(1);
        });
    }

    @Test
    @ResetsAcceptanceData
    void 같은_참가자가_중복으로_좋아요를_눌러도_한번만_집계된다() {
        addLike(1, 1)
                .then()
                .statusCode(200);

        Map<String, Object> result = addLike(1, 1)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .jsonPath()
                .getMap("$");

        assertSoftly(softly -> {
            softly.assertThat(result.get("likeCount")).isEqualTo(1);
            softly.assertThat(findLikeCount(1)).isEqualTo(1);
            softly.assertThat(countLikes()).isEqualTo(1);
        });
    }

    @Test
    @ResetsAcceptanceData
    void 챌린지_코멘트_좋아요를_삭제하면_집계가_감소한다() {
        addLike(1, 1)
                .then()
                .statusCode(200);

        Map<String, Object> result = deleteLike(1, 1)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .jsonPath()
                .getMap("$");

        assertSoftly(softly -> {
            softly.assertThat(result.get("likeCount")).isEqualTo(0);
            softly.assertThat(findLikeCount(1)).isZero();
            softly.assertThat(countLikes()).isZero();
        });
    }

    @Test
    void 좋아요가_없을_때_삭제해도_집계는_변하지_않는다() {
        Map<String, Object> result = deleteLike(1, 1)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .jsonPath()
                .getMap("$");

        assertSoftly(softly -> {
            softly.assertThat(result.get("likeCount")).isEqualTo(0);
            softly.assertThat(findLikeCount(1)).isZero();
            softly.assertThat(countLikes()).isZero();
        });
    }

    @Test
    void 챌린지_코멘트가_20자_미만이면_수정에_실패한다() {
        updateComment(1, "짧은 코멘트")
                .then()
                .statusCode(400);
    }

    @Test
    void 챌린지_ID가_1_미만이면_좋아요_추가에_실패한다() {
        addLike(0, 1)
                .then()
                .statusCode(400);
    }

    @Test
    void 코멘트_ID가_1_미만이면_좋아요_추가에_실패한다() {
        addLike(1, 0)
                .then()
                .statusCode(400);
    }

    @Test
    void 챌린지_ID가_1_미만이면_좋아요_삭제에_실패한다() {
        deleteLike(0, 1)
                .then()
                .statusCode(400);
    }

    @Test
    void 코멘트_ID가_1_미만이면_좋아요_삭제에_실패한다() {
        deleteLike(1, 0)
                .then()
                .statusCode(400);
    }

    private static Map<String, Object> getComments(Map<String, ?> query) {
        return authenticatedRequest()
                .queryParams(query)
                .when()
                .get("/api/v1/challenges/{challengeId}/comments", 1)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .jsonPath()
                .getMap("$");
    }

    private static List<Map<String, Object>> getCandidateArticles(LocalDate date) {
        return authenticatedRequest()
                .queryParam("date", date.toString())
                .when()
                .get("/api/v1/challenges/comments/articles/candidates")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .jsonPath()
                .getList("$");
    }

    private static Map<String, Object> getArticleHighlights(long articleId) {
        return authenticatedRequest()
                .when()
                .get("/api/v1/challenges/comments/articles/{articleId}/highlights", articleId)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .jsonPath()
                .getMap("$");
    }

    private static Response createComment(long articleId, String comment) {
        return authenticatedRequest()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "articleId", articleId,
                        "quotation", "quote",
                        "comment", comment
                ))
                .when()
                .post("/api/v1/challenges/{challengeId}/comments", 1);
    }

    private static Response updateComment(long commentId, String comment) {
        return authenticatedRequest()
                .contentType(ContentType.JSON)
                .body(Map.of("comment", comment))
                .when()
                .patch("/api/v1/challenges/{challengeId}/comments/{commentId}", 1, commentId);
    }

    private static Response addLike(long challengeId, long commentId) {
        return authenticatedRequest()
                .when()
                .put("/api/v1/challenges/{challengeId}/comments/{commentId}/like", challengeId, commentId);
    }

    private static Response deleteLike(long challengeId, long commentId) {
        return authenticatedRequest()
                .when()
                .delete("/api/v1/challenges/{challengeId}/comments/{commentId}/like", challengeId, commentId);
    }

    private static RequestSpecification authenticatedRequest() {
        return RestAssured.given()
                .accept(ContentType.JSON)
                .header(AcceptanceTestHeaders.MEMBER_ID, MEMBER_ID);
    }

    private int countComments() {
        Integer count = jdbcTemplate.queryForObject("select count(*) from challenge_comment", Integer.class);
        return count == null ? 0 : count;
    }

    private String findComment(long commentId) {
        return jdbcTemplate.queryForObject(
                "select comment from challenge_comment where id = ?",
                String.class,
                commentId
        );
    }

    private int findLikeCount(long commentId) {
        Integer count = jdbcTemplate.queryForObject(
                "select like_count from challenge_comment where id = ?",
                Integer.class,
                commentId
        );
        return count == null ? 0 : count;
    }

    private int countLikes() {
        Integer count = jdbcTemplate.queryForObject("select count(*) from challenge_comment_like", Integer.class);
        return count == null ? 0 : count;
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> content(Map<String, Object> page) {
        return (List<Map<String, Object>>) page.get("content");
    }
}
