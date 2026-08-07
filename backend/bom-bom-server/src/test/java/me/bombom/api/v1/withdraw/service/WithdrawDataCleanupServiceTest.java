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
import me.bombom.api.v1.challenge.domain.ChallengeCommentReply;
import me.bombom.api.v1.challenge.domain.ChallengeCommentLike;
import me.bombom.api.v1.challenge.domain.ChallengeDailyGuideComment;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import me.bombom.api.v1.challenge.domain.ChallengeReview;
import me.bombom.api.v1.challenge.dto.request.CreateCommentReplyRequest;
import me.bombom.api.v1.challenge.repository.ChallengeCommentLikeRepository;
import me.bombom.api.v1.challenge.repository.ChallengeCommentReplyRepository;
import me.bombom.api.v1.challenge.repository.ChallengeCommentRepository;
import me.bombom.api.v1.challenge.repository.ChallengeDailyGuideCommentRepository;
import me.bombom.api.v1.challenge.repository.ChallengeParticipantRepository;
import me.bombom.api.v1.challenge.repository.ChallengeReviewRepository;
import me.bombom.api.v1.challenge.service.ChallengeCommentReplyService;
import me.bombom.api.v1.challenge.service.ChallengeCommentService;
import me.bombom.api.v1.coupon.domain.CouponIssue;
import me.bombom.api.v1.coupon.repository.CouponIssueRepository;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.domain.MemberFcmToken;
import me.bombom.api.v1.member.domain.MemberNotificationSetting;
import me.bombom.api.v1.member.repository.MemberFcmTokenRepository;
import me.bombom.api.v1.member.repository.MemberNotificationSettingRepository;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailSentContent;
import me.bombom.api.v1.nativenewsletter.maeilmail.repository.MaeilMailSentContentRepository;
import me.bombom.api.v1.reading.domain.MemberReadTokenBucket;
import me.bombom.api.v1.reading.repository.MemberReadTokenBucketRepository;
import me.bombom.api.v1.subscribe.domain.NewsletterSubscriptionCount;
import me.bombom.api.v1.subscribe.domain.Subscribe;
import me.bombom.api.v1.subscribe.repository.NewsletterSubscriptionCountRepository;
import me.bombom.api.v1.subscribe.repository.SubscribeRepository;
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
    private ChallengeCommentReplyRepository challengeCommentReplyRepository;

    @Autowired
    private ChallengeReviewRepository challengeReviewRepository;

    @Autowired
    private ChallengeCommentService challengeCommentService;

    @Autowired
    private ChallengeCommentReplyService challengeCommentReplyService;

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

    @Autowired
    private ChallengeDailyGuideCommentRepository challengeDailyGuideCommentRepository;

    @Autowired
    private MaeilMailSentContentRepository maeilMailSentContentRepository;

    @Autowired
    private MemberNotificationSettingRepository memberNotificationSettingRepository;

    @Autowired
    private SubscribeRepository subscribeRepository;

    @Autowired
    private NewsletterSubscriptionCountRepository newsletterSubscriptionCountRepository;

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
        ChallengeCommentReply reply = challengeCommentReplyRepository.save(ChallengeCommentReply.builder()
                .commentId(comment.getId())
                .participantId(participant.getId())
                .reply("답글")
                .build());
        challengeDailyGuideCommentRepository.save(ChallengeDailyGuideComment.builder()
                .guideId(1L)
                .participantId(participant.getId())
                .content("데일리 가이드 댓글")
                .build());
        maeilMailSentContentRepository.save(MaeilMailSentContent.builder()
                .memberId(memberId)
                .topicId(1L)
                .contentId(1L)
                .build());
        memberNotificationSettingRepository.save(MemberNotificationSetting.builder()
                .memberId(memberId)
                .category("ARTICLE_ARRIVAL")
                .isEnabled(true)
                .build());

        // when
        boolean isSucceeded = withdrawDataCleanupService.cleanupByMemberId(memberId);

        // then
        Long participantId = participant.getId();
        Long commentId = comment.getId();
        assertSoftly(softly -> {
            softly.assertThat(isSucceeded).isTrue();
            softly.assertThat(badgeRepository.countByMemberId(memberId)).isZero();
            softly.assertThat(couponIssueRepository.findByMemberId(memberId)).isEmpty();
            softly.assertThat(challengeParticipantRepository.countByMemberId(memberId)).isEqualTo(1);
            softly.assertThat(challengeReviewRepository.findByChallengeIdAndMemberId(1L, memberId)).isEmpty();
            softly.assertThat(challengeCommentRepository.findIdsByParticipantIdIn(List.of(participantId)))
                    .containsExactly(commentId);
            softly.assertThat(challengeCommentReplyRepository.findById(reply.getId())).isPresent();
            softly.assertThat(challengeCommentLikeRepository.existsByParticipantIdAndCommentId(participantId, commentId))
                    .isFalse();
            softly.assertThat(memberReadTokenBucketRepository.findById(memberId)).isEmpty();
            softly.assertThat(articleReadHistoryRepository.countByMemberId(memberId)).isZero();
            softly.assertThat(memberFcmTokenRepository.count()).isZero();
            softly.assertThat(articleArrivalNotificationRepository.count()).isZero();
            softly.assertThat(articleArrivalNotificationFailedRepository.count()).isZero();
            softly.assertThat(challengeDailyGuideCommentRepository.count()).isZero();
            softly.assertThat(maeilMailSentContentRepository.count()).isZero();
            softly.assertThat(memberNotificationSettingRepository.count()).isZero();
        });
    }

    @Test
    void 탈퇴_회원이_타인_댓글에_남긴_좋아요는_삭제하고_답글은_보존한다() {
        // given
        Member withdrawer = memberRepository.save(TestFixture.uniqueMemberFixture());
        Member other = memberRepository.save(TestFixture.uniqueMemberFixture());

        ChallengeParticipant otherParticipant = challengeParticipantRepository.save(ChallengeParticipant.builder()
                .challengeId(1L)
                .memberId(other.getId())
                .build());
        ChallengeComment othersComment = challengeCommentRepository.save(ChallengeComment.builder()
                .newsletterId(1L)
                .participantId(otherParticipant.getId())
                .articleTitle("제목")
                .comment("타인의 댓글")
                .build());

        ChallengeParticipant withdrawerParticipant = challengeParticipantRepository.save(ChallengeParticipant.builder()
                .challengeId(1L)
                .memberId(withdrawer.getId())
                .build());
        // 실제 좋아요/답글 생성 플로우로 대상 댓글의 카운터를 1씩 증가시킨다.
        challengeCommentService.addChallengeCommentLike(withdrawer.getId(), 1L, othersComment.getId());
        challengeCommentReplyService.createCommentReply(
                1L, othersComment.getId(), withdrawer.getId(), new CreateCommentReplyRequest("답글", false));

        // when
        boolean isSucceeded = withdrawDataCleanupService.cleanupByMemberId(withdrawer.getId());

        // then
        ChallengeComment updated = challengeCommentRepository.findById(othersComment.getId()).orElseThrow();
        assertSoftly(softly -> {
            softly.assertThat(isSucceeded).isTrue();
            softly.assertThat(updated.getLikeCount()).isEqualTo(1);
            softly.assertThat(updated.getReplyCount()).isEqualTo(1);
            softly.assertThat(challengeCommentLikeRepository.existsByParticipantIdAndCommentId(
                    withdrawerParticipant.getId(), othersComment.getId())).isFalse();
            softly.assertThat(challengeCommentReplyRepository.count()).isEqualTo(1);
        });
    }

    @Test
    void 회원_삭제_이후_탈퇴_정리가_실행되어도_구독자_수는_전달받은_생년월일로_감소한다() {
        // given
        LocalDateTime birthDateTime = LocalDateTime.of(2001, 1, 1, 0, 0);
        Member member = memberRepository.save(Member.builder()
                .provider("apple")
                .providerId("withdraw-subscription")
                .email("withdraw-subscription@bombom.news")
                .nickname("withdraw-subscription")
                .gender(me.bombom.api.v1.member.enums.Gender.FEMALE)
                .birthDate(birthDateTime.toLocalDate())
                .roleId(1L)
                .build());
        Long memberId = member.getId();

        subscribeRepository.save(Subscribe.builder()
                .memberId(memberId)
                .newsletterId(1L)
                .build());
        newsletterSubscriptionCountRepository.save(NewsletterSubscriptionCount.builder()
                .newsletterId(1L)
                .total(1)
                .age20s(1)
                .build());

        memberRepository.delete(member);

        // when
        boolean isSucceeded = withdrawDataCleanupService.cleanupByMemberId(memberId, birthDateTime.toLocalDate());

        // then
        NewsletterSubscriptionCount count = newsletterSubscriptionCountRepository.findAll().getFirst();
        assertSoftly(softly -> {
            softly.assertThat(isSucceeded).isTrue();
            softly.assertThat(subscribeRepository.countByMemberId(memberId)).isZero();
            softly.assertThat(count.getTotal()).isZero();
            softly.assertThat(count.getAge20s()).isZero();
        });
    }
}
