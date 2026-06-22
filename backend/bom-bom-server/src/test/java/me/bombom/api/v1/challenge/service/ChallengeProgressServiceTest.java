package me.bombom.api.v1.challenge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDate;
import java.util.List;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.challenge.domain.Challenge;
import me.bombom.api.v1.challenge.domain.ChallengeDailyResult;
import me.bombom.api.v1.challenge.domain.ChallengeDailyStatus;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import me.bombom.api.v1.challenge.repository.ChallengeDailyResultRepository;
import me.bombom.api.v1.challenge.repository.ChallengeParticipantRepository;
import me.bombom.api.v1.challenge.repository.ChallengeRepository;
import me.bombom.api.v1.common.holiday.domain.Holiday;
import me.bombom.api.v1.common.holiday.repository.HolidayRepository;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.newsletter.domain.NewsletterGroup;
import me.bombom.api.v1.newsletter.repository.NewsletterGroupRepository;
import me.bombom.support.integration.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
class ChallengeProgressServiceTest {

    @Autowired
    private ChallengeProgressService challengeProgressService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private ChallengeParticipantRepository challengeParticipantRepository;

    @Autowired
    private ChallengeDailyResultRepository challengeDailyResultRepository;

    @Autowired
    private NewsletterGroupRepository newsletterGroupRepository;

    @Autowired
    private HolidayRepository holidayRepository;

    @Test
    void 쉴드를_보유한_참가자는_결석_시_쉴드를_사용하여_생존한다() {
        // given
        LocalDate yesterday = LocalDate.of(2026, 1, 9);
        Challenge challenge = saveChallenge("Survival Challenge", yesterday.minusDays(4), yesterday.plusDays(5), 10);
        ChallengeParticipant participant = saveParticipant(challenge, 3, 1, 3, true);

        // when
        challengeProgressService.proceedDailySurvivalCheck(challenge, yesterday);

        // then
        ChallengeParticipant updatedParticipant = challengeParticipantRepository.findById(participant.getId())
                .orElseThrow();
        List<ChallengeDailyResult> results = challengeDailyResultRepository.findAll();

        assertSoftly(softly -> {
            softly.assertThat(updatedParticipant.getShield()).isZero();
            softly.assertThat(updatedParticipant.getCompletedDays()).isEqualTo(4);
            softly.assertThat(updatedParticipant.isSurvived()).isTrue();
            softly.assertThat(updatedParticipant.getStreak()).isEqualTo(3);
            softly.assertThat(results).hasSize(1);
            softly.assertThat(results.getFirst().getStatus()).isEqualTo(ChallengeDailyStatus.SHIELD);
            softly.assertThat(results.getFirst().getDate()).isEqualTo(yesterday);
        });
    }

    @Test
    void 공휴일_결석자는_일반_쉴드_대신_공휴일_쉴드로_처리한다() {
        // given
        LocalDate holidayDate = LocalDate.of(2026, 5, 5);
        holidayRepository.save(Holiday.builder()
                .date(holidayDate)
                .name("어린이날")
                .build());
        Challenge challenge = saveChallenge("Holiday Challenge", holidayDate.minusDays(4), holidayDate.plusDays(5), 10);
        ChallengeParticipant participant = saveParticipant(challenge, 3, 1, 4, true);

        // when
        challengeProgressService.proceedDailySurvivalCheck(challenge, holidayDate);

        // then
        ChallengeParticipant updatedParticipant = challengeParticipantRepository.findById(participant.getId())
                .orElseThrow();
        List<ChallengeDailyResult> results = challengeDailyResultRepository.findAll();

        assertSoftly(softly -> {
            softly.assertThat(updatedParticipant.getShield()).isEqualTo(1);
            softly.assertThat(updatedParticipant.getCompletedDays()).isEqualTo(4);
            softly.assertThat(updatedParticipant.getStreak()).isEqualTo(4);
            softly.assertThat(updatedParticipant.isSurvived()).isTrue();
            softly.assertThat(results).hasSize(1);
            softly.assertThat(results.getFirst().getStatus()).isEqualTo(ChallengeDailyStatus.HOLIDAY_SHIELD);
            softly.assertThat(results.getFirst().getDate()).isEqualTo(holidayDate);
        });
    }

    @Test
    void 쉴드가_없어도_결석_허용일_이내라면_생존한다() {
        // given
        LocalDate yesterday = LocalDate.of(2026, 1, 9);
        Challenge challenge = saveChallenge("Allowed Absence Challenge", yesterday.minusDays(4), yesterday.plusDays(5), 10);
        ChallengeParticipant participant = saveParticipant(challenge, 3, 0, 3, true);

        // when
        challengeProgressService.proceedDailySurvivalCheck(challenge, yesterday);

        // then
        ChallengeParticipant updatedParticipant = challengeParticipantRepository.findById(participant.getId())
                .orElseThrow();

        assertSoftly(softly -> {
            softly.assertThat(updatedParticipant.isSurvived()).isTrue();
            softly.assertThat(updatedParticipant.getCompletedDays()).isEqualTo(3);
            softly.assertThat(updatedParticipant.getStreak()).isZero();
        });
    }

    @Test
    void 결석_허용일을_초과하면_생존에_실패한다() {
        // given
        LocalDate yesterday = LocalDate.of(2026, 1, 9);
        Challenge challenge = saveChallenge("Failed Challenge", yesterday.minusDays(4), yesterday.plusDays(5), 10);
        ChallengeParticipant participant = saveParticipant(challenge, 2, 0, 2, true);

        // when
        challengeProgressService.proceedDailySurvivalCheck(challenge, yesterday);

        // then
        ChallengeParticipant updatedParticipant = challengeParticipantRepository.findById(participant.getId())
                .orElseThrow();
        assertThat(updatedParticipant.isSurvived()).isFalse();
    }

    @Test
    void 주말을_제외한_평일만_계산하여_생존_처리한다() {
        // given
        LocalDate friday = LocalDate.of(2024, 1, 5);
        LocalDate monday = LocalDate.of(2024, 1, 8);
        Challenge challenge = saveChallenge("Weekend Challenge", friday, friday.plusDays(13), 10);
        ChallengeParticipant participant = saveParticipant(challenge, 0, 0, 0, true);

        // when
        challengeProgressService.proceedDailySurvivalCheck(challenge, monday);

        // then
        ChallengeParticipant updatedParticipant = challengeParticipantRepository.findById(participant.getId())
                .orElseThrow();
        assertThat(updatedParticipant.isSurvived()).isTrue();
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

    private ChallengeParticipant saveParticipant(
            Challenge challenge,
            int completedDays,
            int shield,
            int streak,
            boolean isSurvived
    ) {
        var member = memberRepository.save(TestFixture.createUniqueMember("tester", "challenge-progress-test"));
        return challengeParticipantRepository.save(ChallengeParticipant.builder()
                .challengeId(challenge.getId())
                .memberId(member.getId())
                .completedDays(completedDays)
                .shield(shield)
                .streak(streak)
                .isSurvived(isSurvived)
                .build());
    }
}
