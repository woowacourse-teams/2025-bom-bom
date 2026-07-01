package me.bombom.api.v1.nativenewsletter.maeilmail.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailUserAnswer;
import me.bombom.api.v1.nativenewsletter.maeilmail.repository.MaeilMailUserAnswerRepository;
import me.bombom.support.acceptance.AcceptanceTest;
import me.bombom.support.acceptance.AcceptanceTestHeaders;
import me.bombom.support.acceptance.ResetsAcceptanceData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@AcceptanceTest("acceptance/maeilmail/maeil-mail-content.json")
class MaeilMailControllerTest {

    private static final Long MEMBER_ID = 1L;
    private static final Long CONTENT_ID = 1L;
    private static final Long CONTENT_WITHOUT_ANSWER_ID = 2L;
    private static final Long ISSUE_HISTORY_ID = 1L;
    private static final Long SECOND_ISSUE_HISTORY_ID = 2L;
    private static final Long ARTICLE_ID = 10_001L;
    private static final Long SECOND_ARTICLE_ID = 10_002L;
    private static final Long UNKNOWN_ARTICLE_ID = 99_999L;

    @Autowired
    private MaeilMailUserAnswerRepository userAnswerRepository;

    @Test
    void 아티클_ID로_매일메일_콘텐츠_정보를_조회한다() {
        Map<String, Object> response = authenticatedRequest()
                .queryParam("articleId", ARTICLE_ID)
                .when()
                .get("/api/v1/maeil-mail/content")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .jsonPath()
                .getMap("$");

        assertThat(longValue(response, "contentId")).isEqualTo(CONTENT_ID);
    }

    @Test
    void 매일메일_콘텐츠의_모범_답변을_조회한다() {
        Map<String, Object> response = authenticatedRequest()
                .when()
                .get("/api/v1/maeil-mail/{contentId}/answer", CONTENT_ID)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .jsonPath()
                .getMap("$");

        assertSoftly(softly -> {
            softly.assertThat(response.get("title")).isEqualTo("Java의 GC 동작 방식은?");
            softly.assertThat(response.get("answer")).isEqualTo("<p>GC는 더 이상 참조되지 않는 객체를 정리합니다.</p>");
        });
    }

    @Test
    void 모범_답변이_없는_컨텐츠는_404를_반환한다() {
        authenticatedRequest()
                .when()
                .get("/api/v1/maeil-mail/{contentId}/answer", CONTENT_WITHOUT_ANSWER_ID)
                .then()
                .statusCode(404);
    }

    @Test
    @ResetsAcceptanceData
    void 아티클_ID로_사용자_답변을_제출하고_다시_조회한다() {
        String answer = "GC Root에서 도달할 수 없는 객체를 수거한다.";

        submitAnswer(ARTICLE_ID, answer)
                .then()
                .statusCode(201);

        MaeilMailUserAnswer savedAnswer = userAnswerRepository.findAll().getFirst();
        assertSoftly(softly -> {
            softly.assertThat(savedAnswer.getMemberId()).isEqualTo(MEMBER_ID);
            softly.assertThat(savedAnswer.getIssueHistoryId()).isEqualTo(ISSUE_HISTORY_ID);
            softly.assertThat(savedAnswer.getAnswer()).isEqualTo(answer);
        });

        Map<String, Object> response = getSubmittedAnswer(ARTICLE_ID, 200);

        assertThat(response.get("answer")).isEqualTo(answer);
    }

    @Test
    @ResetsAcceptanceData
    void 같은_콘텐츠라도_발행_아티클이_다르면_각각_답변을_제출하고_조회한다() {
        String firstAnswer = "첫 번째 발행 아티클에 대한 답변";
        String secondAnswer = "두 번째 발행 아티클에 대한 답변";

        submitAnswer(ARTICLE_ID, firstAnswer)
                .then()
                .statusCode(201);
        submitAnswer(SECOND_ARTICLE_ID, secondAnswer)
                .then()
                .statusCode(201);

        Map<String, Object> firstResponse = getSubmittedAnswer(ARTICLE_ID, 200);
        Map<String, Object> secondResponse = getSubmittedAnswer(SECOND_ARTICLE_ID, 200);
        MaeilMailUserAnswer savedFirstAnswer = userAnswerRepository
                .findByMemberIdAndIssueHistoryId(MEMBER_ID, ISSUE_HISTORY_ID)
                .orElseThrow();
        MaeilMailUserAnswer savedSecondAnswer = userAnswerRepository
                .findByMemberIdAndIssueHistoryId(MEMBER_ID, SECOND_ISSUE_HISTORY_ID)
                .orElseThrow();

        assertSoftly(softly -> {
            softly.assertThat(firstResponse.get("answer")).isEqualTo(firstAnswer);
            softly.assertThat(secondResponse.get("answer")).isEqualTo(secondAnswer);
            softly.assertThat(savedFirstAnswer.getAnswer()).isEqualTo(firstAnswer);
            softly.assertThat(savedSecondAnswer.getAnswer()).isEqualTo(secondAnswer);
            softly.assertThat(userAnswerRepository.findAll()).hasSize(2);
        });
    }

    @Test
    @ResetsAcceptanceData
    void 답변이_1500자면_제출할_수_있다() {
        String answer = "가".repeat(1_500);

        submitAnswer(ARTICLE_ID, answer)
                .then()
                .statusCode(201);

        MaeilMailUserAnswer savedAnswer = userAnswerRepository.findAll().getFirst();
        assertSoftly(softly -> {
            softly.assertThat(savedAnswer.getIssueHistoryId()).isEqualTo(ISSUE_HISTORY_ID);
            softly.assertThat(savedAnswer.getAnswer()).isEqualTo(answer);
        });
    }

    @Test
    void 존재하지_않는_아티클에_답변을_제출하면_404를_반환한다() {
        Map<String, Object> response = submitAnswer(UNKNOWN_ARTICLE_ID, "존재하지 않는 아티클에는 저장되지 않는다.")
                .then()
                .statusCode(404)
                .contentType(ContentType.JSON)
                .extract()
                .jsonPath()
                .getMap("$");

        assertThat(response.get("code")).isEqualTo(ErrorDetail.ENTITY_NOT_FOUND.getCode());
        assertThat(userAnswerRepository.findAll()).isEmpty();
    }

    @Test
    void 기존_콘텐츠_ID_기반_답변_제출_URL은_더_이상_사용하지_않는다() {
        authenticatedRequest()
                .contentType(ContentType.JSON)
                .body(Map.of("answer", "기존 URL로는 저장되지 않는다."))
                .when()
                .post("/api/v1/maeil-mail/{contentId}/answer/me", CONTENT_ID)
                .then()
                .statusCode(404);

        assertThat(userAnswerRepository.findAll()).isEmpty();
    }

    @Test
    void 답변이_공백이면_제출할_수_없다() {
        Map<String, Object> response = submitAnswer(ARTICLE_ID, " ")
                .then()
                .statusCode(400)
                .contentType(ContentType.JSON)
                .extract()
                .jsonPath()
                .getMap("$");

        assertThat(response.get("code")).isEqualTo(ErrorDetail.INVALID_REQUEST_BODY_VALIDATION.getCode());
        assertThat(userAnswerRepository.findAll()).isEmpty();
    }

    @Test
    void 답변이_1500자를_초과하면_제출할_수_없다() {
        Map<String, Object> response = submitAnswer(ARTICLE_ID, "가".repeat(1_501))
                .then()
                .statusCode(400)
                .contentType(ContentType.JSON)
                .extract()
                .jsonPath()
                .getMap("$");

        assertThat(response.get("code")).isEqualTo(ErrorDetail.INVALID_REQUEST_BODY_VALIDATION.getCode());
        assertThat(userAnswerRepository.findAll()).isEmpty();
    }

    @Test
    void 아직_제출하지_않은_답변을_조회하면_404를_반환한다() {
        Map<String, Object> response = getSubmittedAnswer(ARTICLE_ID, 404);

        assertThat(response.get("code")).isEqualTo(ErrorDetail.ENTITY_NOT_FOUND.getCode());
    }

    @Test
    void 존재하지_않는_아티클의_제출_답변을_조회하면_404를_반환한다() {
        Map<String, Object> response = getSubmittedAnswer(UNKNOWN_ARTICLE_ID, 404);

        assertThat(response.get("code")).isEqualTo(ErrorDetail.ENTITY_NOT_FOUND.getCode());
    }

    private io.restassured.response.Response submitAnswer(Long articleId, String answer) {
        return authenticatedRequest()
                .contentType(ContentType.JSON)
                .body(Map.of("answer", answer))
                .when()
                .post("/api/v1/maeil-mail/articles/{articleId}/answers/me", articleId);
    }

    private Map<String, Object> getSubmittedAnswer(
            Long articleId,
            int statusCode
    ) {
        return authenticatedRequest()
                .when()
                .get("/api/v1/maeil-mail/articles/{articleId}/answers/me", articleId)
                .then()
                .statusCode(statusCode)
                .contentType(ContentType.JSON)
                .extract()
                .jsonPath()
                .getMap("$");
    }

    private io.restassured.specification.RequestSpecification authenticatedRequest() {
        return RestAssured.given()
                .accept(ContentType.JSON)
                .header(AcceptanceTestHeaders.MEMBER_ID, MEMBER_ID);
    }

    private static long longValue(Map<String, Object> response, String key) {
        return ((Number) response.get(key)).longValue();
    }
}
