package me.bombom.api.v1.badge.service;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.badge.domain.BadgeGrade;
import me.bombom.api.v1.badge.domain.RankingBadge;
import me.bombom.api.v1.badge.repository.BadgeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BadgeService {

    private static final BadgeGrade[] RANKING_BADGE_GRADES = BadgeGrade.values();

    private final BadgeRepository badgeRepository;

    @Transactional
    public void issueRankingBadges(List<Long> topRankers, LocalDate period) {
        if (topRankers.isEmpty()) {
            log.info("랭킹 뱃지 발급 대상이 없습니다.");
            return;
        }

        int issueCount = Math.min(topRankers.size(), BadgeGrade.MAX_RANKING_COUNT);
        for (int rank = 0; rank < issueCount; rank++) {
            issueRankingBadge(topRankers.get(rank), RANKING_BADGE_GRADES[rank], period);
        }
    }

    private void issueRankingBadge(Long memberId, BadgeGrade grade, LocalDate period) {
        RankingBadge badge = RankingBadge.create(memberId, grade, period);
        badgeRepository.save(badge);
        log.info("랭킹 뱃지 발급 완료 - memberId: {}, grade: {}", memberId, grade);
    }
}
