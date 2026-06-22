package me.bombom.api.v1.challenge.controller;

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

@AcceptanceTest("acceptance/challenge/comment.json")
class ChallengeCommentReplyControllerTest {

    private static final long MEMBER_ID = 1L;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void commentId가_1_미만이면_400을_응답한다() {
        createCommentReply(1, 0, "감사합니다!", false)
                .then()
                .statusCode(400);
    }

    @Test
    void 답글_내용이_null이면_400을_응답한다() {
        authenticatedRequest()
                .contentType(ContentType.JSON)
                .body(Map.of("isPrivate", false))
                .when()
                .post("/api/v1/challenges/{challengeId}/comments/{commentId}/replies", 1, 1)
                .then()
                .statusCode(400);
    }

    @Test
    void 답글_내용이_500자를_초과하면_400을_응답한다() {
        createCommentReply(1, 1, "a".repeat(501), false)
                .then()
                .statusCode(400);
    }

    @Test
    @ResetsAcceptanceData
    void 코멘트_답글을_생성한다() {
        createCommentReply(1, 1, "새로운 답글입니다.", false)
                .then()
                .statusCode(201);

        assertSoftly(softly -> {
            softly.assertThat(countCommentReplies()).isEqualTo(2);
            softly.assertThat(findReplyCount(1)).isEqualTo(2);
        });
    }

    @Test
    void 코멘트_답글_목록을_조회하면_페이지정보를_반환한다() {
        Map<String, Object> result = getCommentReplies(1, 1);

        assertSoftly(softly -> {
            softly.assertThat(content(result)).hasSize(1);
            softly.assertThat(content(result).getFirst().get("reply")).isEqualTo("첫번째 답글");
            softly.assertThat(content(result).getFirst().get("isMyReply")).isEqualTo(true);
            softly.assertThat(result.get("totalElements")).isEqualTo(1);
        });
    }

    @Test
    void 코멘트ID가_1미만이면_답글_조회시_400을_응답한다() {
        authenticatedRequest()
                .when()
                .get("/api/v1/challenges/{challengeId}/comments/{commentId}/replies", 1, 0)
                .then()
                .statusCode(400);
    }

    private static Map<String, Object> getCommentReplies(long challengeId, long commentId) {
        return authenticatedRequest()
                .queryParam("size", 10)
                .when()
                .get("/api/v1/challenges/{challengeId}/comments/{commentId}/replies", challengeId, commentId)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .jsonPath()
                .getMap("$");
    }

    private static MockMvcResponse createCommentReply(
            long challengeId,
            long commentId,
            String reply,
            boolean isPrivate
    ) {
        return authenticatedRequest()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "reply", reply,
                        "isPrivate", isPrivate
                ))
                .when()
                .post("/api/v1/challenges/{challengeId}/comments/{commentId}/replies", challengeId, commentId);
    }

    private static MockMvcRequestSpecification authenticatedRequest() {
        return RestAssuredMockMvc.given()
                .accept(ContentType.JSON)
                .header(AcceptanceTestHeaders.MEMBER_ID, MEMBER_ID);
    }

    private int countCommentReplies() {
        Integer count = jdbcTemplate.queryForObject("select count(*) from challenge_comment_reply", Integer.class);
        return count == null ? 0 : count;
    }

    private int findReplyCount(long commentId) {
        Integer count = jdbcTemplate.queryForObject(
                "select reply_count from challenge_comment where id = ?",
                Integer.class,
                commentId
        );
        return count == null ? 0 : count;
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> content(Map<String, Object> page) {
        return (List<Map<String, Object>>) page.get("content");
    }
}
