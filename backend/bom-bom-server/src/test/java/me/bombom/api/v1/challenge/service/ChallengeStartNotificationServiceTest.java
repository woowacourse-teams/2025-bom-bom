package me.bombom.api.v1.challenge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.time.LocalDate;
import java.util.List;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.challenge.domain.Challenge;
import me.bombom.api.v1.challenge.domain.NotificationStatus;
import me.bombom.api.v1.challenge.domain.notification.ChallengeStartNotification;
import me.bombom.api.v1.challenge.repository.ChallengeParticipantRepository;
import me.bombom.api.v1.challenge.repository.ChallengeRepository;
import me.bombom.api.v1.challenge.repository.ChallengeStartNotificationRepository;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.newsletter.domain.NewsletterGroup;
import me.bombom.api.v1.newsletter.repository.NewsletterGroupRepository;
import me.bombom.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
class ChallengeStartNotificationServiceTest {

    @Autowired
    private ChallengeStartNotificationService challengeStartNotificationService;

    @Autowired
    private ChallengeStartNotificationRepository challengeStartNotificationRepository;

    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private ChallengeParticipantRepository challengeParticipantRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private NewsletterGroupRepository newsletterGroupRepository;

    @BeforeEach
    void setUp() {
        challengeStartNotificationRepository.deleteAllInBatch();
        challengeParticipantRepository.deleteAllInBatch();
        challengeRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
        newsletterGroupRepository.deleteAllInBatch();
    }

    @Test
    void 시작일_챌린지_참여자에게_PENDING_알림을_생성한다() {
        // given
        LocalDate today = LocalDate.now();

        NewsletterGroup group = newsletterGroupRepository.save(TestFixture.createNewsletterGroup("group"));
        Challenge todayChallenge = challengeRepository.save(
                TestFixture.createChallenge("오늘 시작", today, today.plusDays(5), 6, group.getId()));
        Challenge tomorrowChallenge = challengeRepository.save(
                TestFixture.createChallenge("내일 시작", today.plusDays(1), today.plusDays(6), 6, group.getId()));

        Member member1 = memberRepository.save(TestFixture.createUniqueMember("m1", "p1"));
        Member member2 = memberRepository.save(TestFixture.createUniqueMember("m2", "p2"));
        Member member3 = memberRepository.save(TestFixture.createUniqueMember("m3", "p3"));

        challengeParticipantRepository.save(TestFixture.createChallengeParticipant(todayChallenge.getId(), member1.getId(), 0));
        challengeParticipantRepository.save(TestFixture.createChallengeParticipant(todayChallenge.getId(), member2.getId(), 0));
        challengeParticipantRepository.save(
                TestFixture.createChallengeParticipant(tomorrowChallenge.getId(), member3.getId(), 0));

        // when
        challengeStartNotificationService.createPendingNotificationsForStartingChallenges(today);

        // then
        List<ChallengeStartNotification> notifications = challengeStartNotificationRepository.findAll();
        assertThat(notifications)
                .extracting(
                        ChallengeStartNotification::getMemberId,
                        ChallengeStartNotification::getChallengeId,
                        ChallengeStartNotification::getChallengeName,
                        ChallengeStartNotification::getStatus,
                        ChallengeStartNotification::getAttempts,
                        ChallengeStartNotification::getNextRetryAt,
                        ChallengeStartNotification::getLastError
                )
                .containsExactlyInAnyOrder(
                        tuple(
                                member1.getId(),
                                todayChallenge.getId(),
                                todayChallenge.getName(),
                                NotificationStatus.PENDING,
                                0,
                                null,
                                null
                        ),
                        tuple(
                                member2.getId(),
                                todayChallenge.getId(),
                                todayChallenge.getName(),
                                NotificationStatus.PENDING,
                                0,
                                null,
                                null
                        )
                );
    }

    @Test
    void 이미_저장된_알림은_중복_생성하지_않는다() {
        // given
        LocalDate today = LocalDate.now();

        NewsletterGroup group = newsletterGroupRepository.save(TestFixture.createNewsletterGroup("group"));
        Challenge challenge = challengeRepository.save(
                TestFixture.createChallenge("오늘 시작", today, today.plusDays(5), 6, group.getId()));

        Member member1 = memberRepository.save(TestFixture.createUniqueMember("m1", "p1"));
        Member member2 = memberRepository.save(TestFixture.createUniqueMember("m2", "p2"));

        challengeParticipantRepository.save(TestFixture.createChallengeParticipant(challenge.getId(), member1.getId(), 0));
        challengeParticipantRepository.save(TestFixture.createChallengeParticipant(challenge.getId(), member2.getId(), 0));

        challengeStartNotificationRepository.save(
                ChallengeStartNotification.createPending(member1.getId(), challenge.getId(), challenge.getName()));

        // when
        challengeStartNotificationService.createPendingNotificationsForStartingChallenges(today);

        // then
        List<ChallengeStartNotification> notifications = challengeStartNotificationRepository.findAll();
        assertThat(notifications).hasSize(2);
        assertThat(notifications)
                .extracting(ChallengeStartNotification::getMemberId)
                .containsExactlyInAnyOrder(member1.getId(), member2.getId());
    }
}
