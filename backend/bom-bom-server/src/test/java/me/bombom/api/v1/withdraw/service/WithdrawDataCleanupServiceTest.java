package me.bombom.api.v1.withdraw.service;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDateTime;
import java.util.List;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.article.domain.ArticleArrivalNotification;
import me.bombom.api.v1.article.domain.ArticleArrivalNotificationFailed;
import me.bombom.api.v1.article.domain.ArticleReadHistory;
import me.bombom.api.v1.article.repository.ArticleArrivalNotificationFailedRepository;
import me.bombom.api.v1.article.repository.ArticleArrivalNotificationRepository;
import me.bombom.api.v1.article.repository.ArticleReadHistoryRepository;
import me.bombom.api.v1.badge.domain.StreakBadge;
import me.bombom.api.v1.badge.repository.BadgeRepository;
import me.bombom.api.v1.challenge.domain.ChallengeComment;
import me.bombom.api.v1.challenge.domain.ChallengeCommentLike;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import me.bombom.api.v1.challenge.domain.ChallengeReview;
import me.bombom.api.v1.challenge.repository.ChallengeCommentLikeRepository;
import me.bombom.api.v1.challenge.repository.ChallengeCommentRepository;
import me.bombom.api.v1.challenge.repository.ChallengeParticipantRepository;
import me.bombom.api.v1.challenge.repository.ChallengeReviewRepository;
import me.bombom.api.v1.coupon.domain.CouponIssue;
import me.bombom.api.v1.coupon.repository.CouponIssueRepository;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.domain.MemberFcmToken;
import me.bombom.api.v1.member.repository.MemberFcmTokenRepository;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.reading.domain.MemberReadTokenBucket;
import me.bombom.api.v1.reading.repository.MemberReadTokenBucketRepository;
import me.bombom.support.integration.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
class WithdrawDataCleanupServiceTest {

    @Autowired
    private WithdrawDataCleanupService withdrawDataCleanupService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BadgeRepository badgeRepository;

    @Autowired
    private CouponIssueRepository couponIssueRepository;

    @Autowired
    private ChallengeParticipantRepository challengeParticipantRepository;

    @Autowired
    private ChallengeCommentRepository challengeCommentRepository;

    @Autowired
    private ChallengeCommentLikeRepository challengeCommentLikeRepository;

    @Autowired
    private ChallengeReviewRepository challengeReviewRepository;

    @Autowired
    private MemberReadTokenBucketRepository memberReadTokenBucketRepository;

    @Autowired
    private ArticleReadHistoryRepository articleReadHistoryRepository;

    @Autowired
    private MemberFcmTokenRepository memberFcmTokenRepository;

    @Autowired
    private ArticleArrivalNotificationRepository articleArrivalNotificationRepository;

    @Autowired
    private ArticleArrivalNotificationFailedRepository articleArrivalNotificationFailedRepository;

    @Test
    void 탈퇴_회원의_모든_도메인_데이터가_삭제된다() {
        // given
        Member member = memberRepository.save(TestFixture.uniqueMemberFixture());
        Long memberId = member.getId();

        badgeRepository.save(StreakBadge.builder().memberId(memberId).streakDayCount(3).build());
        couponIssueRepository.save(CouponIssue.of(memberId, "쿠폰", "https://image"));
        challengeReviewRepository.save(ChallengeReview.builder()
                .memberId(memberId)
                .challengeId(1L)
                .comment("좋았어요")
                .isPrivate(false)
                .build());
        memberReadTokenBucketRepository.save(MemberReadTokenBucket.builder()
                .memberId(memberId)
                .tokens(5)
                .updatedAt(LocalDateTime.now())
                .build());
        articleReadHistoryRepository.save(ArticleReadHistory.builder()
                .memberId(memberId)
                .articleId(1L)
                .newsletterId(1L)
                .categoryId(1L)
                .readAt(LocalDateTime.now())
                .build());
        memberFcmTokenRepository.save(MemberFcmToken.builder()
                .memberId(memberId)
                .deviceUuid("device-uuid")
                .fcmToken("fcm-token")
                .isNotificationEnabled(true)
                .build());
        articleArrivalNotificationRepository.save(ArticleArrivalNotification.builder()
                .memberId(memberId)
                .articleId(1L)
                .newsletterName("뉴스레터")
                .articleTitle("제목")
                .build());
        articleArrivalNotificationFailedRepository.save(ArticleArrivalNotificationFailed.builder()
                .originalNotificationId(1L)
                .memberId(memberId)
                .articleId(1L)
                .newsletterName("뉴스레터")
                .articleTitle("제목")
                .finalAttempts(3)
                .failedAt(LocalDateTime.now())
                .build());

        ChallengeParticipant participant = challengeParticipantRepository.save(ChallengeParticipant.builder()
                .challengeId(1L)
                .memberId(memberId)
                .build());
        ChallengeComment comment = challengeCommentRepository.save(ChallengeComment.builder()
                .newsletterId(1L)
                .participantId(participant.getId())
                .articleTitle("제목")
                .comment("댓글")
                .build());
        challengeCommentLikeRepository.save(ChallengeCommentLike.builder()
                .participantId(participant.getId())
                .commentId(comment.getId())
                .build());

        // when
        withdrawDataCleanupService.cleanupByMemberId(memberId);

        // then
        Long participantId = participant.getId();
        Long commentId = comment.getId();
        assertSoftly(softly -> {
            softly.assertThat(badgeRepository.countByMemberId(memberId)).isZero();
            softly.assertThat(couponIssueRepository.findByMemberId(memberId)).isEmpty();
            softly.assertThat(challengeParticipantRepository.countByMemberId(memberId)).isZero();
            softly.assertThat(challengeReviewRepository.findByChallengeIdAndMemberId(1L, memberId)).isEmpty();
            softly.assertThat(challengeCommentRepository.findIdsByParticipantIdIn(List.of(participantId))).isEmpty();
            softly.assertThat(challengeCommentLikeRepository.existsByParticipantIdAndCommentId(participantId, commentId))
                    .isFalse();
            softly.assertThat(memberReadTokenBucketRepository.findById(memberId)).isEmpty();
            softly.assertThat(articleReadHistoryRepository.countByMemberId(memberId)).isZero();
            softly.assertThat(memberFcmTokenRepository.count()).isZero();
            softly.assertThat(articleArrivalNotificationRepository.count()).isZero();
            softly.assertThat(articleArrivalNotificationFailedRepository.count()).isZero();
        });
    }
}
