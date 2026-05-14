package me.bombom.api.v1.challenge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.time.LocalDate;
import java.util.List;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.challenge.domain.Challenge;
import me.bombom.api.v1.challenge.domain.ChallengeDailyStatus;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import me.bombom.api.v1.challenge.domain.ChallengeTodoType;
import me.bombom.api.v1.challenge.domain.notification.ChallengeTodoReminderNotification;
import me.bombom.api.v1.challenge.domain.notification.ChallengeTodoReminderPhase;
import me.bombom.api.v1.challenge.domain.notification.NotificationStatus;
import me.bombom.api.v1.challenge.repository.ChallengeDailyResultRepository;
import me.bombom.api.v1.challenge.repository.ChallengeDailyTodoRepository;
import me.bombom.api.v1.challenge.repository.ChallengeParticipantRepository;
import me.bombom.api.v1.challenge.repository.ChallengeRepository;
import me.bombom.api.v1.challenge.repository.ChallengeTodoReminderNotificationRepository;
import me.bombom.api.v1.challenge.repository.ChallengeTodoRepository;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.newsletter.domain.NewsletterGroup;
import me.bombom.api.v1.newsletter.repository.NewsletterGroupRepository;
import me.bombom.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
class ChallengeTodoReminderNotificationServiceTest {

    @Autowired
    private ChallengeTodoReminderNotificationService challengeTodoReminderNotificationService;

    @Autowired
    private ChallengeTodoReminderNotificationRepository challengeTodoReminderNotificationRepository;

    @Autowired
    private ChallengeDailyTodoRepository challengeDailyTodoRepository;

    @Autowired
    private ChallengeDailyResultRepository challengeDailyResultRepository;

    @Autowired
    private ChallengeTodoRepository challengeTodoRepository;

    @Autowired
    private ChallengeParticipantRepository challengeParticipantRepository;

    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private NewsletterGroupRepository newsletterGroupRepository;

    @BeforeEach
    void setUp() {
        challengeTodoReminderNotificationRepository.deleteAllInBatch();
        challengeDailyResultRepository.deleteAllInBatch();
        challengeDailyTodoRepository.deleteAllInBatch();
        challengeParticipantRepository.deleteAllInBatch();
        challengeTodoRepository.deleteAllInBatch();
        challengeRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
        newsletterGroupRepository.deleteAllInBatch();
    }

    @Test
    void 오늘_COMPLETE_DailyResult가_없는_참여자에게만_PENDING_알림을_생성한다() {
        // given
        LocalDate today = LocalDate.now();
        NewsletterGroup group = newsletterGroupRepository.save(TestFixture.createNewsletterGroup("group"));
        Challenge challenge = challengeRepository.save(
                TestFixture.createChallenge("진행 중 챌린지", today.minusDays(1), today.plusDays(3), 5, group.getId()));

        Member incompleteMember = memberRepository.save(TestFixture.createUniqueMember("incomplete", "p1"));
        Member completeMember = memberRepository.save(TestFixture.createUniqueMember("complete", "p2"));
        Member anotherIncompleteMember = memberRepository.save(TestFixture.createUniqueMember("incomplete2", "p3"));

        var participant1 = challengeParticipantRepository.save(
                TestFixture.createChallengeParticipant(challenge.getId(), incompleteMember.getId(), 0));
        var participant2 = challengeParticipantRepository.save(
                TestFixture.createChallengeParticipant(challenge.getId(), completeMember.getId(), 0));
        var participant3 = challengeParticipantRepository.save(
                TestFixture.createChallengeParticipant(challenge.getId(), anotherIncompleteMember.getId(), 0));

        challengeTodoRepository.save(TestFixture.createChallengeTodo(challenge.getId(), ChallengeTodoType.READ));

        challengeDailyResultRepository.save(TestFixture.createChallengeDailyResult(
                participant2.getId(),
                today,
                ChallengeDailyStatus.COMPLETE
        ));

        // when
        challengeTodoReminderNotificationService.createPendingNotificationsForIncompleteTodos(today, ChallengeTodoReminderPhase.FIRST);

        // then
        List<ChallengeTodoReminderNotification> notifications = challengeTodoReminderNotificationRepository.findAll();
        assertThat(notifications)
                .extracting(
                        ChallengeTodoReminderNotification::getMemberId,
                        ChallengeTodoReminderNotification::getChallengeId,
                        ChallengeTodoReminderNotification::getChallengeName,
                        ChallengeTodoReminderNotification::getPhase,
                        ChallengeTodoReminderNotification::getStatus,
                        ChallengeTodoReminderNotification::getAttempts,
                        ChallengeTodoReminderNotification::getDaysSinceLastParticipation
                )
                .containsExactlyInAnyOrder(
                        tuple(
                                incompleteMember.getId(),
                                challenge.getId(),
                                challenge.getName(),
                                ChallengeTodoReminderPhase.FIRST,
                                NotificationStatus.PENDING,
                                0,
                                null
                        ),
                        tuple(
                                anotherIncompleteMember.getId(),
                                challenge.getId(),
                                challenge.getName(),
                                ChallengeTodoReminderPhase.FIRST,
                                NotificationStatus.PENDING,
                                0,
                                null
                        )
                );
    }

    @Test
    void 같은_날짜에_이미_적재된_알림은_중복_생성하지_않는다() {
        // given
        LocalDate today = LocalDate.now();
        NewsletterGroup group = newsletterGroupRepository.save(TestFixture.createNewsletterGroup("group"));
        Challenge challenge = challengeRepository.save(
                TestFixture.createChallenge("진행 중 챌린지", today.minusDays(1), today.plusDays(3), 5, group.getId()));

        Member member1 = memberRepository.save(TestFixture.createUniqueMember("m1", "p1"));
        Member member2 = memberRepository.save(TestFixture.createUniqueMember("m2", "p2"));

        challengeParticipantRepository.save(TestFixture.createChallengeParticipant(challenge.getId(), member1.getId(), 0));
        challengeParticipantRepository.save(TestFixture.createChallengeParticipant(challenge.getId(), member2.getId(), 0));
        challengeTodoRepository.save(TestFixture.createChallengeTodo(challenge.getId(), ChallengeTodoType.READ));

        challengeTodoReminderNotificationRepository.save(
                ChallengeTodoReminderNotification.createPending(
                        member1.getId(),
                        challenge.getId(),
                        challenge.getName(),
                        ChallengeTodoReminderPhase.FIRST,
                        0,
                        null,
                        0,
                        false
                ));

        // when
        challengeTodoReminderNotificationService.createPendingNotificationsForIncompleteTodos(today, ChallengeTodoReminderPhase.FIRST);

        // then
        List<ChallengeTodoReminderNotification> notifications = challengeTodoReminderNotificationRepository.findAll();
        assertThat(notifications).hasSize(2);
        assertThat(notifications)
                .extracting(ChallengeTodoReminderNotification::getMemberId)
                .containsExactlyInAnyOrder(member1.getId(), member2.getId());
    }

    @Test
    void 같은_날짜라도_phase가_다르면_알림을_생성한다() {
        // given
        LocalDate today = LocalDate.now();
        NewsletterGroup group = newsletterGroupRepository.save(TestFixture.createNewsletterGroup("group"));
        Challenge challenge = challengeRepository.save(
                TestFixture.createChallenge("진행 중 챌린지", today.minusDays(1), today.plusDays(3), 5, group.getId()));

        Member member = memberRepository.save(TestFixture.createUniqueMember("m1", "p1"));
        challengeParticipantRepository.save(TestFixture.createChallengeParticipant(challenge.getId(), member.getId(), 0));
        challengeTodoRepository.save(TestFixture.createChallengeTodo(challenge.getId(), ChallengeTodoType.READ));

        challengeTodoReminderNotificationRepository.save(
                ChallengeTodoReminderNotification.createPending(
                        member.getId(),
                        challenge.getId(),
                        challenge.getName(),
                        ChallengeTodoReminderPhase.FIRST,
                        0,
                        null,
                        0,
                        false
                ));

        // when
        challengeTodoReminderNotificationService.createPendingNotificationsForIncompleteTodos(today, ChallengeTodoReminderPhase.SECOND);

        // then
        List<ChallengeTodoReminderNotification> notifications = challengeTodoReminderNotificationRepository.findAll();
        assertThat(notifications).hasSize(2);
        assertThat(notifications)
                .extracting(ChallengeTodoReminderNotification::getPhase)
                .containsExactlyInAnyOrder(ChallengeTodoReminderPhase.FIRST, ChallengeTodoReminderPhase.SECOND);
    }

    @Test
    void 스트릭이_있는_참여자에게는_스트릭_값이_저장된_알림을_생성한다() {
        // given
        LocalDate today = LocalDate.now();
        NewsletterGroup group = newsletterGroupRepository.save(TestFixture.createNewsletterGroup("group"));
        Challenge challenge = challengeRepository.save(
                TestFixture.createChallenge("봄봄 챌린지", today.minusDays(7), today.plusDays(10), 20, group.getId()));
        Member member = memberRepository.save(TestFixture.createUniqueMember("m1", "p1"));
        challengeParticipantRepository.save(
                ChallengeParticipant.builder()
                        .challengeId(challenge.getId())
                        .memberId(member.getId())
                        .completedDays(7)
                        .isSurvived(true)
                        .shield(0)
                        .streak(7)
                        .lastParticipatedDate(today.minusDays(1))
                        .build());
        challengeTodoRepository.save(TestFixture.createChallengeTodo(challenge.getId(), ChallengeTodoType.READ));

        // when
        challengeTodoReminderNotificationService.createPendingNotificationsForIncompleteTodos(today, ChallengeTodoReminderPhase.FIRST);

        // then
        ChallengeTodoReminderNotification notification = challengeTodoReminderNotificationRepository.findAll().get(0);
        assertThat(notification.getStreak()).isEqualTo(7);
        assertThat(notification.getDaysSinceLastParticipation()).isEqualTo(0); // 어제 참여, 오늘 제외 → 결석일 0
        assertThat(notification.isLastDay()).isFalse();
        assertThat(notification.getRemainingAbsences()).isNotNegative();
    }

    @Test
    void 챌린지_마지막_날에는_isLastDay가_true인_알림을_생성한다() {
        // given
        LocalDate today = LocalDate.now();
        NewsletterGroup group = newsletterGroupRepository.save(TestFixture.createNewsletterGroup("group"));
        Challenge challenge = challengeRepository.save(
                TestFixture.createChallenge("봄봄 챌린지", today.minusDays(14), today, 15, group.getId()));
        Member member = memberRepository.save(TestFixture.createUniqueMember("m1", "p1"));
        challengeParticipantRepository.save(TestFixture.createChallengeParticipant(challenge.getId(), member.getId(), 0));
        challengeTodoRepository.save(TestFixture.createChallengeTodo(challenge.getId(), ChallengeTodoType.READ));

        // when
        challengeTodoReminderNotificationService.createPendingNotificationsForIncompleteTodos(today, ChallengeTodoReminderPhase.FIRST);

        // then
        ChallengeTodoReminderNotification notification = challengeTodoReminderNotificationRepository.findAll().get(0);
        assertThat(notification.isLastDay()).isTrue();
    }

    @Test
    void 스트릭이_0이어도_마지막_COMPLETE_기준_며칠_전_참여인지_저장한다() {
        // given
        // today = 금요일, lastParticipatedDate = 월요일(-4일) → 화/수/목 = 3 평일 결석 (오늘 제외)
        LocalDate today = LocalDate.of(2025, 4, 4); // 금요일
        NewsletterGroup group = newsletterGroupRepository.save(TestFixture.createNewsletterGroup("group"));
        Challenge challenge = challengeRepository.save(
                TestFixture.createChallenge("봄봄 챌린지", today.minusDays(14), today.plusDays(7), 22, group.getId()));
        Member member = memberRepository.save(TestFixture.createUniqueMember("m1", "p1"));
        var participant = challengeParticipantRepository.save(
                ChallengeParticipant.builder()
                        .challengeId(challenge.getId())
                        .memberId(member.getId())
                        .completedDays(3)
                        .isSurvived(true)
                        .shield(0)
                        .streak(0)
                        .lastParticipatedDate(today.minusDays(4)) // 월요일
                        .build());
        challengeTodoRepository.save(TestFixture.createChallengeTodo(challenge.getId(), ChallengeTodoType.READ));

        // when
        challengeTodoReminderNotificationService.createPendingNotificationsForIncompleteTodos(today, ChallengeTodoReminderPhase.FIRST);

        // then
        ChallengeTodoReminderNotification notification = challengeTodoReminderNotificationRepository.findAll().get(0);
        assertThat(notification.getStreak()).isZero();
        assertThat(notification.getDaysSinceLastParticipation()).isEqualTo(3); // 화/수/목 = 3 평일 (오늘 금요일 제외)
    }

    @Test
    void 탈락까지_남은_결석_횟수를_저장한다() {
        // given
        // totalDays=10, maxAllowedAbsent = 10 - ceil(10 * 0.8) = 10 - 8 = 2
        LocalDate today = LocalDate.of(2025, 4, 7); // 월요일
        LocalDate startDate = LocalDate.of(2025, 3, 31); // 월요일 (오늘 기준 -7일)
        // 어제 = 4/6(일요일) → passedDays = 월~금(5) = 5일
        // completedDays=4 → currentAbsent = 5 - 4 = 1 → remainingAbsences = 2 - 1 = 1
        NewsletterGroup group = newsletterGroupRepository.save(TestFixture.createNewsletterGroup("group"));
        Challenge challenge = challengeRepository.save(
                TestFixture.createChallenge("봄봄 챌린지", startDate, today.plusDays(7), 10, group.getId()));
        Member member = memberRepository.save(TestFixture.createUniqueMember("m1", "p1"));
        challengeParticipantRepository.save(
                ChallengeParticipant.builder()
                        .challengeId(challenge.getId())
                        .memberId(member.getId())
                        .completedDays(4) // passedDays=5, completedDays=4 → currentAbsent=1 → remainingAbsences=2-1=1
                        .isSurvived(true)
                        .shield(0)
                        .streak(5)
                        .lastParticipatedDate(today.minusDays(1))
                        .build());
        challengeTodoRepository.save(TestFixture.createChallengeTodo(challenge.getId(), ChallengeTodoType.READ));

        // when
        challengeTodoReminderNotificationService.createPendingNotificationsForIncompleteTodos(today, ChallengeTodoReminderPhase.FIRST);

        // then
        ChallengeTodoReminderNotification notification = challengeTodoReminderNotificationRepository.findAll().get(0);
        assertThat(notification.getRemainingAbsences()).isEqualTo(1);
    }

    @Test
    void 쉴드가_있는_경우_남은_결석_횟수에_쉴드_수를_더해서_저장한다() {
        // given
        // totalDays=10, maxAllowedAbsent = 10 - ceil(10 * 0.8) = 10 - 8 = 2
        LocalDate today = LocalDate.of(2025, 4, 7); // 월요일
        LocalDate startDate = LocalDate.of(2025, 3, 31); // 월요일 (오늘 기준 -7일)
        // 어제 = 4/6(일요일) → passedDays = 월~금(5) = 5일
        // completedDays=4 → currentAbsent = 5 - 4 = 1 → remainingAbsences = 2 - 1 + shield(2) = 3
        NewsletterGroup group = newsletterGroupRepository.save(TestFixture.createNewsletterGroup("group"));
        Challenge challenge = challengeRepository.save(
                TestFixture.createChallenge("봄봄 챌린지", startDate, today.plusDays(7), 10, group.getId()));
        Member member = memberRepository.save(TestFixture.createUniqueMember("m1", "p1"));
        challengeParticipantRepository.save(
                ChallengeParticipant.builder()
                        .challengeId(challenge.getId())
                        .memberId(member.getId())
                        .completedDays(4)
                        .isSurvived(true)
                        .shield(2)
                        .streak(5)
                        .lastParticipatedDate(today.minusDays(1))
                        .build());
        challengeTodoRepository.save(TestFixture.createChallengeTodo(challenge.getId(), ChallengeTodoType.READ));

        // when
        challengeTodoReminderNotificationService.createPendingNotificationsForIncompleteTodos(today, ChallengeTodoReminderPhase.FIRST);

        // then
        ChallengeTodoReminderNotification notification = challengeTodoReminderNotificationRepository.findAll().get(0);
        assertThat(notification.getRemainingAbsences()).isEqualTo(3); // 2 - 1 + shield(2)
    }
}
