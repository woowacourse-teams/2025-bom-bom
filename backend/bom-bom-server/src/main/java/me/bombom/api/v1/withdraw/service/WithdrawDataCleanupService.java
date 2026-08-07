package me.bombom.api.v1.withdraw.service;

import java.time.LocalDate;
import java.util.function.LongConsumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.article.service.ArticleService;
import me.bombom.api.v1.badge.service.BadgeService;
import me.bombom.api.v1.bookmark.service.BookmarkService;
import me.bombom.api.v1.challenge.service.ChallengeWithdrawService;
import me.bombom.api.v1.coupon.service.CouponService;
import me.bombom.api.v1.highlight.service.HighlightService;
import me.bombom.api.v1.member.service.MemberFcmTokenService;
import me.bombom.api.v1.member.service.MemberNotificationSettingService;
import me.bombom.api.v1.member.service.WarningService;
import me.bombom.api.v1.nativenewsletter.maeilmail.service.MaeilMailSubscribeService;
import me.bombom.api.v1.pet.service.PetService;
import me.bombom.api.v1.reading.service.ReadingService;
import me.bombom.api.v1.subscribe.service.SubscribeService;
import org.springframework.stereotype.Service;

/**
 * 회원 탈퇴 시 정리해야 하는 모든 도메인 데이터 삭제의 단일 소스.
 * 각 도메인 삭제는 개별 트랜잭션(REQUIRES_NEW)으로 수행되며, 하나가 실패해도 나머지는 계속 진행한다.
 * 실패로 남은 고아 데이터는 재조정 스케줄러가 동일 로직을 재실행(멱등)하여 정리한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WithdrawDataCleanupService {

    private final ArticleService articleService;
    private final BookmarkService bookmarkService;
    private final HighlightService highlightService;
    private final PetService petService;
    private final ReadingService readingService;
    private final SubscribeService subscribeService;
    private final WarningService warningService;
    private final BadgeService badgeService;
    private final CouponService couponService;
    private final ChallengeWithdrawService challengeWithdrawService;
    private final MaeilMailSubscribeService maeilMailSubscribeService;
    private final MemberFcmTokenService memberFcmTokenService;
    private final MemberNotificationSettingService memberNotificationSettingService;

    public boolean cleanupByMemberId(Long memberId) {
        return cleanupByMemberId(memberId, null);
    }

    public boolean cleanupByMemberId(Long memberId, LocalDate birthDate) {
        boolean isSucceeded = true;
        isSucceeded &= runSafely("아티클", memberId, articleService::deleteAllByMemberId);
        isSucceeded &= runSafely("북마크", memberId, bookmarkService::deleteAllByMemberId);
        isSucceeded &= runSafely("하이라이트", memberId, highlightService::deleteAllByMemberId);
        isSucceeded &= runSafely("키우기", memberId, petService::deleteByMemberId);
        isSucceeded &= runSafely("읽기", memberId, readingService::deleteAllByMemberId);
        isSucceeded &= runSafely("구독", memberId, id -> deleteSubscriptions(id, birthDate));
        isSucceeded &= runSafely("경고 설정", memberId, warningService::deleteByMemberId);
        isSucceeded &= runSafely("뱃지", memberId, badgeService::deleteAllByMemberId);
        isSucceeded &= runSafely("쿠폰", memberId, couponService::deleteAllByMemberId);
        isSucceeded &= runSafely("챌린지", memberId, challengeWithdrawService::deleteAllByMemberId);
        isSucceeded &= runSafely("매일메일", memberId, maeilMailSubscribeService::deleteAllByMemberId);
        isSucceeded &= runSafely("FCM 토큰", memberId, memberFcmTokenService::deleteAllByMemberId);
        isSucceeded &= runSafely("알림 설정", memberId, memberNotificationSettingService::deleteAllByMemberId);
        return isSucceeded;
    }

    private void deleteSubscriptions(Long memberId, LocalDate birthDate) {
        if (birthDate == null) {
            subscribeService.deleteAllByMemberId(memberId);
            return;
        }

        subscribeService.deleteAllByMemberId(memberId, birthDate);
    }

    private boolean runSafely(String domain, Long memberId, LongConsumer action) {
        try {
            action.accept(memberId);
            return true;
        } catch (Exception e) {
            log.error("회원 탈퇴 데이터 삭제 실패 - domain={}, memberId={}", domain, memberId, e);
            return false;
        }
    }
}
