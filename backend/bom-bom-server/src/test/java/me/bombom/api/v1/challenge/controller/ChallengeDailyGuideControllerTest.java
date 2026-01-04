package me.bombom.api.v1.challenge.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.auth.dto.CustomOAuth2User;
import me.bombom.api.v1.challenge.domain.Challenge;
import me.bombom.api.v1.challenge.domain.ChallengeDailyGuide;
import me.bombom.api.v1.challenge.domain.ChallengeDailyGuideComment;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import me.bombom.api.v1.challenge.domain.DailyGuideType;
import me.bombom.api.v1.challenge.dto.request.DailyGuideCommentRequest;
import me.bombom.api.v1.challenge.repository.ChallengeDailyGuideCommentRepository;
import me.bombom.api.v1.challenge.repository.ChallengeDailyGuideRepository;
import me.bombom.api.v1.challenge.repository.ChallengeParticipantRepository;
import me.bombom.api.v1.challenge.repository.ChallengeRepository;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@AutoConfigureMockMvc
class ChallengeDailyGuideControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private ChallengeParticipantRepository challengeParticipantRepository;

    @Autowired
    private ChallengeDailyGuideRepository challengeDailyGuideRepository;

    @Autowired
    private ChallengeDailyGuideCommentRepository challengeDailyGuideCommentRepository;

    @Autowired
    private MemberRepository memberRepository;

    @MockitoBean
    private me.bombom.api.v1.auth.handler.OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    private Member member;
    private Challenge challenge;
    private ChallengeParticipant participant;
    private ChallengeDailyGuide guide;
    private OAuth2AuthenticationToken authToken;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        challengeDailyGuideCommentRepository.deleteAllInBatch();
        challengeDailyGuideRepository.deleteAllInBatch();
        challengeParticipantRepository.deleteAllInBatch();
        challengeRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();

        member = TestFixture.normalMemberFixture();
        memberRepository.save(member);

        today = LocalDate.now();
        challenge = challengeRepository.save(TestFixture.createChallenge(
                "테스트 챌린지",
                today.minusDays(5),
                today.plusDays(5),
                10
        ));

        participant = challengeParticipantRepository.save(
                TestFixture.createChallengeParticipant(
                        challenge.getId(),
                        member.getId(),
                        0
                )
        );

        int dayIndex = (int) java.time.temporal.ChronoUnit.DAYS.between(challenge.getStartDate(), today) + 1;
        guide = challengeDailyGuideRepository.save(
                TestFixture.createChallengeDailyGuide(
                        challenge.getId(),
                        dayIndex,
                        DailyGuideType.COMMENT,
                        "https://example.com/day07.webp",
                        "오늘은 팁을 남겨주세요",
                        true
                )
        );

        Map<String, Object> attributes = Map.of(
                "id", member.getId().toString(),
                "email", member.getEmail(),
                "name", member.getNickname()
        );
        CustomOAuth2User customOAuth2User = new CustomOAuth2User(attributes, member, null, null);
        authToken = new OAuth2AuthenticationToken(
                customOAuth2User,
                customOAuth2User.getAuthorities(),
                "registrationId"
        );
    }

    @Test
    void 오늘의_데일리_가이드_조회_성공() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/challenges/{challengeId}/daily-guides/today", challenge.getId())
                        .with(authentication(authToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dayIndex").value(guide.getDayIndex()))
                .andExpect(jsonPath("$.type").value("COMMENT"))
                .andExpect(jsonPath("$.imageUrl").value("https://example.com/day07.webp"))
                .andExpect(jsonPath("$.notice").value("오늘은 팁을 남겨주세요"))
                .andExpect(jsonPath("$.commentEnabled").value(true))
                .andExpect(jsonPath("$.myComment.exists").value(false))
                .andExpect(jsonPath("$.myComment.content").doesNotExist())
                .andExpect(jsonPath("$.myComment.createdAt").doesNotExist());
    }

    @Test
    void 댓글이_있는_경우_오늘의_데일리_가이드_조회() throws Exception {
        // given
        challengeDailyGuideCommentRepository.save(
                TestFixture.createChallengeDailyGuideComment(
                        guide.getId(),
                        participant.getId(),
                        "뉴스레터 읽기 팁을 공유합니다"
                )
        );

        // when & then
        mockMvc.perform(get("/api/v1/challenges/{challengeId}/daily-guides/today", challenge.getId())
                        .with(authentication(authToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.myComment.exists").value(true))
                .andExpect(jsonPath("$.myComment.content").value("뉴스레터 읽기 팁을 공유합니다"))
                .andExpect(jsonPath("$.myComment.createdAt").exists());
    }

    @Test
    void 데일리_가이드_댓글_작성_성공() throws Exception {
        // given
        DailyGuideCommentRequest request = new DailyGuideCommentRequest("뉴스레터 읽기 팁을 공유합니다");
        int dayIndex = guide.getDayIndex();

        // when & then
        mockMvc.perform(post("/api/v1/challenges/{challengeId}/daily-guides/{dayIndex}/comment",
                        challenge.getId(), dayIndex)
                        .with(authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // then - 댓글이 생성되었는지 확인
        List<ChallengeDailyGuideComment> comments = challengeDailyGuideCommentRepository.findAll();
        assertThat(comments).hasSize(1);
        assertThat(comments.get(0).getContent()).isEqualTo("뉴스레터 읽기 팁을 공유합니다");
    }

    @Test
    void 이미_댓글이_있는_경우_400_응답() throws Exception {
        // given
        challengeDailyGuideCommentRepository.save(
                TestFixture.createChallengeDailyGuideComment(
                        guide.getId(),
                        participant.getId(),
                        "기존 댓글"
                )
        );
        DailyGuideCommentRequest request = new DailyGuideCommentRequest("새 댓글");
        int dayIndex = guide.getDayIndex();

        // when & then
        mockMvc.perform(post("/api/v1/challenges/{challengeId}/daily-guides/{dayIndex}/comment",
                        challenge.getId(), dayIndex)
                        .with(authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 챌린지에_참여하지_않은_경우_404_응답() throws Exception {
        // given
        Member otherMember = TestFixture.createUniqueMember("other", "other");
        memberRepository.save(otherMember);
        Map<String, Object> attributes = Map.of(
                "id", otherMember.getId().toString(),
                "email", otherMember.getEmail(),
                "name", otherMember.getNickname()
        );
        CustomOAuth2User otherUser = new CustomOAuth2User(attributes, otherMember, null, null);
        OAuth2AuthenticationToken otherToken = new OAuth2AuthenticationToken(
                otherUser,
                otherUser.getAuthorities(),
                "registrationId"
        );

        // when & then
        mockMvc.perform(get("/api/v1/challenges/{challengeId}/daily-guides/today", challenge.getId())
                        .with(authentication(otherToken)))
                .andExpect(status().isNotFound());
    }

    @Test
    void 잘못된_challengeId로_조회시_404_응답() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/challenges/{challengeId}/daily-guides/today", 999L)
                        .with(authentication(authToken)))
                .andExpect(status().isNotFound());
    }

    @Test
    void 댓글_내용이_비어있으면_400_응답() throws Exception {
        // given
        DailyGuideCommentRequest request = new DailyGuideCommentRequest("");
        int dayIndex = guide.getDayIndex();

        // when & then
        mockMvc.perform(post("/api/v1/challenges/{challengeId}/daily-guides/{dayIndex}/comment",
                        challenge.getId(), dayIndex)
                        .with(authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}

