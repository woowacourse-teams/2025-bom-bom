package me.bombom.api.v1.nativenewsletter.maeilmail.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.auth.dto.CustomOAuth2User;
import me.bombom.api.v1.auth.handler.OAuth2LoginSuccessHandler;
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
import me.bombom.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@IntegrationTest
@AutoConfigureMockMvc
class MaeilMailControllerTest {

    private static final Long ARTICLE_ID = 10_001L;
    private static final Long SECOND_ARTICLE_ID = 10_002L;
    private static final Long UNKNOWN_ARTICLE_ID = 99_999L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

    @MockitoBean
    private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    private Member member;
    private MaeilMailContent content;
    private MaeilMailIssueHistory issueHistory;
    private OAuth2AuthenticationToken authToken;

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

        authToken = createAuthToken(member);
    }

    @Test
    @DisplayName("아티클 id로 매일메일 컨텐츠 정보를 조회한다")
    void getContentInformationByArticle_success() throws Exception {
        // given
        Long articleId = issueHistory.getArticleId();

        // when
        MvcResult result = mockMvc.perform(get("/api/v1/maeil-mail/content")
                        .with(authentication(authToken))
                        .param("articleId", articleId.toString()))
                .andExpect(status().isOk())
                .andReturn();

        // then
        MaeilMailInformationResponse response = readResponse(result, MaeilMailInformationResponse.class);
        assertThat(response.contentId()).isEqualTo(content.getId());
    }

    @Test
    @DisplayName("매일메일 컨텐츠의 모범 답변을 조회한다")
    void getIdealAnswer_success() throws Exception {
        // given
        Long contentId = content.getId();

        // when
        MvcResult result = mockMvc.perform(get("/api/v1/maeil-mail/{contentId}/answer", contentId)
                        .with(authentication(authToken)))
                .andExpect(status().isOk())
                .andReturn();

        // then
        MaeilMailIdealAnswerResponse response = readResponse(result, MaeilMailIdealAnswerResponse.class);
        assertSoftly(softly -> {
            softly.assertThat(response.title()).isEqualTo("Java의 GC 동작 방식은?");
            softly.assertThat(response.answer()).isEqualTo("<p>GC는 더 이상 참조되지 않는 객체를 정리합니다.</p>");
        });
    }

    @Test
    @DisplayName("아티클 id로 사용자 답변을 제출하고 다시 조회한다")
    void submitAnswerAndGetSubmittedAnswer_success() throws Exception {
        // given
        Long articleId = issueHistory.getArticleId();
        String answer = "GC Root에서 도달할 수 없는 객체를 수거한다.";

        // when
        mockMvc.perform(post("/api/v1/maeil-mail/articles/{articleId}/answers/me", articleId)
                        .with(authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("answer", answer))))
                .andExpect(status().isCreated());

        // then
        MaeilMailUserAnswer savedAnswer = userAnswerRepository.findAll().getFirst();
        assertSoftly(softly -> {
            softly.assertThat(savedAnswer.getMemberId()).isEqualTo(member.getId());
            softly.assertThat(savedAnswer.getIssueHistoryId()).isEqualTo(issueHistory.getId());
            softly.assertThat(savedAnswer.getAnswer()).isEqualTo(answer);
        });

        MvcResult result = mockMvc.perform(get("/api/v1/maeil-mail/articles/{articleId}/answers/me", articleId)
                        .with(authentication(authToken)))
                .andExpect(status().isOk())
                .andReturn();

        MaeilMailSubmittedAnswerResponse response = readResponse(result, MaeilMailSubmittedAnswerResponse.class);
        assertThat(response.answer()).isEqualTo(answer);
    }

    @Test
    @DisplayName("같은 컨텐츠라도 발행 아티클이 다르면 각각 답변을 제출하고 조회한다")
    void submitAnswer_sameContentDifferentArticles_success() throws Exception {
        // given
        MaeilMailIssueHistory secondIssueHistory = issueHistoryRepository.save(
                createIssueHistory(SECOND_ARTICLE_ID, content.getId())
        );
        String firstAnswer = "첫 번째 발행 아티클에 대한 답변";
        String secondAnswer = "두 번째 발행 아티클에 대한 답변";

        // when
        mockMvc.perform(post("/api/v1/maeil-mail/articles/{articleId}/answers/me", issueHistory.getArticleId())
                        .with(authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("answer", firstAnswer))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/maeil-mail/articles/{articleId}/answers/me", secondIssueHistory.getArticleId())
                        .with(authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("answer", secondAnswer))))
                .andExpect(status().isCreated());

        MvcResult firstResult = mockMvc.perform(get("/api/v1/maeil-mail/articles/{articleId}/answers/me",
                        issueHistory.getArticleId())
                        .with(authentication(authToken)))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult secondResult = mockMvc.perform(get("/api/v1/maeil-mail/articles/{articleId}/answers/me",
                        secondIssueHistory.getArticleId())
                        .with(authentication(authToken)))
                .andExpect(status().isOk())
                .andReturn();

        // then
        MaeilMailSubmittedAnswerResponse firstResponse = readResponse(firstResult, MaeilMailSubmittedAnswerResponse.class);
        MaeilMailSubmittedAnswerResponse secondResponse = readResponse(secondResult, MaeilMailSubmittedAnswerResponse.class);
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
    @DisplayName("답변이 1500자면 제출할 수 있다")
    void submitAnswer_answerMaxLength_success() throws Exception {
        // given
        Long articleId = issueHistory.getArticleId();
        String answer = "가".repeat(1_500);

        // when
        mockMvc.perform(post("/api/v1/maeil-mail/articles/{articleId}/answers/me", articleId)
                        .with(authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("answer", answer))))
                .andExpect(status().isCreated());

        // then
        MaeilMailUserAnswer savedAnswer = userAnswerRepository.findAll().getFirst();
        assertSoftly(softly -> {
            softly.assertThat(savedAnswer.getIssueHistoryId()).isEqualTo(issueHistory.getId());
            softly.assertThat(savedAnswer.getAnswer()).isEqualTo(answer);
        });
    }

    @Test
    @DisplayName("존재하지 않는 아티클에 답변을 제출하면 404를 반환한다")
    void submitAnswer_articleNotFound() throws Exception {
        // given
        String answer = "존재하지 않는 아티클에는 저장되지 않는다.";

        // when & then
        MvcResult result = mockMvc.perform(post("/api/v1/maeil-mail/articles/{articleId}/answers/me", UNKNOWN_ARTICLE_ID)
                        .with(authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("answer", answer))))
                .andExpect(status().isNotFound())
                .andReturn();

        ErrorResponse response = readResponse(result, ErrorResponse.class);
        assertThat(response.code()).isEqualTo(ErrorDetail.ENTITY_NOT_FOUND.getCode());
        assertThat(userAnswerRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("기존 컨텐츠 id 기반 답변 제출 URL은 더 이상 사용하지 않는다")
    void submitAnswer_legacyContentIdPathNotFound() throws Exception {
        // given
        Long contentId = issueHistory.getContentId();
        String answer = "기존 URL로는 저장되지 않는다.";

        // when & then
        mockMvc.perform(post("/api/v1/maeil-mail/{contentId}/answer/me", contentId)
                        .with(authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("answer", answer))))
                .andExpect(status().isNotFound());

        assertThat(userAnswerRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("답변이 공백이면 제출할 수 없다")
    void submitAnswer_blankAnswer() throws Exception {
        // given
        Long articleId = issueHistory.getArticleId();

        // when & then
        MvcResult result = mockMvc.perform(post("/api/v1/maeil-mail/articles/{articleId}/answers/me", articleId)
                        .with(authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("answer", " "))))
                .andExpect(status().isBadRequest())
                .andReturn();

        ErrorResponse response = readResponse(result, ErrorResponse.class);
        assertThat(response.code()).isEqualTo(ErrorDetail.INVALID_REQUEST_BODY_VALIDATION.getCode());
        assertThat(userAnswerRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("답변이 1500자를 초과하면 제출할 수 없다")
    void submitAnswer_answerTooLong() throws Exception {
        // given
        Long articleId = issueHistory.getArticleId();
        String answer = "가".repeat(1_501);

        // when & then
        MvcResult result = mockMvc.perform(post("/api/v1/maeil-mail/articles/{articleId}/answers/me", articleId)
                        .with(authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("answer", answer))))
                .andExpect(status().isBadRequest())
                .andReturn();

        ErrorResponse response = readResponse(result, ErrorResponse.class);
        assertThat(response.code()).isEqualTo(ErrorDetail.INVALID_REQUEST_BODY_VALIDATION.getCode());
        assertThat(userAnswerRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("아직 제출하지 않은 답변을 조회하면 404를 반환한다")
    void getSubmittedAnswer_notFound() throws Exception {
        // given
        Long articleId = issueHistory.getArticleId();

        // when & then
        MvcResult result = mockMvc.perform(get("/api/v1/maeil-mail/articles/{articleId}/answers/me", articleId)
                        .with(authentication(authToken)))
                .andExpect(status().isNotFound())
                .andReturn();

        ErrorResponse response = readResponse(result, ErrorResponse.class);
        assertThat(response.code()).isEqualTo(ErrorDetail.ENTITY_NOT_FOUND.getCode());
    }

    @Test
    @DisplayName("존재하지 않는 아티클의 제출 답변을 조회하면 404를 반환한다")
    void getSubmittedAnswer_articleNotFound() throws Exception {
        // when & then
        MvcResult result = mockMvc.perform(get("/api/v1/maeil-mail/articles/{articleId}/answers/me",
                        UNKNOWN_ARTICLE_ID)
                        .with(authentication(authToken)))
                .andExpect(status().isNotFound())
                .andReturn();

        ErrorResponse response = readResponse(result, ErrorResponse.class);
        assertThat(response.code()).isEqualTo(ErrorDetail.ENTITY_NOT_FOUND.getCode());
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

    private OAuth2AuthenticationToken createAuthToken(Member member) {
        Map<String, Object> attributes = Map.of(
                "id", member.getId().toString(),
                "email", member.getEmail(),
                "name", member.getNickname()
        );
        CustomOAuth2User customOAuth2User = new CustomOAuth2User(attributes, member, null, null);

        return new OAuth2AuthenticationToken(
                customOAuth2User,
                customOAuth2User.getAuthorities(),
                "registrationId"
        );
    }

    private String toJson(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    private <T> T readResponse(MvcResult result, Class<T> responseType) throws Exception {
        return objectMapper.readValue(result.getResponse().getContentAsString(), responseType);
    }
}
