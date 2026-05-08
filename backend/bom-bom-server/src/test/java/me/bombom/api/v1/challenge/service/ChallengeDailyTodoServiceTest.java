package me.bombom.api.v1.challenge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.BDDMockito.given;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.article.domain.Article;
import me.bombom.api.v1.article.event.MarkAsReadEvent;
import me.bombom.api.v1.article.repository.ArticleRepository;
import me.bombom.api.v1.challenge.domain.Challenge;
import me.bombom.api.v1.challenge.domain.ChallengeDailyTodo;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import me.bombom.api.v1.challenge.domain.ChallengeTodo;
import me.bombom.api.v1.challenge.domain.ChallengeTodoType;
import me.bombom.api.v1.challenge.repository.ChallengeDailyTodoRepository;
import me.bombom.api.v1.challenge.repository.ChallengeParticipantRepository;
import me.bombom.api.v1.challenge.repository.ChallengeRepository;
import me.bombom.api.v1.challenge.repository.ChallengeTodoRepository;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.newsletter.domain.NewsletterGroup;
import me.bombom.api.v1.newsletter.repository.NewsletterGroupRepository;
import me.bombom.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

@IntegrationTest
class ChallengeDailyTodoServiceTest {

    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");
    // 테스트용 고정 평일 (2025-03-10 월요일)
    private static final LocalDate FIXED_WEEKDAY = LocalDate.of(2025, 3, 10);
    // 테스트용 고정 토요일 (2025-03-08)
    private static final LocalDate FIXED_SATURDAY = LocalDate.of(2025, 3, 8);
    // 테스트용 고정 일요일 (2025-03-09)
    private static final LocalDate FIXED_SUNDAY = LocalDate.of(2025, 3, 9);

    @MockitoBean
    private Clock clock;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private ChallengeParticipantRepository challengeParticipantRepository;

    @Autowired
    private ChallengeTodoRepository challengeTodoRepository;

    @Autowired
    private ChallengeDailyTodoRepository challengeDailyTodoRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private ChallengeDailyTodoService challengeDailyTodoService;

    @Autowired
    private NewsletterGroupRepository newsletterGroupRepository;

    private Member member;
    private Challenge challenge;
    private ChallengeTodo readTodo;

    @BeforeEach
    void setUp() {
        fixClockTo(FIXED_WEEKDAY);

        challengeDailyTodoRepository.deleteAllInBatch();
        articleRepository.deleteAllInBatch();
        challengeTodoRepository.deleteAllInBatch();
        challengeParticipantRepository.deleteAllInBatch();
        challengeRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();

        member = memberRepository.save(TestFixture.createUniqueMember("tester", "12345"));

        NewsletterGroup group = TestFixture.createNewsletterGroup("그룹");
        newsletterGroupRepository.save(group);

        challenge = challengeRepository.save(TestFixture.createChallenge(
                "테스트 챌린지",
                FIXED_WEEKDAY.minusDays(5),
                FIXED_WEEKDAY.plusDays(5),
                10,
                group.getId()
        ));

        challengeParticipantRepository.save(
                TestFixture.createChallengeParticipant(
                        challenge.getId(),
                        member.getId(),
                        0
                )
        );

        readTodo = challengeTodoRepository.save(
                TestFixture.createChallengeTodo(challenge.getId(), ChallengeTodoType.READ)
        );
    }

    @Test
    @Transactional
    void 오늘_도착한_아티클_읽기시_챌린지_투두_업데이트() {
        // given
        Article todayArticle = articleRepository.save(
                TestFixture.createArticle(
                        "오늘 뉴스레터",
                        member.getId(),
                        1L,
                        LocalDateTime.of(FIXED_WEEKDAY, java.time.LocalTime.of(9, 0))
                )
        );

        ChallengeParticipant participant = challengeParticipantRepository.findByChallengeIdAndMemberId(
                challenge.getId(), member.getId()).orElseThrow();

        // when
        eventPublisher.publishEvent(new MarkAsReadEvent(member.getId(), todayArticle.getId(), LocalDateTime.now()));
        TestTransaction.flagForCommit();
        TestTransaction.end();

        // then
        List<ChallengeDailyTodo> dailyTodos = challengeDailyTodoRepository.findAll().stream()
                .filter(todo -> todo.getParticipantId().equals(participant.getId()))
                .filter(todo -> todo.getChallengeTodoId().equals(readTodo.getId()))
                .filter(todo -> todo.getTodoDate().equals(FIXED_WEEKDAY))
                .toList();

        assertSoftly(softly -> {
            softly.assertThat(dailyTodos).hasSize(1);
            softly.assertThat(dailyTodos.get(0).getParticipantId()).isEqualTo(participant.getId());
            softly.assertThat(dailyTodos.get(0).getChallengeTodoId()).isEqualTo(readTodo.getId());
            softly.assertThat(dailyTodos.get(0).getTodoDate()).isEqualTo(FIXED_WEEKDAY);
        });
    }

    @Test
    @Transactional
    void 과거_도착한_아티클_읽기시_챌린지_투두_업데이트() {
        // given - 3일 전에 도착한 아티클
        Article pastArticle = articleRepository.save(
                TestFixture.createArticle(
                        "과거 뉴스레터",
                        member.getId(),
                        1L,
                        LocalDateTime.of(FIXED_WEEKDAY.minusDays(3), java.time.LocalTime.of(9, 0))
                )
        );

        ChallengeParticipant participant = challengeParticipantRepository.findByChallengeIdAndMemberId(
                challenge.getId(), member.getId()).orElseThrow();

        // when
        eventPublisher.publishEvent(new MarkAsReadEvent(member.getId(), pastArticle.getId(), LocalDateTime.now()));
        TestTransaction.flagForCommit();
        TestTransaction.end();

        // then - 과거 도착 아티클이어도 오늘 투두가 생성되어야 함
        List<ChallengeDailyTodo> dailyTodos = challengeDailyTodoRepository.findAll().stream()
                .filter(todo -> todo.getParticipantId().equals(participant.getId()))
                .filter(todo -> todo.getChallengeTodoId().equals(readTodo.getId()))
                .filter(todo -> todo.getTodoDate().equals(FIXED_WEEKDAY))
                .toList();

        assertSoftly(softly -> {
            softly.assertThat(dailyTodos).hasSize(1);
            softly.assertThat(dailyTodos.get(0).getParticipantId()).isEqualTo(participant.getId());
            softly.assertThat(dailyTodos.get(0).getChallengeTodoId()).isEqualTo(readTodo.getId());
            softly.assertThat(dailyTodos.get(0).getTodoDate()).isEqualTo(FIXED_WEEKDAY);
        });
    }

    @Test
    @Transactional
    void 이미_존재하는_챌린지_투두_중복_생성_안함() {
        // given
        Article article = articleRepository.save(
                TestFixture.createArticle(
                        "뉴스레터",
                        member.getId(),
                        1L,
                        LocalDateTime.of(FIXED_WEEKDAY, java.time.LocalTime.of(9, 0))
                )
        );

        ChallengeParticipant participant = challengeParticipantRepository.findByChallengeIdAndMemberId(
                challenge.getId(), member.getId()).orElseThrow();

        challengeDailyTodoRepository.save(
                TestFixture.createChallengeDailyTodo(
                        participant.getId(),
                        FIXED_WEEKDAY,
                        readTodo.getId()
                )
        );

        // when
        eventPublisher.publishEvent(new MarkAsReadEvent(member.getId(), article.getId(), LocalDateTime.now()));
        TestTransaction.flagForCommit();
        TestTransaction.end();

        // then
        List<ChallengeDailyTodo> dailyTodos = challengeDailyTodoRepository.findAll().stream()
                .filter(todo -> todo.getParticipantId().equals(participant.getId()))
                .filter(todo -> todo.getChallengeTodoId().equals(readTodo.getId()))
                .filter(todo -> todo.getTodoDate().equals(FIXED_WEEKDAY))
                .toList();

        assertThat(dailyTodos).hasSize(1);
    }

    @Test
    @Transactional
    void 토요일에는_투두_생성되지_않음() {
        // given
        fixClockTo(FIXED_SATURDAY);

        ChallengeParticipant participant = challengeParticipantRepository.findByChallengeIdAndMemberId(
                challenge.getId(), member.getId()).orElseThrow();

        // when
        challengeDailyTodoService.updateChallengeDailyTodo(member.getId(), null);
        TestTransaction.flagForCommit();
        TestTransaction.end();

        // then
        List<ChallengeDailyTodo> dailyTodos = challengeDailyTodoRepository.findAll().stream()
                .filter(todo -> todo.getParticipantId().equals(participant.getId()))
                .toList();

        assertThat(dailyTodos).isEmpty();
    }

    @Test
    @Transactional
    void 일요일에는_투두_생성되지_않음() {
        // given
        fixClockTo(FIXED_SUNDAY);

        ChallengeParticipant participant = challengeParticipantRepository.findByChallengeIdAndMemberId(
                challenge.getId(), member.getId()).orElseThrow();

        // when
        challengeDailyTodoService.updateChallengeDailyTodo(member.getId(), null);
        TestTransaction.flagForCommit();
        TestTransaction.end();

        // then
        List<ChallengeDailyTodo> dailyTodos = challengeDailyTodoRepository.findAll().stream()
                .filter(todo -> todo.getParticipantId().equals(participant.getId()))
                .toList();

        assertThat(dailyTodos).isEmpty();
    }

    @Test
    @Transactional
    void 평일에는_정상적으로_투두_생성됨() {
        // given - clock은 setUp에서 이미 FIXED_WEEKDAY로 고정됨
        ChallengeParticipant participant = challengeParticipantRepository.findByChallengeIdAndMemberId(
                challenge.getId(), member.getId()).orElseThrow();

        // when
        challengeDailyTodoService.updateChallengeDailyTodo(member.getId(), null);
        TestTransaction.flagForCommit();
        TestTransaction.end();

        // then
        List<ChallengeDailyTodo> dailyTodos = challengeDailyTodoRepository.findAll().stream()
                .filter(todo -> todo.getParticipantId().equals(participant.getId()))
                .filter(todo -> todo.getChallengeTodoId().equals(readTodo.getId()))
                .filter(todo -> todo.getTodoDate().equals(FIXED_WEEKDAY))
                .toList();

        assertSoftly(softly -> {
            softly.assertThat(dailyTodos).hasSize(1);
            softly.assertThat(dailyTodos.get(0).getParticipantId()).isEqualTo(participant.getId());
            softly.assertThat(dailyTodos.get(0).getChallengeTodoId()).isEqualTo(readTodo.getId());
            softly.assertThat(dailyTodos.get(0).getTodoDate()).isEqualTo(FIXED_WEEKDAY);
        });
    }

    private void fixClockTo(LocalDate date) {
        given(clock.instant()).willReturn(date.atStartOfDay(SEOUL_ZONE).toInstant());
        given(clock.getZone()).willReturn(SEOUL_ZONE);
    }
}
