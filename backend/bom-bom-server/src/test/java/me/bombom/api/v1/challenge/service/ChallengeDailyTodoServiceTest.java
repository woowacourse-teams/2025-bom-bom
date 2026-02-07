package me.bombom.api.v1.challenge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.DayOfWeek;
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
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

@IntegrationTest
class ChallengeDailyTodoServiceTest {

    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

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
    private Article todayArticle;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        challengeDailyTodoRepository.deleteAllInBatch();
        articleRepository.deleteAllInBatch();
        challengeTodoRepository.deleteAllInBatch();
        challengeParticipantRepository.deleteAllInBatch();
        challengeRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();

        member = memberRepository.save(TestFixture.createUniqueMember("tester", "12345"));

        // 평일로 고정 (주말 체크 로직 때문에)
        LocalDate now = LocalDate.now(SEOUL_ZONE);
        while (now.getDayOfWeek() == DayOfWeek.SATURDAY || now.getDayOfWeek() == DayOfWeek.SUNDAY) {
            now = now.plusDays(1);
        }
        today = now;
        
        NewsletterGroup group = TestFixture.createNewsletterGroup("그룹");
        newsletterGroupRepository.save(group);
        
        challenge = challengeRepository.save(TestFixture.createChallenge(
                "테스트 챌린지",
                today.minusDays(5),
                today.plusDays(5),
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

        todayArticle = articleRepository.save(
                TestFixture.createArticle(
                        "오늘 뉴스레터",
                        member.getId(),
                        1L,
                        LocalDateTime.of(today, java.time.LocalTime.now(SEOUL_ZONE))
                )
        );
    }

    @Test
    @Transactional
    void 아티클_읽기시_챌린지_투두_업데이트() {
        // given - 실제 오늘이 주말이 아니어야 함 (ChallengeParticipantTodoListener가 실제 날짜 사용)
        LocalDate actualToday = LocalDate.now(SEOUL_ZONE);
        if (actualToday.getDayOfWeek() == DayOfWeek.SATURDAY || actualToday.getDayOfWeek() == DayOfWeek.SUNDAY) {
            // 주말이면 todo가 생성되지 않으므로 테스트 스킵
            return;
        }

        // 실제 오늘 날짜에 맞는 데이터 재생성
        challengeDailyTodoRepository.deleteAllInBatch();
        articleRepository.deleteAllInBatch();
        
        NewsletterGroup group2 = TestFixture.createNewsletterGroup("그룹2");
        newsletterGroupRepository.save(group2);
        
        challenge = challengeRepository.save(TestFixture.createChallenge(
                "테스트 챌린지",
                actualToday.minusDays(5),
                actualToday.plusDays(5),
                10,
                group2.getId()
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

        todayArticle = articleRepository.save(
                TestFixture.createArticle(
                        "오늘 뉴스레터",
                        member.getId(),
                        1L,
                        LocalDateTime.of(actualToday, java.time.LocalTime.now(SEOUL_ZONE))
                )
        );

        ChallengeParticipant participant = challengeParticipantRepository.findByChallengeIdAndMemberId(
                challenge.getId(), member.getId()).orElseThrow();

        // when
        eventPublisher.publishEvent(new MarkAsReadEvent(member.getId(), todayArticle.getId()));
        TestTransaction.flagForCommit();
        TestTransaction.end();

        // then - 특정 멤버와 챌린지에 대한 todo만 확인
        List<ChallengeDailyTodo> dailyTodos = challengeDailyTodoRepository.findAll().stream()
                .filter(todo -> todo.getParticipantId().equals(participant.getId()))
                .filter(todo -> todo.getChallengeTodoId().equals(readTodo.getId()))
                .filter(todo -> todo.getTodoDate().equals(actualToday))
                .toList();

        assertSoftly(softly -> {
            softly.assertThat(dailyTodos).hasSize(1);
            softly.assertThat(dailyTodos.get(0).getParticipantId()).isEqualTo(participant.getId());
            softly.assertThat(dailyTodos.get(0).getChallengeTodoId()).isEqualTo(readTodo.getId());
            softly.assertThat(dailyTodos.get(0).getTodoDate()).isEqualTo(actualToday);
        });
    }

    @Test
    @Transactional
    void 이미_존재하는_챌린지_투두_중복_생성_안함() {
        // given
        ChallengeParticipant participant = challengeParticipantRepository.findByChallengeIdAndMemberId(
                challenge.getId(), member.getId()).orElseThrow();

        // 기존 todo 생성
        challengeDailyTodoRepository.save(
                TestFixture.createChallengeDailyTodo(
                        participant.getId(),
                        today,
                        readTodo.getId()
                )
        );

        // when
        eventPublisher.publishEvent(new MarkAsReadEvent(member.getId(), todayArticle.getId()));
        TestTransaction.flagForCommit();
        TestTransaction.end();

        // then - 특정 멤버와 챌린지에 대한 todo만 확인
        List<ChallengeDailyTodo> dailyTodos = challengeDailyTodoRepository.findAll().stream()
                .filter(todo -> todo.getParticipantId().equals(participant.getId()))
                .filter(todo -> todo.getChallengeTodoId().equals(readTodo.getId()))
                .filter(todo -> todo.getTodoDate().equals(today))
                .toList();

        assertSoftly(softly -> {
            softly.assertThat(dailyTodos).hasSize(1);
            softly.assertThat(dailyTodos.get(0).getParticipantId()).isEqualTo(participant.getId());
            softly.assertThat(dailyTodos.get(0).getChallengeTodoId()).isEqualTo(readTodo.getId());
            softly.assertThat(dailyTodos.get(0).getTodoDate()).isEqualTo(today);
        });
    }

    @Test
    @Transactional
    void 주말_토요일에는_투두_생성되지_않음() {
        // given
        LocalDate today = LocalDate.now(SEOUL_ZONE);
        LocalDate tempSaturday = today;
        
        // 토요일로 조정
        while (tempSaturday.getDayOfWeek() != DayOfWeek.SATURDAY) {
            tempSaturday = tempSaturday.plusDays(1);
        }
        final LocalDate saturday = tempSaturday;
        
        // 챌린지 기간에 포함되도록 조정
        NewsletterGroup saturdayGroup = TestFixture.createNewsletterGroup("토요일 그룹");
        newsletterGroupRepository.save(saturdayGroup);
        challenge = challengeRepository.save(TestFixture.createChallenge(
                "주말 테스트 챌린지",
                saturday.minusDays(5),
                saturday.plusDays(5),
                10,
                saturdayGroup.getId()
        ));
        
        challengeParticipantRepository.save(
                TestFixture.createChallengeParticipant(
                        challenge.getId(),
                        member.getId(),
                        0
                )
        );

        ChallengeParticipant participant = challengeParticipantRepository.findByChallengeIdAndMemberId(
                challenge.getId(), member.getId()).orElseThrow();

        // when
        challengeDailyTodoService.updateChallengeDailyTodo(member.getId(), null, saturday);
        TestTransaction.flagForCommit();
        TestTransaction.end();

        // then - todo가 생성되지 않아야 함
        List<ChallengeDailyTodo> dailyTodos = challengeDailyTodoRepository.findAll().stream()
                .filter(todo -> todo.getParticipantId().equals(participant.getId()))
                .filter(todo -> todo.getTodoDate().equals(saturday))
                .toList();

        assertThat(dailyTodos).isEmpty();
    }

    @Test
    @Transactional
    void 주말_일요일에는_투두_생성되지_않음() {
        // given
        LocalDate today = LocalDate.now(SEOUL_ZONE);
        LocalDate tempSunday = today;
        
        // 일요일로 조정
        while (tempSunday.getDayOfWeek() != DayOfWeek.SUNDAY) {
            tempSunday = tempSunday.plusDays(1);
        }
        final LocalDate sunday = tempSunday;
        
        // 챌린지 기간에 포함되도록 조정
        NewsletterGroup sundayGroup = TestFixture.createNewsletterGroup("일요일 그룹");
        newsletterGroupRepository.save(sundayGroup);
        challenge = challengeRepository.save(TestFixture.createChallenge(
                "주말 테스트 챌린지",
                sunday.minusDays(5),
                sunday.plusDays(5),
                10,
                sundayGroup.getId()
        ));
        
        challengeParticipantRepository.save(
                TestFixture.createChallengeParticipant(
                        challenge.getId(),
                        member.getId(),
                        0
                )
        );

        ChallengeParticipant participant = challengeParticipantRepository.findByChallengeIdAndMemberId(
                challenge.getId(), member.getId()).orElseThrow();

        // when
        challengeDailyTodoService.updateChallengeDailyTodo(member.getId(), null, sunday);
        TestTransaction.flagForCommit();
        TestTransaction.end();

        // then - todo가 생성되지 않아야 함
        List<ChallengeDailyTodo> dailyTodos = challengeDailyTodoRepository.findAll().stream()
                .filter(todo -> todo.getParticipantId().equals(participant.getId()))
                .filter(todo -> todo.getTodoDate().equals(sunday))
                .toList();

        assertThat(dailyTodos).isEmpty();
    }

    @Test
    @Transactional
    void 평일에는_정상적으로_투두_생성됨() {
        // given
        LocalDate today = LocalDate.now(SEOUL_ZONE);
        LocalDate tempWeekday = today;
        
        // 평일로 조정 (월요일~금요일)
        while (tempWeekday.getDayOfWeek() == DayOfWeek.SATURDAY || tempWeekday.getDayOfWeek() == DayOfWeek.SUNDAY) {
            tempWeekday = tempWeekday.plusDays(1);
        }
        final LocalDate weekday = tempWeekday;
        
        // 챌린지 기간에 포함되도록 조정
        NewsletterGroup weekdayGroup = TestFixture.createNewsletterGroup("평일 그룹");
        newsletterGroupRepository.save(weekdayGroup);
        challenge = challengeRepository.save(TestFixture.createChallenge(
                "평일 테스트 챌린지",
                weekday.minusDays(5),
                weekday.plusDays(5),
                10,
                weekdayGroup.getId()
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

        ChallengeParticipant participant = challengeParticipantRepository.findByChallengeIdAndMemberId(
                challenge.getId(), member.getId()).orElseThrow();

        // when
        challengeDailyTodoService.updateChallengeDailyTodo(member.getId(), null, weekday);
        TestTransaction.flagForCommit();
        TestTransaction.end();

        // then - todo가 정상적으로 생성되어야 함
        List<ChallengeDailyTodo> dailyTodos = challengeDailyTodoRepository.findAll().stream()
                .filter(todo -> todo.getParticipantId().equals(participant.getId()))
                .filter(todo -> todo.getChallengeTodoId().equals(readTodo.getId()))
                .filter(todo -> todo.getTodoDate().equals(weekday))
                .toList();

        assertSoftly(softly -> {
            softly.assertThat(dailyTodos).hasSize(1);
            softly.assertThat(dailyTodos.get(0).getParticipantId()).isEqualTo(participant.getId());
            softly.assertThat(dailyTodos.get(0).getChallengeTodoId()).isEqualTo(readTodo.getId());
            softly.assertThat(dailyTodos.get(0).getTodoDate()).isEqualTo(weekday);
        });
    }
}
