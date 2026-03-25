package me.bombom.api.v1.challenge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.time.LocalDate;
import java.util.List;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.challenge.domain.Challenge;
import me.bombom.api.v1.challenge.domain.ChallengeDailyStatus;
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
                        ChallengeTodoReminderNotification::getAttempts
                )
                .containsExactlyInAnyOrder(
                        tuple(
                                incompleteMember.getId(),
                                challenge.getId(),
                                challenge.getName(),
                                ChallengeTodoReminderPhase.FIRST,
                                NotificationStatus.PENDING,
                                0
                        ),
                        tuple(
                                anotherIncompleteMember.getId(),
                                challenge.getId(),
                                challenge.getName(),
                                ChallengeTodoReminderPhase.FIRST,
                                NotificationStatus.PENDING,
                                0
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
                TestFixture.createChallengeParticipantWithStreak(challenge.getId(), member.getId(), 7));
        challengeTodoRepository.save(TestFixture.createChallengeTodo(challenge.getId(), ChallengeTodoType.READ));

        // when
        challengeTodoReminderNotificationService.createPendingNotificationsForIncompleteTodos(today, ChallengeTodoReminderPhase.FIRST);

        // then
        ChallengeTodoReminderNotification notification = challengeTodoReminderNotificationRepository.findAll().get(0);
        assertThat(notification.getStreak()).isEqualTo(7);
        assertThat(notification.isLastDay()).isFalse();
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
}
