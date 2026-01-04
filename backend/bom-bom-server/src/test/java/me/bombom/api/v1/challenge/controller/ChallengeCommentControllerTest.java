package me.bombom.api.v1.challenge.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.article.domain.Article;
import me.bombom.api.v1.article.repository.ArticleRepository;
import me.bombom.api.v1.auth.dto.CustomOAuth2User;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import me.bombom.api.v1.challenge.dto.request.ChallengeCommentRequest;
import me.bombom.api.v1.challenge.repository.ChallengeCommentRepository;
import me.bombom.api.v1.challenge.repository.ChallengeParticipantRepository;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.newsletter.domain.Category;
import me.bombom.api.v1.newsletter.domain.Newsletter;
import me.bombom.api.v1.newsletter.domain.NewsletterDetail;
import me.bombom.api.v1.newsletter.repository.CategoryRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterDetailRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterRepository;
import me.bombom.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@AutoConfigureMockMvc
class ChallengeCommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ChallengeCommentRepository challengeCommentRepository;

    @Autowired
    private ChallengeParticipantRepository challengeParticipantRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private NewsletterRepository newsletterRepository;

    @Autowired
    private NewsletterDetailRepository newsletterDetailRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Member member;
    private List<Newsletter> newsletters;
    private Article article;
    private OAuth2AuthenticationToken authToken;

    @BeforeEach
    void setUp() {
        challengeCommentRepository.deleteAllInBatch();
        challengeParticipantRepository.deleteAllInBatch();
        articleRepository.deleteAllInBatch();
        newsletterRepository.deleteAllInBatch();
        newsletterDetailRepository.deleteAllInBatch();
        categoryRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();

        member = TestFixture.normalMemberFixture();
        memberRepository.save(member);
        var attributes = java.util.Map.<String, Object>of(
                "id", member.getId().toString(),
                "email", member.getEmail(),
                "name", member.getNickname()
        );
        CustomOAuth2User principal = new CustomOAuth2User(attributes, member, null, null);
        authToken = new OAuth2AuthenticationToken(
                principal,
                principal.getAuthorities(),
                "registrationId"
        );

        List<Category> categories = TestFixture.createCategories();
        categoryRepository.saveAll(categories);

        List<NewsletterDetail> details = TestFixture.createNewsletterDetails();
        newsletterDetailRepository.saveAll(details);

        newsletters = TestFixture.createNewslettersWithDetails(categories, details);
        newsletterRepository.saveAll(newsletters);

        article = TestFixture.createArticles(member, newsletters).get(0);
        articleRepository.save(article);

        ChallengeParticipant participant = challengeParticipantRepository.save(
                TestFixture.createChallengeParticipantWithTeam(
                        1L,
                        member.getId(),
                        10L,
                        0,
                        0
                )
        );

        challengeCommentRepository.save(
                TestFixture.createChallengeComment(
                        article.getNewsletterId(),
                        participant.getId(),
                        article.getTitle(),
                        "quote",
                        "comment"
                )
        );
    }

    @Test
    void 챌린지_팀_댓글을_기간으로_필터링해_조회한다() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/challenges/{challengeId}/comments", 1L)
                        .param("start", LocalDate.now().minusDays(1).toString())
                        .param("end", LocalDate.now().plusDays(1).toString())
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                authToken))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].comment").value("comment"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void id가_1_미만이면_400을_응답한다() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/challenges/{challengeId}/comments", 0L)
                        .param("start", LocalDate.now().toString())
                        .param("end", LocalDate.now().toString())
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                authToken))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 챌린지_코멘트_후보_아티클을_조회한다() throws Exception {
        // given
        articleRepository.save(
                Article.builder()
                        .title("타이틀")
                        .contents("<h1>아티클</h1>")
                        .contentsText("아티클")
                        .thumbnailUrl("https://example.com/images/thumb.png")
                        .expectedReadTime(5)
                        .contentsSummary("요약")
                        .isRead(true)
                        .memberId(member.getId())
                        .newsletterId(newsletters.getFirst().getId())
                        .arrivedDateTime(LocalDateTime.now())
                        .build()
        );

        // when & then
        mockMvc.perform(get("/api/v1/challenges/comments/articles/candidates")
                        .param("date", LocalDate.now().toString())
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authToken))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].articleId").exists())
                .andExpect(jsonPath("$[0].newsletterName").isNotEmpty())
                .andExpect(jsonPath("$[0].articleTitle").isNotEmpty());
    }

    @Test
    void 챌린지_코멘트를_생성한다() throws Exception {
        // given
        ChallengeCommentRequest request = new ChallengeCommentRequest(
                article.getId(),
                "quote",
                "챌린지 한 줄 코멘트로 20자 이상의 댓글을 작성했습니다."
        );

        // when & then
        mockMvc.perform(post("/api/v1/challenges/{challengeId}/comments", 1L)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void 코멘트가_20자_미만이면_400을_응답한다() throws Exception {
        // given
        ChallengeCommentRequest request = new ChallengeCommentRequest(
                article.getId(),
                "quote",
                "너무 짧은 댓글"
        );

        // when & then
        mockMvc.perform(post("/api/v1/challenges/{challengeId}/comments", 1L)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
