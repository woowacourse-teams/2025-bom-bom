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
import java.util.List;
import java.util.Map;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.auth.dto.CustomOAuth2User;
import me.bombom.api.v1.challenge.domain.Challenge;
import me.bombom.api.v1.challenge.domain.ChallengeDailyGuide;
import me.bombom.api.v1.challenge.domain.ChallengeDailyGuideComment;
import me.bombom.api.v1.challenge.domain.ChallengeDailyStatus;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import me.bombom.api.v1.challenge.domain.ChallengeTodo;
import me.bombom.api.v1.challenge.domain.ChallengeTodoType;
import me.bombom.api.v1.challenge.domain.DailyGuideType;
import me.bombom.api.v1.challenge.dto.request.DailyGuideCommentRequest;
import me.bombom.api.v1.challenge.repository.ChallengeDailyGuideCommentRepository;
import me.bombom.api.v1.challenge.repository.ChallengeDailyGuideRepository;
import me.bombom.api.v1.challenge.repository.ChallengeDailyResultRepository;
import me.bombom.api.v1.challenge.repository.ChallengeDailyTodoRepository;
import me.bombom.api.v1.challenge.repository.ChallengeParticipantRepository;
import me.bombom.api.v1.challenge.repository.ChallengeRepository;
import me.bombom.api.v1.challenge.repository.ChallengeTodoRepository;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.newsletter.domain.NewsletterGroup;
import me.bombom.api.v1.newsletter.repository.NewsletterGroupRepository;
import me.bombom.support.IntegrationTest;
import me.bombom.support.MutableClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@IntegrationTest
class ChallengeDailyGuideControllerTest {

    private static final LocalDate MONDAY = LocalDate.of(2026, 1, 26);
    private static final String COMMENT = "뉴스레터 읽기 팁을 공유합니다";

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
    private ChallengeTodoRepository challengeTodoRepository;

    @Autowired
    private ChallengeDailyTodoRepository challengeDailyTodoRepository;

    @Autowired
    private ChallengeDailyResultRepository challengeDailyResultRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private NewsletterGroupRepository newsletterGroupRepository;

    @Autowired
    private MutableClock clock;

    @BeforeEach
    void 시간을_월요일로_고정한다() {
        clock.setDate(MONDAY);
    }

    @Test
    void 오늘의_데일리_가이드를_조회한다() throws Exception {
        DailyGuideTestData data = 참여중인_챌린지와_오늘의_데일리_가이드를_저장한다();

        오늘의_데일리_가이드를_조회한다(data.challenge().getId(), data.authentication())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dayIndex").value(data.guide().getDayIndex()))
                .andExpect(jsonPath("$.type").value("COMMENT"))
                .andExpect(jsonPath("$.imageUrl").value("https://example.com/daily-guide.webp"))
                .andExpect(jsonPath("$.notice").value("오늘은 팁을 남겨주세요"))
                .andExpect(jsonPath("$.commentEnabled").value(true))
                .andExpect(jsonPath("$.myComment.exists").value(false))
                .andExpect(jsonPath("$.myComment.content").doesNotExist())
                .andExpect(jsonPath("$.myComment.createdAt").doesNotExist());
    }

    @Test
    void 작성한_댓글과_함께_오늘의_데일리_가이드를_조회한다() throws Exception {
        DailyGuideTestData data = 참여중인_챌린지와_오늘의_데일리_가이드를_저장한다();
        데일리_가이드_댓글을_저장한다(data, COMMENT);

        오늘의_데일리_가이드를_조회한다(data.challenge().getId(), data.authentication())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.myComment.exists").value(true))
                .andExpect(jsonPath("$.myComment.content").value(COMMENT))
                .andExpect(jsonPath("$.myComment.createdAt").exists());
    }

    @Test
    void 참여하지_않은_회원은_오늘의_데일리_가이드를_조회할_수_없다() throws Exception {
        DailyGuideTestData data = 참여중인_챌린지와_오늘의_데일리_가이드를_저장한다();
        Member nonParticipant = 회원을_저장한다("미참여자", "non-participant");

        오늘의_데일리_가이드를_조회한다(data.challenge().getId(), 인증정보를_생성한다(nonParticipant))
                .andExpect(status().isNotFound());
    }

    @Test
    void 존재하지_않는_챌린지의_오늘의_데일리_가이드는_조회할_수_없다() throws Exception {
        Member member = 회원을_저장한다("회원", "member");

        오늘의_데일리_가이드를_조회한다(999L, 인증정보를_생성한다(member))
                .andExpect(status().isNotFound());
    }

    @Test
    void 진행_기간이_아닌_챌린지의_오늘의_데일리_가이드는_조회할_수_없다() throws Exception {
        Member member = 회원을_저장한다("회원", "member");
        Challenge futureChallenge = 챌린지를_저장한다(MONDAY.plusDays(10), MONDAY.plusDays(20));
        챌린지_참여자를_저장한다(futureChallenge, member);

        오늘의_데일리_가이드를_조회한다(futureChallenge.getId(), 인증정보를_생성한다(member))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 데일리_가이드에_댓글을_작성한다() throws Exception {
        DailyGuideTestData data = 참여중인_챌린지와_데일리_가이드를_저장한다(2, true);

        데일리_가이드_댓글을_작성한다(data, COMMENT)
                .andExpect(status().isCreated());

        List<ChallengeDailyGuideComment> comments = challengeDailyGuideCommentRepository.findAll();
        assertThat(comments).singleElement().satisfies(comment -> {
            assertThat(comment.getGuideId()).isEqualTo(data.guide().getId());
            assertThat(comment.getParticipantId()).isEqualTo(data.participant().getId());
            assertThat(comment.getContent()).isEqualTo(COMMENT);
        });
    }

    @Test
    void 이미_댓글을_작성한_가이드에는_다시_작성할_수_없다() throws Exception {
        DailyGuideTestData data = 참여중인_챌린지와_오늘의_데일리_가이드를_저장한다();
        데일리_가이드_댓글을_저장한다(data, "기존 댓글");

        데일리_가이드_댓글을_작성한다(data, "새 댓글")
                .andExpect(status().isBadRequest());
    }

    @Test
    void 댓글_작성이_비활성화된_가이드에는_댓글을_작성할_수_없다() throws Exception {
        DailyGuideTestData data = 참여중인_챌린지와_데일리_가이드를_저장한다(2, false);

        데일리_가이드_댓글을_작성한다(data, COMMENT)
                .andExpect(status().isBadRequest());
    }

    @Test
    void 존재하지_않는_가이드에는_댓글을_작성할_수_없다() throws Exception {
        DailyGuideTestData data = 참여중인_챌린지와_오늘의_데일리_가이드를_저장한다();

        데일리_가이드_댓글을_작성한다(data.challenge().getId(), 2, data.authentication(), COMMENT)
                .andExpect(status().isNotFound());
    }

    @Test
    void 아직_열리지_않은_가이드에는_댓글을_작성할_수_없다() throws Exception {
        int tomorrowDayIndex = 오늘의_일차를_계산한다() + 1;
        DailyGuideTestData data = 참여중인_챌린지와_데일리_가이드를_저장한다(tomorrowDayIndex, true);

        데일리_가이드_댓글을_작성한다(data, COMMENT)
                .andExpect(status().isBadRequest());
    }

    @Test
    void 빈_내용으로는_댓글을_작성할_수_없다() throws Exception {
        DailyGuideTestData data = 참여중인_챌린지와_오늘의_데일리_가이드를_저장한다();

        데일리_가이드_댓글을_작성한다(data, "")
                .andExpect(status().isBadRequest());
    }

    @Test
    void 첫날_댓글을_작성하면_MINDSET_투두와_출석이_완료된다() throws Exception {
        DailyGuideTestData data = 참여중인_챌린지와_데일리_가이드를_저장한다(1, true);
        ChallengeTodos todos = 챌린지_투두를_저장한다(data.challenge());

        데일리_가이드_댓글을_작성한다(data, "첫날 댓글")
                .andExpect(status().isCreated());

        assertThat(challengeDailyTodoRepository.existsByParticipantIdAndTodoDateAndChallengeTodoId(
                data.participant().getId(), MONDAY, todos.mindset().getId())).isTrue();
        assertThat(challengeDailyResultRepository.existsByParticipantIdAndDate(
                data.participant().getId(), MONDAY)).isTrue();
        ChallengeParticipant participant = challengeParticipantRepository.findById(data.participant().getId()).orElseThrow();
        assertThat(participant.getCompletedDays()).isEqualTo(1);
    }

    @Test
    void 첫날이_아니면_댓글을_작성해도_챌린지_투두가_완료되지_않는다() throws Exception {
        DailyGuideTestData data = 참여중인_챌린지와_데일리_가이드를_저장한다(2, true);
        ChallengeTodos todos = 챌린지_투두를_저장한다(data.challenge());

        데일리_가이드_댓글을_작성한다(data, "둘째 날 댓글")
                .andExpect(status().isCreated());

        assertThat(challengeDailyTodoRepository.existsByParticipantIdAndTodoDateAndChallengeTodoId(
                data.participant().getId(), MONDAY, todos.read().getId())).isFalse();
        assertThat(challengeDailyTodoRepository.existsByParticipantIdAndTodoDateAndChallengeTodoId(
                data.participant().getId(), MONDAY, todos.comment().getId())).isFalse();
        assertThat(challengeDailyTodoRepository.existsByParticipantIdAndTodoDateAndChallengeTodoId(
                data.participant().getId(), MONDAY, todos.mindset().getId())).isFalse();
    }

    @Test
    void 첫날_MINDSET_투두가_이미_있으면_중복으로_생성하지_않는다() throws Exception {
        DailyGuideTestData data = 참여중인_챌린지와_데일리_가이드를_저장한다(1, true);
        ChallengeTodos todos = 챌린지_투두를_저장한다(data.challenge());
        challengeDailyTodoRepository.save(TestFixture.createChallengeDailyTodo(
                data.participant().getId(), MONDAY, todos.mindset().getId()));

        데일리_가이드_댓글을_작성한다(data, "첫날 댓글")
                .andExpect(status().isCreated());

        long mindsetTodoCount = challengeDailyTodoRepository.findAll().stream()
                .filter(todo -> todo.getParticipantId().equals(data.participant().getId()))
                .filter(todo -> todo.getChallengeTodoId().equals(todos.mindset().getId()))
                .filter(todo -> todo.getTodoDate().equals(MONDAY))
                .count();
        assertThat(mindsetTodoCount).isEqualTo(1);
    }

    @Test
    void 첫날_출석이_이미_완료되었으면_출석과_완료일을_중복으로_반영하지_않는다() throws Exception {
        DailyGuideTestData data = 참여중인_챌린지와_데일리_가이드를_저장한다(1, true);
        ChallengeTodos todos = 챌린지_투두를_저장한다(data.challenge());
        challengeDailyResultRepository.save(TestFixture.createChallengeDailyResult(
                data.participant().getId(), MONDAY, ChallengeDailyStatus.COMPLETE));

        데일리_가이드_댓글을_작성한다(data, "첫날 댓글")
                .andExpect(status().isCreated());

        ChallengeParticipant participant = challengeParticipantRepository.findById(data.participant().getId()).orElseThrow();
        assertThat(challengeDailyResultRepository.count()).isEqualTo(1);
        assertThat(participant.getCompletedDays()).isZero();
        assertThat(challengeDailyTodoRepository.existsByParticipantIdAndTodoDateAndChallengeTodoId(
                data.participant().getId(), MONDAY, todos.comment().getId())).isTrue();
        assertThat(challengeDailyTodoRepository.existsByParticipantIdAndTodoDateAndChallengeTodoId(
                data.participant().getId(), MONDAY, todos.mindset().getId())).isTrue();
    }

    @Test
    void 주말에_첫날_댓글을_작성하면_READ를_제외한_투두와_출석이_완료된다() throws Exception {
        DailyGuideTestData data = 참여중인_챌린지와_데일리_가이드를_저장한다(1, true);
        ChallengeTodos todos = 챌린지_투두를_저장한다(data.challenge());
        LocalDate saturday = LocalDate.of(2026, 1, 31);
        clock.setDate(saturday);

        데일리_가이드_댓글을_작성한다(data, "주말 첫날 댓글")
                .andExpect(status().isCreated());

        ChallengeParticipant participant = challengeParticipantRepository.findById(data.participant().getId()).orElseThrow();
        assertThat(challengeDailyTodoRepository.existsByParticipantIdAndTodoDateAndChallengeTodoId(
                data.participant().getId(), saturday, todos.read().getId())).isFalse();
        assertThat(challengeDailyTodoRepository.existsByParticipantIdAndTodoDateAndChallengeTodoId(
                data.participant().getId(), saturday, todos.comment().getId())).isTrue();
        assertThat(challengeDailyTodoRepository.existsByParticipantIdAndTodoDateAndChallengeTodoId(
                data.participant().getId(), saturday, todos.mindset().getId())).isTrue();
        assertThat(challengeDailyResultRepository.existsByParticipantIdAndDate(
                data.participant().getId(), saturday)).isTrue();
        assertThat(participant.getCompletedDays()).isEqualTo(1);
    }

    @Test
    void 주말에는_주말용_데일리_가이드를_조회한다() throws Exception {
        LocalDate saturday = LocalDate.of(2026, 1, 31);
        clock.setDate(saturday);
        DailyGuideTestData data = 참여중인_챌린지와_데일리_가이드를_저장한다(0, false);

        오늘의_데일리_가이드를_조회한다(data.challenge().getId(), data.authentication())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dayIndex").value(0))
                .andExpect(jsonPath("$.type").value("COMMENT"))
                .andExpect(jsonPath("$.imageUrl").value("https://example.com/daily-guide.webp"));
    }

    @Test
    void 데일리_가이드의_댓글_목록을_조회한다() throws Exception {
        DailyGuideTestData data = 참여중인_챌린지와_오늘의_데일리_가이드를_저장한다();
        데일리_가이드_댓글을_저장한다(data, "첫 번째 댓글");
        Member anotherMember = 회원을_저장한다("다른 회원", "another-member");
        ChallengeParticipant anotherParticipant = 챌린지_참여자를_저장한다(data.challenge(), anotherMember);
        challengeDailyGuideCommentRepository.save(TestFixture.createChallengeDailyGuideComment(
                data.guide().getId(), anotherParticipant.getId(), "두 번째 댓글"));

        데일리_가이드_댓글_목록을_조회한다(data, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void 데일리_가이드의_댓글_목록을_페이지_크기에_맞게_조회한다() throws Exception {
        DailyGuideTestData data = 참여중인_챌린지와_오늘의_데일리_가이드를_저장한다();
        데일리_가이드_댓글을_저장한다(data, "첫 번째 댓글");
        Member anotherMember = 회원을_저장한다("다른 회원", "another-member");
        ChallengeParticipant anotherParticipant = 챌린지_참여자를_저장한다(data.challenge(), anotherMember);
        challengeDailyGuideCommentRepository.save(TestFixture.createChallengeDailyGuideComment(
                data.guide().getId(), anotherParticipant.getId(), "두 번째 댓글"));

        데일리_가이드_댓글_목록을_조회한다(data, 1)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void 존재하지_않는_챌린지의_댓글_목록은_조회할_수_없다() throws Exception {
        DailyGuideTestData data = 참여중인_챌린지와_오늘의_데일리_가이드를_저장한다();

        데일리_가이드_댓글_목록을_조회한다(999L, data.guide().getDayIndex(), data.authentication(), null)
                .andExpect(status().isNotFound());
    }

    @Test
    void 챌린지_범위를_벗어난_일차의_댓글_목록은_조회할_수_없다() throws Exception {
        DailyGuideTestData data = 참여중인_챌린지와_오늘의_데일리_가이드를_저장한다();

        데일리_가이드_댓글_목록을_조회한다(
                data.challenge().getId(), data.challenge().getTotalDays() + 1, data.authentication(), null)
                .andExpect(status().isBadRequest());
    }

    @Test
    void 존재하지_않는_가이드의_댓글_목록은_조회할_수_없다() throws Exception {
        DailyGuideTestData data = 참여중인_챌린지와_오늘의_데일리_가이드를_저장한다();
        int missingDayIndex = data.guide().getDayIndex() + 1;

        데일리_가이드_댓글_목록을_조회한다(
                data.challenge().getId(), missingDayIndex, data.authentication(), null)
                .andExpect(status().isNotFound());
    }

    @Test
    void 댓글이_없으면_빈_목록을_반환한다() throws Exception {
        DailyGuideTestData data = 참여중인_챌린지와_오늘의_데일리_가이드를_저장한다();

        데일리_가이드_댓글_목록을_조회한다(data, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));
    }

    @Test
    void 참여하지_않은_회원은_데일리_가이드의_댓글_목록을_조회할_수_없다() throws Exception {
        DailyGuideTestData data = 참여중인_챌린지와_오늘의_데일리_가이드를_저장한다();
        Member nonParticipant = 회원을_저장한다("미참여자", "non-participant");

        데일리_가이드_댓글_목록을_조회한다(
                data.challenge().getId(), data.guide().getDayIndex(), 인증정보를_생성한다(nonParticipant), null)
                .andExpect(status().isNotFound());
    }

    @Test
    void 데일리_가이드에_작성한_내_댓글을_조회한다() throws Exception {
        DailyGuideTestData data = 참여중인_챌린지와_오늘의_데일리_가이드를_저장한다();
        데일리_가이드_댓글을_저장한다(data, COMMENT);

        내_데일리_가이드_댓글을_조회한다(data.challenge().getId(), data.guide().getDayIndex(), data.authentication())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comment").value(COMMENT));
    }

    @Test
    void 작성한_댓글이_없으면_내_댓글_내용은_null이다() throws Exception {
        DailyGuideTestData data = 참여중인_챌린지와_오늘의_데일리_가이드를_저장한다();

        내_데일리_가이드_댓글을_조회한다(data.challenge().getId(), data.guide().getDayIndex(), data.authentication())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comment").doesNotExist());
    }

    @Test
    void 존재하지_않는_챌린지에서는_내_댓글을_조회할_수_없다() throws Exception {
        DailyGuideTestData data = 참여중인_챌린지와_오늘의_데일리_가이드를_저장한다();

        내_데일리_가이드_댓글을_조회한다(999L, data.guide().getDayIndex(), data.authentication())
                .andExpect(status().isNotFound());
    }

    @Test
    void 참여하지_않은_회원은_내_댓글을_조회할_수_없다() throws Exception {
        DailyGuideTestData data = 참여중인_챌린지와_오늘의_데일리_가이드를_저장한다();
        Member nonParticipant = 회원을_저장한다("미참여자", "non-participant");

        내_데일리_가이드_댓글을_조회한다(
                        data.challenge().getId(), data.guide().getDayIndex(), 인증정보를_생성한다(nonParticipant))
                .andExpect(status().isNotFound());
    }

    @Test
    void 아직_열리지_않은_가이드의_내_댓글은_조회할_수_없다() throws Exception {
        DailyGuideTestData data = 참여중인_챌린지와_오늘의_데일리_가이드를_저장한다();
        int tomorrowDayIndex = 오늘의_일차를_계산한다() + 1;

        내_데일리_가이드_댓글을_조회한다(
                        data.challenge().getId(), tomorrowDayIndex, data.authentication())
                .andExpect(status().isBadRequest());
    }

    private DailyGuideTestData 참여중인_챌린지와_오늘의_데일리_가이드를_저장한다() {
        return 참여중인_챌린지와_데일리_가이드를_저장한다(오늘의_일차를_계산한다(), true);
    }

    private DailyGuideTestData 참여중인_챌린지와_데일리_가이드를_저장한다(
            int dayIndex,
            boolean commentEnabled
    ) {
        Member member = 회원을_저장한다("회원", "member");
        LocalDate today = LocalDate.now(clock);
        Challenge challenge = 챌린지를_저장한다(today.minusDays(5), today.plusDays(5));
        ChallengeParticipant participant = 챌린지_참여자를_저장한다(challenge, member);
        ChallengeDailyGuide guide = challengeDailyGuideRepository.save(TestFixture.createChallengeDailyGuide(
                challenge.getId(),
                dayIndex,
                DailyGuideType.COMMENT,
                "https://example.com/daily-guide.webp",
                "오늘은 팁을 남겨주세요",
                commentEnabled
        ));
        return new DailyGuideTestData(member, 인증정보를_생성한다(member), challenge, participant, guide);
    }

    private Challenge 챌린지를_저장한다(LocalDate startDate, LocalDate endDate) {
        NewsletterGroup group = newsletterGroupRepository.save(TestFixture.createNewsletterGroup("그룹"));
        return challengeRepository.save(TestFixture.createChallenge(
                "테스트 챌린지",
                startDate,
                endDate,
                10,
                group.getId()
        ));
    }

    private Member 회원을_저장한다(String nickname, String identifier) {
        return memberRepository.save(TestFixture.createUniqueMember(nickname, identifier));
    }

    private ChallengeParticipant 챌린지_참여자를_저장한다(Challenge challenge, Member member) {
        return challengeParticipantRepository.save(TestFixture.createChallengeParticipant(
                challenge.getId(), member.getId(), 0));
    }

    private ChallengeTodos 챌린지_투두를_저장한다(Challenge challenge) {
        ChallengeTodo read = challengeTodoRepository.save(
                TestFixture.createChallengeTodo(challenge.getId(), ChallengeTodoType.READ));
        ChallengeTodo comment = challengeTodoRepository.save(
                TestFixture.createChallengeTodo(challenge.getId(), ChallengeTodoType.COMMENT));
        ChallengeTodo mindset = challengeTodoRepository.save(
                TestFixture.createChallengeTodo(challenge.getId(), ChallengeTodoType.MINDSET));
        return new ChallengeTodos(read, comment, mindset);
    }

    private ChallengeDailyGuideComment 데일리_가이드_댓글을_저장한다(DailyGuideTestData data, String content) {
        return challengeDailyGuideCommentRepository.save(TestFixture.createChallengeDailyGuideComment(
                data.guide().getId(), data.participant().getId(), content));
    }

    private OAuth2AuthenticationToken 인증정보를_생성한다(Member member) {
        Map<String, Object> attributes = Map.of(
                "id", member.getId().toString(),
                "email", member.getEmail(),
                "name", member.getNickname()
        );
        CustomOAuth2User user = new CustomOAuth2User(attributes, member, null, null);
        return new OAuth2AuthenticationToken(user, user.getAuthorities(), "registrationId");
    }

    private int 오늘의_일차를_계산한다() {
        LocalDate today = LocalDate.now(clock);
        if (today.getDayOfWeek() == DayOfWeek.SATURDAY || today.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return 0;
        }
        return (int) DAYS.between(today.minusDays(5), today) + 1;
    }

    private ResultActions 오늘의_데일리_가이드를_조회한다(
            Long challengeId,
            OAuth2AuthenticationToken authentication
    ) throws Exception {
        return mockMvc.perform(get("/api/v1/challenges/{challengeId}/daily-guides/today", challengeId)
                .with(authentication(authentication)));
    }

    private ResultActions 데일리_가이드_댓글을_작성한다(DailyGuideTestData data, String content) throws Exception {
        return 데일리_가이드_댓글을_작성한다(
                data.challenge().getId(), data.guide().getDayIndex(), data.authentication(), content);
    }

    private ResultActions 데일리_가이드_댓글을_작성한다(
            Long challengeId,
            int dayIndex,
            OAuth2AuthenticationToken authentication,
            String content
    ) throws Exception {
        DailyGuideCommentRequest request = new DailyGuideCommentRequest(content);
        return mockMvc.perform(post("/api/v1/challenges/{challengeId}/daily-guides/{dayIndex}/my-comment",
                        challengeId, dayIndex)
                .with(authentication(authentication))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    private ResultActions 데일리_가이드_댓글_목록을_조회한다(DailyGuideTestData data, Integer size) throws Exception {
        return 데일리_가이드_댓글_목록을_조회한다(
                data.challenge().getId(), data.guide().getDayIndex(), data.authentication(), size);
    }

    private ResultActions 데일리_가이드_댓글_목록을_조회한다(
            Long challengeId,
            int dayIndex,
            OAuth2AuthenticationToken authentication,
            Integer size
    ) throws Exception {
        var request = get("/api/v1/challenges/{challengeId}/daily-guides/{dayIndex}/comments", challengeId, dayIndex)
                .with(authentication(authentication));
        if (size != null) {
            request.param("size", String.valueOf(size));
        }
        return mockMvc.perform(request);
    }

    private ResultActions 내_데일리_가이드_댓글을_조회한다(
            Long challengeId,
            int dayIndex,
            OAuth2AuthenticationToken authentication
    ) throws Exception {
        return mockMvc.perform(get("/api/v1/challenges/{challengeId}/daily-guides/{dayIndex}/my-comment",
                        challengeId, dayIndex)
                .with(authentication(authentication)));
    }

    private record DailyGuideTestData(
            Member member,
            OAuth2AuthenticationToken authentication,
            Challenge challenge,
            ChallengeParticipant participant,
            ChallengeDailyGuide guide
    ) {
    }

    private record ChallengeTodos(ChallengeTodo read, ChallengeTodo comment, ChallengeTodo mindset) {
    }
}
