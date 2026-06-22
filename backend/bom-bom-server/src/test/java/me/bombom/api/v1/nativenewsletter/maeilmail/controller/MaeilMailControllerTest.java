package me.bombom.api.v1.nativenewsletter.maeilmail.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.common.exception.ErrorResponse;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailContent;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailContentAnswer;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailIssueHistory;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailUserAnswer;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.MaeilMailIdealAnswerResponse;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.MaeilMailInformationResponse;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.MaeilMailSubmittedAnswerResponse;
import me.bombom.api.v1.nativenewsletter.maeilmail.repository.MaeilMailContentAnswerRepository;
import me.bombom.api.v1.nativenewsletter.maeilmail.repository.MaeilMailContentRepository;
import me.bombom.api.v1.nativenewsletter.maeilmail.repository.MaeilMailIssueHistoryRepository;
import me.bombom.api.v1.nativenewsletter.maeilmail.repository.MaeilMailUserAnswerRepository;
import me.bombom.support.acceptance.AcceptanceTest;
import me.bombom.support.acceptance.AcceptanceTestHeaders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@AcceptanceTest("acceptance/maeilmail/maeil-mail-content-empty.json")
class MaeilMailControllerTest {

    private static final Long ARTICLE_ID = 10_001L;
    private static final Long SECOND_ARTICLE_ID = 10_002L;
    private static final Long UNKNOWN_ARTICLE_ID = 99_999L;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MaeilMailContentRepository contentRepository;

    @Autowired
    private MaeilMailContentAnswerRepository contentAnswerRepository;

    @Autowired
    private MaeilMailIssueHistoryRepository issueHistoryRepository;

    @Autowired
    private MaeilMailUserAnswerRepository userAnswerRepository;

    private Member member;
    private MaeilMailContent content;
    private MaeilMailIssueHistory issueHistory;

    @BeforeEach
    void setUp() {
        userAnswerRepository.deleteAllInBatch();
        contentAnswerRepository.deleteAllInBatch();
        issueHistoryRepository.deleteAllInBatch();
        contentRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();

        member = memberRepository.save(TestFixture.normalMemberFixture());
        content = contentRepository.save(createContent());
        contentAnswerRepository.save(createContentAnswer(content.getId()));
        issueHistory = issueHistoryRepository.save(createIssueHistory(ARTICLE_ID, content.getId()));
    }

    @Test
    void 아티클_ID로_매일메일_콘텐츠_정보를_조회한다() {
        MaeilMailInformationResponse response = authenticatedRequest()
                .queryParam("articleId", issueHistory.getArticleId())
                .when()
                .get("/api/v1/maeil-mail/content")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .as(MaeilMailInformationResponse.class);

        assertThat(response.contentId()).isEqualTo(content.getId());
    }

    @Test
    void 매일메일_콘텐츠의_모범_답변을_조회한다() {
        MaeilMailIdealAnswerResponse response = authenticatedRequest()
                .when()
                .get("/api/v1/maeil-mail/{contentId}/answer", content.getId())
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .as(MaeilMailIdealAnswerResponse.class);

        assertSoftly(softly -> {
            softly.assertThat(response.title()).isEqualTo("Java의 GC 동작 방식은?");
            softly.assertThat(response.answer()).isEqualTo("<p>GC는 더 이상 참조되지 않는 객체를 정리합니다.</p>");
        });
    }

    @Test
    void 모범_답변이_없는_컨텐츠는_404를_반환한다() {
        MaeilMailContent contentWithoutAnswer = contentRepository.save(MaeilMailContent.builder()
                .topicId(2L)
                .title("답변 없는 질문")
                .content("<p>질문</p>")
                .contentsText("질문")
                .contentsSummary("질문")
                .expectedReadTime(1)
                .build());

        authenticatedRequest()
                .when()
                .get("/api/v1/maeil-mail/{contentId}/answer", contentWithoutAnswer.getId())
                .then()
                .statusCode(404);
    }

    @Test
    void 아티클_ID로_사용자_답변을_제출하고_다시_조회한다() {
        String answer = "GC Root에서 도달할 수 없는 객체를 수거한다.";

        submitAnswer(issueHistory.getArticleId(), answer)
                .then()
                .statusCode(201);

        MaeilMailUserAnswer savedAnswer = userAnswerRepository.findAll().getFirst();
        assertSoftly(softly -> {
            softly.assertThat(savedAnswer.getMemberId()).isEqualTo(member.getId());
            softly.assertThat(savedAnswer.getIssueHistoryId()).isEqualTo(issueHistory.getId());
            softly.assertThat(savedAnswer.getAnswer()).isEqualTo(answer);
        });

        MaeilMailSubmittedAnswerResponse response = getSubmittedAnswer(issueHistory.getArticleId(), 200)
                .as(MaeilMailSubmittedAnswerResponse.class);

        assertThat(response.answer()).isEqualTo(answer);
    }

    @Test
    void 같은_콘텐츠라도_발행_아티클이_다르면_각각_답변을_제출하고_조회한다() {
        MaeilMailIssueHistory secondIssueHistory = issueHistoryRepository.save(
                createIssueHistory(SECOND_ARTICLE_ID, content.getId())
        );
        String firstAnswer = "첫 번째 발행 아티클에 대한 답변";
        String secondAnswer = "두 번째 발행 아티클에 대한 답변";

        submitAnswer(issueHistory.getArticleId(), firstAnswer)
                .then()
                .statusCode(201);
        submitAnswer(secondIssueHistory.getArticleId(), secondAnswer)
                .then()
                .statusCode(201);

        MaeilMailSubmittedAnswerResponse firstResponse = getSubmittedAnswer(issueHistory.getArticleId(), 200)
                .as(MaeilMailSubmittedAnswerResponse.class);
        MaeilMailSubmittedAnswerResponse secondResponse = getSubmittedAnswer(secondIssueHistory.getArticleId(), 200)
                .as(MaeilMailSubmittedAnswerResponse.class);
        MaeilMailUserAnswer savedFirstAnswer = userAnswerRepository
                .findByMemberIdAndIssueHistoryId(member.getId(), issueHistory.getId())
                .orElseThrow();
        MaeilMailUserAnswer savedSecondAnswer = userAnswerRepository
                .findByMemberIdAndIssueHistoryId(member.getId(), secondIssueHistory.getId())
                .orElseThrow();

        assertSoftly(softly -> {
            softly.assertThat(firstResponse.answer()).isEqualTo(firstAnswer);
            softly.assertThat(secondResponse.answer()).isEqualTo(secondAnswer);
            softly.assertThat(savedFirstAnswer.getAnswer()).isEqualTo(firstAnswer);
            softly.assertThat(savedSecondAnswer.getAnswer()).isEqualTo(secondAnswer);
            softly.assertThat(userAnswerRepository.findAll()).hasSize(2);
        });
    }

    @Test
    void 답변이_1500자면_제출할_수_있다() {
        String answer = "가".repeat(1_500);

        submitAnswer(issueHistory.getArticleId(), answer)
                .then()
                .statusCode(201);

        MaeilMailUserAnswer savedAnswer = userAnswerRepository.findAll().getFirst();
        assertSoftly(softly -> {
            softly.assertThat(savedAnswer.getIssueHistoryId()).isEqualTo(issueHistory.getId());
            softly.assertThat(savedAnswer.getAnswer()).isEqualTo(answer);
        });
    }

    @Test
    void 존재하지_않는_아티클에_답변을_제출하면_404를_반환한다() {
        ErrorResponse response = submitAnswer(UNKNOWN_ARTICLE_ID, "존재하지 않는 아티클에는 저장되지 않는다.")
                .then()
                .statusCode(404)
                .contentType(ContentType.JSON)
                .extract()
                .as(ErrorResponse.class);

        assertThat(response.code()).isEqualTo(ErrorDetail.ENTITY_NOT_FOUND.getCode());
        assertThat(userAnswerRepository.findAll()).isEmpty();
    }

    @Test
    void 기존_콘텐츠_ID_기반_답변_제출_URL은_더_이상_사용하지_않는다() {
        authenticatedRequest()
                .contentType(ContentType.JSON)
                .body(Map.of("answer", "기존 URL로는 저장되지 않는다."))
                .when()
                .post("/api/v1/maeil-mail/{contentId}/answer/me", issueHistory.getContentId())
                .then()
                .statusCode(404);

        assertThat(userAnswerRepository.findAll()).isEmpty();
    }

    @Test
    void 답변이_공백이면_제출할_수_없다() {
        ErrorResponse response = submitAnswer(issueHistory.getArticleId(), " ")
                .then()
                .statusCode(400)
                .contentType(ContentType.JSON)
                .extract()
                .as(ErrorResponse.class);

        assertThat(response.code()).isEqualTo(ErrorDetail.INVALID_REQUEST_BODY_VALIDATION.getCode());
        assertThat(userAnswerRepository.findAll()).isEmpty();
    }

    @Test
    void 답변이_1500자를_초과하면_제출할_수_없다() {
        ErrorResponse response = submitAnswer(issueHistory.getArticleId(), "가".repeat(1_501))
                .then()
                .statusCode(400)
                .contentType(ContentType.JSON)
                .extract()
                .as(ErrorResponse.class);

        assertThat(response.code()).isEqualTo(ErrorDetail.INVALID_REQUEST_BODY_VALIDATION.getCode());
        assertThat(userAnswerRepository.findAll()).isEmpty();
    }

    @Test
    void 아직_제출하지_않은_답변을_조회하면_404를_반환한다() {
        ErrorResponse response = getSubmittedAnswer(issueHistory.getArticleId(), 404)
                .as(ErrorResponse.class);

        assertThat(response.code()).isEqualTo(ErrorDetail.ENTITY_NOT_FOUND.getCode());
    }

    @Test
    void 존재하지_않는_아티클의_제출_답변을_조회하면_404를_반환한다() {
        ErrorResponse response = getSubmittedAnswer(UNKNOWN_ARTICLE_ID, 404)
                .as(ErrorResponse.class);

        assertThat(response.code()).isEqualTo(ErrorDetail.ENTITY_NOT_FOUND.getCode());
    }

    private io.restassured.response.Response submitAnswer(Long articleId, String answer) {
        return authenticatedRequest()
                .contentType(ContentType.JSON)
                .body(Map.of("answer", answer))
                .when()
                .post("/api/v1/maeil-mail/articles/{articleId}/answers/me", articleId);
    }

    private io.restassured.response.ExtractableResponse<io.restassured.response.Response> getSubmittedAnswer(
            Long articleId,
            int statusCode
    ) {
        return authenticatedRequest()
                .when()
                .get("/api/v1/maeil-mail/articles/{articleId}/answers/me", articleId)
                .then()
                .statusCode(statusCode)
                .contentType(ContentType.JSON)
                .extract();
    }

    private io.restassured.specification.RequestSpecification authenticatedRequest() {
        return RestAssured.given()
                .accept(ContentType.JSON)
                .header(AcceptanceTestHeaders.MEMBER_ID, member.getId());
    }

    private MaeilMailContent createContent() {
        return MaeilMailContent.builder()
                .topicId(1L)
                .title("Java의 GC 동작 방식은?")
                .content("<p>Java의 GC 동작 방식은?</p>")
                .contentsText("Java의 GC 동작 방식은?")
                .contentsSummary("GC 질문")
                .expectedReadTime(3)
                .build();
    }

    private MaeilMailContentAnswer createContentAnswer(Long contentId) {
        return MaeilMailContentAnswer.builder()
                .contentId(contentId)
                .answer("<p>GC는 더 이상 참조되지 않는 객체를 정리합니다.</p>")
                .build();
    }

    private MaeilMailIssueHistory createIssueHistory(Long articleId, Long contentId) {
        return MaeilMailIssueHistory.builder()
                .articleId(articleId)
                .contentId(contentId)
                .build();
    }
}
