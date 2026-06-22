package me.bombom.api.v1.challenge.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.challenge.domain.Challenge;
import me.bombom.api.v1.challenge.repository.ChallengeRepository;
import me.bombom.api.v1.newsletter.domain.NewsletterGroup;
import me.bombom.api.v1.newsletter.repository.NewsletterGroupRepository;
import me.bombom.support.integration.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
class ChallengeServiceTest {

    @Autowired
    private ChallengeService challengeService;

    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private NewsletterGroupRepository newsletterGroupRepository;

    @Test
    void 토요일에_진행_중인_챌린지_조회_시_빈_리스트를_반환한다() {
        // given
        LocalDate saturday = LocalDate.of(2025, 3, 15);
        saveChallenge("진행 중 챌린지", saturday.minusDays(1), saturday.plusDays(1), 3);

        // when
        List<Challenge> result = challengeService.getOngoingChallenges(saturday);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void 일요일에_진행_중인_챌린지_조회_시_빈_리스트를_반환한다() {
        // given
        LocalDate sunday = LocalDate.of(2025, 3, 16);
        saveChallenge("진행 중 챌린지", sunday.minusDays(1), sunday.plusDays(1), 3);

        // when
        List<Challenge> result = challengeService.getOngoingChallenges(sunday);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void 평일에_진행_중인_챌린지를_조회한다() {
        // given
        LocalDate monday = LocalDate.of(2025, 3, 17);
        Challenge ongoing = saveChallenge("진행 중 챌린지", monday.minusDays(1), monday.plusDays(1), 3);
        saveChallenge("시작 전 챌린지", monday.plusDays(1), monday.plusDays(5), 5);
        saveChallenge("종료된 챌린지", monday.minusDays(5), monday.minusDays(1), 5);

        // when
        List<Challenge> result = challengeService.getOngoingChallenges(monday);

        // then
        assertThat(result)
                .extracting(Challenge::getId)
                .containsExactly(ongoing.getId());
    }

    @Test
    void 종료됐고_뱃지를_아직_발급하지_않은_장기_챌린지만_조회한다() {
        // given
        LocalDate today = LocalDate.of(2025, 3, 17);
        Challenge pending = saveChallenge("뱃지 발급 대상", today.minusDays(30), today.minusDays(1), 20);
        saveChallenge("짧은 챌린지", today.minusDays(10), today.minusDays(1), 10);
        saveChallenge("진행 중 챌린지", today.minusDays(1), today.plusDays(10), 20);

        Challenge issued = saveChallenge("이미 발급된 챌린지", today.minusDays(30), today.minusDays(1), 20);
        issued.markBadgeAsIssued();
        challengeRepository.save(issued);

        // when
        List<Challenge> result = challengeService.getEndedChallengesPendingBadge(today);

        // then
        assertThat(result)
                .extracting(Challenge::getId)
                .containsExactly(pending.getId());
    }

    private Challenge saveChallenge(String name, LocalDate startDate, LocalDate endDate, int totalDays) {
        NewsletterGroup group = newsletterGroupRepository.save(TestFixture.createNewsletterGroup(name + " 그룹"));
        return challengeRepository.save(TestFixture.createChallenge(
                name,
                startDate,
                endDate,
                totalDays,
                group.getId()
        ));
    }
}
