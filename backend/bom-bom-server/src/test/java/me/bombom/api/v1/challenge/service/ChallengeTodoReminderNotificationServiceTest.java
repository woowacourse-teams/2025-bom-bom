package me.bombom.api.v1.challenge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.time.LocalDate;
import java.util.List;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.challenge.domain.Challenge;
import me.bombom.api.v1.challenge.domain.ChallengeTodoType;
import me.bombom.api.v1.challenge.domain.notification.ChallengeTodoReminderNotification;
import me.bombom.api.v1.challenge.domain.notification.NotificationStatus;
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
        challengeDailyTodoRepository.deleteAllInBatch();
        challengeParticipantRepository.deleteAllInBatch();
        challengeTodoRepository.deleteAllInBatch();
        challengeRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
        newsletterGroupRepository.deleteAllInBatch();
    }

    @Test
    void 오늘_TODO가_미완료인_참여자에게만_PENDING_알림을_생성한다() {
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

        var readTodo = challengeTodoRepository.save(TestFixture.createChallengeTodo(challenge.getId(), ChallengeTodoType.READ));
        var commentTodo = challengeTodoRepository.save(
                TestFixture.createChallengeTodo(challenge.getId(), ChallengeTodoType.COMMENT));

        challengeDailyTodoRepository.save(TestFixture.createChallengeDailyTodo(participant2.getId(), today, readTodo.getId()));
        challengeDailyTodoRepository.save(TestFixture.createChallengeDailyTodo(participant2.getId(), today, commentTodo.getId()));
        challengeDailyTodoRepository.save(TestFixture.createChallengeDailyTodo(participant3.getId(), today, readTodo.getId()));

        // when
        challengeTodoReminderNotificationService.createPendingNotificationsForIncompleteTodos(today);

        // then
        List<ChallengeTodoReminderNotification> notifications = challengeTodoReminderNotificationRepository.findAll();
        assertThat(notifications)
                .extracting(
                        ChallengeTodoReminderNotification::getMemberId,
                        ChallengeTodoReminderNotification::getChallengeId,
                        ChallengeTodoReminderNotification::getChallengeName,
                        ChallengeTodoReminderNotification::getStatus,
                        ChallengeTodoReminderNotification::getAttempts
                )
                .containsExactlyInAnyOrder(
                        tuple(
                                incompleteMember.getId(),
                                challenge.getId(),
                                challenge.getName(),
                                NotificationStatus.PENDING,
                                0
                        ),
                        tuple(
                                anotherIncompleteMember.getId(),
                                challenge.getId(),
                                challenge.getName(),
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
                ChallengeTodoReminderNotification.createPending(member1.getId(), challenge.getId(), challenge.getName()));

        // when
        challengeTodoReminderNotificationService.createPendingNotificationsForIncompleteTodos(today);

        // then
        List<ChallengeTodoReminderNotification> notifications = challengeTodoReminderNotificationRepository.findAll();
        assertThat(notifications).hasSize(2);
        assertThat(notifications)
                .extracting(ChallengeTodoReminderNotification::getMemberId)
                .containsExactlyInAnyOrder(member1.getId(), member2.getId());
    }
}
