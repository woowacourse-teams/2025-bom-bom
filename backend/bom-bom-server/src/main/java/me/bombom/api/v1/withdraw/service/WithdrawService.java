package me.bombom.api.v1.withdraw.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.article.repository.ArticleReadHistoryRepository;
import me.bombom.api.v1.badge.repository.BadgeRepository;
import me.bombom.api.v1.bookmark.repository.BookmarkRepository;
import me.bombom.api.v1.challenge.repository.ChallengeParticipantRepository;
import me.bombom.api.v1.highlight.repository.HighlightRepository;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.reading.domain.ContinueReadingRealtime;
import me.bombom.api.v1.reading.repository.ContinueReadingRealtimeRepository;
import me.bombom.api.v1.subscribe.domain.AgeGroup;
import me.bombom.api.v1.subscribe.repository.SubscribeRepository;
import me.bombom.api.v1.withdraw.domain.WithdrawnMember;
import me.bombom.api.v1.withdraw.repository.WithdrawnMemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WithdrawService {

    private static final int EXPIRATION_DAYS = 90;

    private final WithdrawnMemberRepository withdrawnMemberRepository;
    private final HighlightRepository highlightRepository;
    private final ContinueReadingRealtimeRepository continueReadingRepository;
    private final BookmarkRepository bookmarkRepository;
    private final ArticleReadHistoryRepository articleReadHistoryRepository;
    private final SubscribeRepository subscribeRepository;
    private final ChallengeParticipantRepository challengeParticipantRepository;
    private final BadgeRepository badgeRepository;
    private final Clock clock;

    @Transactional
    public void migrateDeletedMember(Member member) {
        withdrawnMemberRepository.save(createWithdrawnMember(member));
        log.info("탈퇴한 회원 정보 이전 성공. memberId:{}", member.getId());
    }

    @Transactional
    public void deleteExpiredWithdrawnMembers() {
        withdrawnMemberRepository.bulkDeleteAllExpired(LocalDate.now(clock));
        log.info("만료된 회원 정보 삭제 성공");
    }

    public List<Long> findActiveWithdrawnMemberIds() {
        return withdrawnMemberRepository.findAllMemberIds();
    }

    private WithdrawnMember createWithdrawnMember(Member member) {
        Long memberId = member.getId();
        LocalDate today = LocalDate.now(clock);
        return WithdrawnMember.builder()
                .memberId(memberId)
                .gender(member.getGender())
                .ageGroup(getAgeGroup(member.getBirthDate()))
                .provider(member.getProvider())
                .joinedDate(member.getCreatedAt().toLocalDate())
                .deletedDate(today)
                .expireDate(today.plusDays(EXPIRATION_DAYS))
                .lastReadDate(getLastReadDate(memberId))
                .continueReading(getContinueReadingRealtime(memberId))
                .bookmarkedCount(getBookmarkedCount(memberId))
                .highlightCount(getHighlightCount(memberId))
                .totalReadCount(articleReadHistoryRepository.countByMemberId(memberId))
                .subscribeCount(subscribeRepository.countByMemberId(memberId))
                .challengeCount(challengeParticipantRepository.countByMemberId(memberId))
                .badgeCount(badgeRepository.countByMemberId(memberId))
                .build();
    }

    private AgeGroup getAgeGroup(LocalDate birthDate) {
        if (birthDate == null) {
            return null;
        }
        return AgeGroup.fromBirthYear(LocalDate.now(clock).getYear(), birthDate.getYear());
    }

    private LocalDate getLastReadDate(Long memberId) {
        return articleReadHistoryRepository.findLastReadAtByMemberId(memberId)
                .map(LocalDateTime::toLocalDate)
                .orElse(null);
    }

    private int getContinueReadingRealtime(Long memberId) {
        Optional<ContinueReadingRealtime> continueReading = continueReadingRepository.findByMemberId(memberId);
        if (continueReading.isEmpty()) {
            log.warn("탈퇴한 회원의 연속 읽기 정보를 찾을 수 없습니다. memberId:{}", memberId);
            return 0;
        }
        return continueReading.get().getDayCount();
    }

    private int getBookmarkedCount(Long memberId) {
        return (int) bookmarkRepository.countByMemberId(memberId);
    }

    private int getHighlightCount(Long memberId) {
        return highlightRepository.countByMemberId(memberId);
    }
}
