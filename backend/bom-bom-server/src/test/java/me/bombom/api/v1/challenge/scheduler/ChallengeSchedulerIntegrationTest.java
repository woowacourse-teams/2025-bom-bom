package me.bombom.api.v1.challenge.scheduler;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDate;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.challenge.domain.Challenge;
import me.bombom.api.v1.challenge.domain.ChallengeTeam;
import me.bombom.api.v1.challenge.domain.ChallengeTodoType;
import me.bombom.api.v1.challenge.domain.notification.ChallengeStartNotification;
import me.bombom.api.v1.challenge.domain.notification.ChallengeTodoReminderNotification;
import me.bombom.api.v1.challenge.domain.notification.ChallengeTodoReminderPhase;
import me.bombom.api.v1.challenge.domain.notification.NotificationStatus;
import me.bombom.api.v1.challenge.repository.ChallengeParticipantRepository;
import me.bombom.api.v1.challenge.repository.ChallengeRepository;
import me.bombom.api.v1.challenge.repository.ChallengeStartNotificationRepository;
import me.bombom.api.v1.challenge.repository.ChallengeTeamRepository;
import me.bombom.api.v1.challenge.repository.ChallengeTodoReminderNotificationRepository;
import me.bombom.api.v1.challenge.repository.ChallengeTodoRepository;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.newsletter.domain.NewsletterGroup;
import me.bombom.api.v1.newsletter.repository.NewsletterGroupRepository;
import me.bombom.support.integration.IntegrationTest;
import me.bombom.support.time.MutableClock;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
class ChallengeSchedulerIntegrationTest {

    @Autowired
    private ChallengeScheduler challengeScheduler;

    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private ChallengeParticipantRepository challengeParticipantRepository;

    @Autowired
    private ChallengeTodoRepository challengeTodoRepository;

    @Autowired
    private ChallengeStartNotificationRepository challengeStartNotificationRepository;

    @Autowired
    private ChallengeTodoReminderNotificationRepository challengeTodoReminderNotificationRepository;

    @Autowired
    private ChallengeTeamRepository challengeTeamRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private NewsletterGroupRepository newsletterGroupRepository;

    @Autowired
    private MutableClock clock;

    @Test
    void 시작_알림_스케줄러는_오늘_시작하는_챌린지_참여자에게_PENDING_알림을_생성한다() {
        // given
        LocalDate today = LocalDate.of(2026, 1, 5);
        clock.setDate(today);
        NewsletterGroup group = newsletterGroupRepository.save(TestFixture.createNewsletterGroup("시작알림그룹"));
        Challenge challenge = challengeRepository.save(
                TestFixture.createChallenge("오늘 시작", today, today.plusDays(14), 15, group.getId()));
        Member member = memberRepository.save(TestFixture.createUniqueMember("시작알림회원", "start-notification"));
        challengeParticipantRepository.save(TestFixture.createChallengeParticipant(challenge.getId(), member.getId(), 0));

        // when
        challengeScheduler.createChallengeStartNotifications();

        // then
        ChallengeStartNotification notification = challengeStartNotificationRepository.findAll().getFirst();
        assertSoftly(softly -> {
            softly.assertThat(notification.getMemberId()).isEqualTo(member.getId());
            softly.assertThat(notification.getChallengeId()).isEqualTo(challenge.getId());
            softly.assertThat(notification.getChallengeName()).isEqualTo(challenge.getName());
            softly.assertThat(notification.getStatus()).isEqualTo(NotificationStatus.PENDING);
            softly.assertThat(notification.getAttempts()).isZero();
        });
    }

    @Test
    void TODO_리마인더_스케줄러는_미완료_참여자에게_phase별_PENDING_알림을_생성한다() {
        // given
        LocalDate today = LocalDate.of(2026, 1, 7);
        clock.setDate(today);
        NewsletterGroup group = newsletterGroupRepository.save(TestFixture.createNewsletterGroup("리마인더그룹"));
        Challenge challenge = challengeRepository.save(
                TestFixture.createChallenge("진행 챌린지", today.minusDays(2), today.plusDays(12), 15, group.getId()));
        Member member = memberRepository.save(TestFixture.createUniqueMember("리마인더회원", "todo-reminder"));
        challengeParticipantRepository.save(TestFixture.createChallengeParticipant(challenge.getId(), member.getId(), 1));
        challengeTodoRepository.save(TestFixture.createChallengeTodo(challenge.getId(), ChallengeTodoType.READ));

        // when
        challengeScheduler.createChallengeTodoReminderNotificationsFirst();
        challengeScheduler.createChallengeTodoReminderNotificationsSecond();

        // then
        assertSoftly(softly -> {
            softly.assertThat(challengeTodoReminderNotificationRepository.findAll())
                    .extracting(ChallengeTodoReminderNotification::getPhase)
                    .containsExactlyInAnyOrder(ChallengeTodoReminderPhase.FIRST, ChallengeTodoReminderPhase.SECOND);
            softly.assertThat(challengeTodoReminderNotificationRepository.findAll())
                    .allSatisfy(notification -> {
                        softly.assertThat(notification.getMemberId()).isEqualTo(member.getId());
                        softly.assertThat(notification.getStatus()).isEqualTo(NotificationStatus.PENDING);
                    });
        });
    }

    @Test
    void 팀_진행률_초기화_스케줄러는_진행중인_챌린지_팀의_진행률을_0으로_변경한다() {
        // given
        LocalDate today = LocalDate.of(2026, 1, 7);
        clock.setDate(today);
        NewsletterGroup group = newsletterGroupRepository.save(TestFixture.createNewsletterGroup("팀그룹"));
        Challenge challenge = challengeRepository.save(
                TestFixture.createChallenge("진행 챌린지", today.minusDays(2), today.plusDays(12), 15, group.getId()));
        ChallengeTeam team = challengeTeamRepository.save(TestFixture.createChallengeTeam(challenge.getId(), 80));

        // when
        challengeScheduler.resetTeamProgress();

        // then
        ChallengeTeam updatedTeam = challengeTeamRepository.findById(team.getId()).orElseThrow();
        assertSoftly(softly -> {
            softly.assertThat(updatedTeam.getProgress()).isZero();
            softly.assertThat(updatedTeam.getChallengeId()).isEqualTo(challenge.getId());
        });
    }

    @Test
    void 종료_챌린지_처리_스케줄러는_뱃지_발급_대상_챌린지를_처리완료로_표시한다() {
        // given
        LocalDate today = LocalDate.of(2026, 1, 20);
        clock.setDate(today);
        NewsletterGroup group = newsletterGroupRepository.save(TestFixture.createNewsletterGroup("종료그룹"));
        Challenge endedChallenge = challengeRepository.save(
                TestFixture.createChallenge("종료 챌린지", today.minusDays(20), today.minusDays(1), 20, group.getId()));

        // when
        challengeScheduler.processEndedChallenges();

        // then
        Challenge updatedChallenge = challengeRepository.findById(endedChallenge.getId()).orElseThrow();
        assertSoftly(softly -> {
            softly.assertThat(updatedChallenge.isBadgeIssued()).isTrue();
            softly.assertThat(updatedChallenge.getEndDate()).isBefore(today);
        });
    }
}
