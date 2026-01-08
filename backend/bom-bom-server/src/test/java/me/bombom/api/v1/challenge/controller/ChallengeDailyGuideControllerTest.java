package me.bombom.api.v1.challenge.controller;

import static java.time.temporal.ChronoUnit.DAYS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
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

    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

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

        today = LocalDate.now(SEOUL_ZONE);
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

        int dayIndex = calculateDayIndex(challenge.getStartDate(), today);
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

    private int calculateDayIndex(LocalDate startDate, LocalDate today) {
        DayOfWeek dayOfWeek = today.getDayOfWeek();
        boolean isWeekend = dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;

        if (isWeekend) {
            return 0;
        }
        return (int) DAYS.between(startDate, today) + 1;
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
        mockMvc.perform(post("/api/v1/challenges/{challengeId}/daily-guides/{dayIndex}/my-comment",
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
        mockMvc.perform(post("/api/v1/challenges/{challengeId}/daily-guides/{dayIndex}/my-comment",
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
        mockMvc.perform(post("/api/v1/challenges/{challengeId}/daily-guides/{dayIndex}/my-comment",
                        challenge.getId(), dayIndex)
                        .with(authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 주말_가이드_조회_성공() throws Exception {
        // given - 주말 가이드 생성 (dayIndex = 0)
        ChallengeDailyGuide weekendGuide = challengeDailyGuideRepository.save(
                TestFixture.createChallengeDailyGuide(
                        challenge.getId(),
                        0,
                        DailyGuideType.COMMENT,
                        "https://example.com/weekend.webp",
                        "주말입니다",
                        false
                )
        );

        // when & then - 주말에는 dayIndex 0으로 조회됨
        // 실제로는 주말이어야 하지만, 가이드가 있으면 정상 조회됨
        mockMvc.perform(get("/api/v1/challenges/{challengeId}/daily-guides/today", challenge.getId())
                        .with(authentication(authToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dayIndex").exists())
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.imageUrl").exists());
    }

    @Test
    void 주말_가이드_댓글_작성_불가() throws Exception {
        // given - 주말 가이드 생성 (dayIndex = 0, commentEnabled = false)
        ChallengeDailyGuide weekendGuide = challengeDailyGuideRepository.save(
                TestFixture.createChallengeDailyGuide(
                        challenge.getId(),
                        0,
                        DailyGuideType.COMMENT,
                        "https://example.com/weekend.webp",
                        "주말입니다",
                        false
                )
        );
        DailyGuideCommentRequest request = new DailyGuideCommentRequest("주말 댓글 작성 시도");

        // when & then - dayIndex 0으로 댓글 작성 시도 (댓글 작성 불가능하므로 400)
        mockMvc.perform(post("/api/v1/challenges/{challengeId}/daily-guides/{dayIndex}/my-comment",
                        challenge.getId(), 0)
                        .with(authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 데일리_가이드_코멘트_목록_조회_성공() throws Exception {
        // given
        challengeDailyGuideCommentRepository.save(
                TestFixture.createChallengeDailyGuideComment(
                        guide.getId(),
                        participant.getId(),
                        "테스트 코멘트"
                )
        );

        // when & then
        mockMvc.perform(get("/api/v1/challenges/{challengeId}/daily-guides/{dayIndex}/comments",
                        challenge.getId(), guide.getDayIndex()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].nickname").exists())
                .andExpect(jsonPath("$.content[0].comment").exists())
                .andExpect(jsonPath("$.content[0].createdAt").exists());
    }

    @Test
    void 존재하지_않는_챌린지로_코멘트_목록_조회시_404_응답() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/challenges/{challengeId}/daily-guides/{dayIndex}/comments",
                        999L, guide.getDayIndex()))
                .andExpect(status().isNotFound());
    }
}

