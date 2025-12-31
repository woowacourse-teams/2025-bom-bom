package me.bombom.api.v1.challenge.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.auth.dto.CustomOAuth2User;
import me.bombom.api.v1.challenge.domain.Challenge;
import me.bombom.api.v1.challenge.domain.ChallengeNewsletter;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import me.bombom.api.v1.challenge.repository.ChallengeNewsletterRepository;
import me.bombom.api.v1.challenge.repository.ChallengeParticipantRepository;
import me.bombom.api.v1.challenge.repository.ChallengeRepository;
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
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@AutoConfigureMockMvc
class ChallengeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private ChallengeParticipantRepository challengeParticipantRepository;

    @Autowired
    private ChallengeNewsletterRepository challengeNewsletterRepository;

    @Autowired
    private NewsletterRepository newsletterRepository;

    @Autowired
    private NewsletterDetailRepository newsletterDetailRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private MemberRepository memberRepository;

    @MockitoBean
    private me.bombom.api.v1.auth.handler.OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    private Member member;
    private CustomOAuth2User customOAuth2User;
    private OAuth2AuthenticationToken authToken;
    private List<Newsletter> newsletters;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        challengeParticipantRepository.deleteAllInBatch();
        challengeNewsletterRepository.deleteAllInBatch();
        challengeRepository.deleteAllInBatch();
        newsletterRepository.deleteAllInBatch();
        newsletterDetailRepository.deleteAllInBatch();
        categoryRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();

        member = TestFixture.normalMemberFixture();
        memberRepository.save(member);

        List<Category> categories = TestFixture.createCategories();
        categoryRepository.saveAll(categories);

        List<NewsletterDetail> newsletterDetails = TestFixture.createNewsletterDetails();
        newsletterDetailRepository.saveAll(newsletterDetails);

        newsletters = TestFixture.createNewslettersWithDetails(categories, newsletterDetails);
        newsletterRepository.saveAll(newsletters);

        today = LocalDate.now();

        Map<String, Object> attributes = Map.of(
                "id", member.getId().toString(),
                "email", member.getEmail(),
                "name", member.getNickname()
        );
        customOAuth2User = new CustomOAuth2User(attributes, member, null, null);

        authToken = new OAuth2AuthenticationToken(
                customOAuth2User,
                customOAuth2User.getAuthorities(),
                "registrationId"
        );
    }

    @Test
    void 비로그인_상태로_챌린지_목록_조회() throws Exception {
        // given
        Challenge challenge = TestFixture.createChallenge("챌린지", 1, today.minusDays(10), today.plusDays(10));
        challengeRepository.save(challenge);

        ChallengeNewsletter challengeNewsletter = TestFixture.createChallengeNewsletter(
                challenge.getId(),
                newsletters.get(0).getId()
        );
        challengeNewsletterRepository.save(challengeNewsletter);

        // when & then
        mockMvc.perform(get("/api/v1/challenges"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(challenge.getId()))
                .andExpect(jsonPath("$[0].title").value("챌린지"))
                .andExpect(jsonPath("$[0].participantCount").exists())
                .andExpect(jsonPath("$[0].newsletters").isArray())
                .andExpect(jsonPath("$[0].status").exists())
                .andExpect(jsonPath("$[0].detail.isJoined").value(false));
    }

    @Test
    void 로그인_상태로_챌린지_목록_조회() throws Exception {
        // given
        Challenge challenge = TestFixture.createChallenge("챌린지", 1, today.minusDays(10), today.plusDays(10));
        challengeRepository.save(challenge);

        ChallengeParticipant participant = TestFixture.createChallengeParticipant(
                challenge.getId(),
                member.getId(),
                5,
                true
        );
        challengeParticipantRepository.save(participant);

        ChallengeNewsletter challengeNewsletter = TestFixture.createChallengeNewsletter(
                challenge.getId(),
                newsletters.get(0).getId()
        );
        challengeNewsletterRepository.save(challengeNewsletter);

        // when & then
        mockMvc.perform(get("/api/v1/challenges")
                        .with(authentication(authToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(challenge.getId()))
                .andExpect(jsonPath("$[0].detail.isJoined").value(true))
                .andExpect(jsonPath("$[0].detail.progress").exists());
    }

    @Test
    void 챌린지가_없을_때_빈_배열_반환() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/challenges"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}

