package me.bombom.api.v1.badge.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.badge.domain.BadgeGrade;
import me.bombom.api.v1.badge.domain.ChallengeBadge;
import me.bombom.api.v1.badge.domain.RankingBadge;
import me.bombom.api.v1.badge.repository.BadgeRepository;
import me.bombom.api.v1.badge.repository.ChallengeBadgeRepository;
import me.bombom.api.v1.badge.repository.RankingBadgeRepository;
import me.bombom.api.v1.challenge.domain.Challenge;
import me.bombom.api.v1.reading.dto.RankerInfo;
import me.bombom.api.v1.challenge.domain.ChallengeGrade;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BadgeService {

    private final BadgeRepository badgeRepository;
    private final ChallengeBadgeRepository challengeBadgeRepository;
    private final RankingBadgeRepository rankingBadgeRepository;

    @Transactional
    public void issueRankingBadges(List<RankerInfo> rankers, LocalDate period) {
        if (rankers.isEmpty()) {
            log.info("랭킹 뱃지 발급 대상이 없습니다.");
            return;
        }

        for (RankerInfo ranker : rankers) {
            Optional<BadgeGrade> grade = BadgeGrade.fromRankOrder(ranker.rankOrder());
            grade.ifPresent(badgeGrade -> issueRankingBadge(ranker.memberId(), badgeGrade, period));
        }
    }

    @Transactional
    public void issueChallengeBadges(Challenge challenge, List<ChallengeParticipant> participants) {
        if (participants.isEmpty()) {
            log.info("챌린지 뱃지 발급 대상이 없습니다. challengeId: {}", challenge.getId());
            return;
        }

        int totalDays = challenge.getTotalDays();
        for (ChallengeParticipant participant : participants) {
            int progress = participant.calculateProgress(totalDays);
            ChallengeGrade challengeGrade = ChallengeGrade.calculate(progress, participant.isSurvived());

            Optional<BadgeGrade> badgeGrade = challengeGrade.toBadge();
            badgeGrade.ifPresent(grade -> issueChallengeBadge(participant.getMemberId(), grade, challenge));
        }
    }

    private void issueRankingBadge(Long memberId, BadgeGrade grade, LocalDate period) {
        int year = period.getYear();
        int month = period.getMonthValue();

        if (rankingBadgeRepository.existsByMemberIdAndPeriodYearAndPeriodMonth(memberId, year, month)) {
            log.info("이미 발급된 랭킹 뱃지입니다. - memberId: {}, period: {}-{}", memberId, year, month);
            return;
        }

        RankingBadge badge = RankingBadge.builder()
                .memberId(memberId)
                .grade(grade)
                .periodYear(year)
                .periodMonth(month)
                .build();
        badgeRepository.save(badge);
        log.info("랭킹 뱃지 발급 완료 - memberId: {}, grade: {}", memberId, grade);
    }

    private void issueChallengeBadge(Long memberId, BadgeGrade grade, Challenge challenge) {
        if (challengeBadgeRepository.existsByMemberIdAndChallengeId(memberId, challenge.getId())) {
            log.info("이미 발급된 챌린지 뱃지입니다. - memberId: {}, challengeId: {}", memberId, challenge.getId());
            return;
        }
        
        ChallengeBadge badge = ChallengeBadge.builder()
                .memberId(memberId)
                .grade(grade)
                .challengeId(challenge.getId())
                .challengeName(challenge.getName())
                .challengeGeneration(challenge.getGeneration())
                .build();
        badgeRepository.save(badge);
        log.info("챌린지 뱃지 발급 완료 - memberId: {}, challengeId: {}, grade: {}", memberId, challenge.getId(), grade);
    }
}
