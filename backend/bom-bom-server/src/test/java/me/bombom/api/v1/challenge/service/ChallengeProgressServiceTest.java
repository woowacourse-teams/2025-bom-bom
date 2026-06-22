package me.bombom.api.v1.challenge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDate;
import java.util.List;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.challenge.domain.Challenge;
import me.bombom.api.v1.challenge.domain.ChallengeDailyResult;
import me.bombom.api.v1.challenge.domain.ChallengeDailyStatus;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import me.bombom.api.v1.challenge.domain.ChallengeTeam;
import me.bombom.api.v1.challenge.dto.response.ChallengeStreakResponse;
import me.bombom.api.v1.challenge.repository.ChallengeDailyResultRepository;
import me.bombom.api.v1.challenge.repository.ChallengeParticipantRepository;
import me.bombom.api.v1.challenge.repository.ChallengeRepository;
import me.bombom.api.v1.challenge.repository.ChallengeTeamRepository;
import me.bombom.api.v1.common.holiday.domain.Holiday;
import me.bombom.api.v1.common.holiday.repository.HolidayRepository;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.CServerErrorException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.common.exception.UnauthorizedException;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.newsletter.domain.NewsletterGroup;
import me.bombom.api.v1.newsletter.repository.NewsletterGroupRepository;
import me.bombom.support.integration.IntegrationTest;
import me.bombom.support.time.MutableClock;
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
    private ChallengeTeamRepository challengeTeamRepository;

    @Autowired
    private ChallengeDailyResultRepository challengeDailyResultRepository;

    @Autowired
    private NewsletterGroupRepository newsletterGroupRepository;

    @Autowired
    private HolidayRepository holidayRepository;

    @Autowired
    private MutableClock clock;

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

    @Test
    void 참여자가_아니면_내_진행상황을_조회할_수_없다() {
        // given
        Member member = saveMember("진행상황미참여회원", "progress-not-participant");
        Challenge challenge = saveChallenge(
                "Progress Not Participant",
                LocalDate.of(2026, 1, 5),
                LocalDate.of(2026, 1, 16),
                10
        );

        // when & then
        assertThatThrownBy(() -> challengeProgressService.getMemberProgress(challenge.getId(), member))
                .isInstanceOf(UnauthorizedException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.FORBIDDEN_RESOURCE);
    }

    @Test
    void 참가자는_있지만_일일_진행상황_데이터가_없으면_서버_오류를_반환한다() {
        // given
        Member member = saveMember("진행상황데이터없음회원", "progress-empty-data");
        Challenge challenge = saveChallenge(
                "Progress Empty Data",
                LocalDate.of(2026, 1, 5),
                LocalDate.of(2026, 1, 16),
                10
        );
        challengeParticipantRepository.save(TestFixture.createChallengeParticipant(
                challenge.getId(),
                member.getId(),
                1
        ));

        // when & then
        assertThatThrownBy(() -> challengeProgressService.getMemberProgress(challenge.getId(), member))
                .isInstanceOf(CServerErrorException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.INTERNAL_SERVER_ERROR);
    }

    @Test
    void 오늘_완료했지만_스트릭이_0이면_빈_스트릭을_반환한다() {
        // given
        LocalDate today = LocalDate.of(2026, 1, 5);
        clock.setDate(today);
        Challenge challenge = saveChallenge("Empty Streak", today.minusDays(1), today.plusDays(9), 10);
        Member member = saveMember("빈스트릭회원", "empty-streak");
        ChallengeParticipant participant = challengeParticipantRepository.save(ChallengeParticipant.builder()
                .challengeId(challenge.getId())
                .memberId(member.getId())
                .completedDays(1)
                .streak(0)
                .isSurvived(true)
                .build());
        saveDailyResult(participant, today, ChallengeDailyStatus.COMPLETE);

        // when
        ChallengeStreakResponse result = challengeProgressService.getMemberStreak(challenge.getId(), member, 7);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.streak()).isZero();
            softly.assertThat(result.streakDays()).isEmpty();
        });
    }

    @Test
    void 오늘_완료하지_않은_스트릭은_오늘_미참여를_포함한다() {
        // given
        LocalDate today = LocalDate.of(2026, 1, 5);
        clock.setDate(today);
        Challenge challenge = saveChallenge("Absent Streak", today.minusDays(2), today.plusDays(8), 10);
        Member member = saveMember("오늘미참여회원", "today-absent-streak");
        ChallengeParticipant participant = challengeParticipantRepository.save(ChallengeParticipant.builder()
                .challengeId(challenge.getId())
                .memberId(member.getId())
                .completedDays(1)
                .streak(2)
                .isSurvived(true)
                .build());
        saveDailyResult(participant, today.minusDays(1), ChallengeDailyStatus.COMPLETE);

        // when
        ChallengeStreakResponse result = challengeProgressService.getMemberStreak(challenge.getId(), member, 3);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.streak()).isEqualTo(2);
            softly.assertThat(result.streakDays()).hasSize(2);
            softly.assertThat(result.streakDays().getLast().date()).isEqualTo(today);
            softly.assertThat(result.streakDays().getLast().isCompleted()).isFalse();
        });
    }

    @Test
    void 다른_챌린지의_팀_진행상황은_조회할_수_없다() {
        // given
        Member member = saveMember("팀진행회원", "team-progress-member");
        Challenge challenge = saveChallenge("Team Progress", LocalDate.of(2026, 1, 5), LocalDate.of(2026, 1, 16), 10);
        Challenge otherChallenge = saveChallenge(
                "Other Team Progress",
                LocalDate.of(2026, 1, 5),
                LocalDate.of(2026, 1, 16),
                10
        );
        ChallengeTeam otherTeam = challengeTeamRepository.save(TestFixture.createChallengeTeam(otherChallenge.getId(), 0));
        challengeParticipantRepository.save(TestFixture.createChallengeParticipant(
                challenge.getId(),
                member.getId(),
                1
        ));

        // when & then
        assertThatThrownBy(() -> challengeProgressService.getTeamProgressByTeamId(
                challenge.getId(),
                otherTeam.getId(),
                member
        ))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.ENTITY_NOT_FOUND);
    }

    @Test
    void 진행중인_챌린지는_수료증을_조회할_수_없다() {
        // given
        LocalDate today = LocalDate.of(2026, 1, 5);
        clock.setDate(today);
        Challenge challenge = saveChallenge("Ongoing Certification", today, today.plusDays(9), 10);

        // when & then
        assertThatThrownBy(() -> challengeProgressService.getCertificationInfo(challenge.getId(), 1L))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.PRECONDITION_FAILED);
    }

    @Test
    void 탈락한_참가자는_수료증을_조회할_수_없다() {
        // given
        LocalDate today = LocalDate.of(2026, 1, 20);
        clock.setDate(today);
        Member member = saveMember("탈락수료증회원", "failed-certification");
        Challenge challenge = saveChallenge(
                "Failed Certification",
                LocalDate.of(2026, 1, 5),
                LocalDate.of(2026, 1, 16),
                10
        );
        challengeParticipantRepository.save(TestFixture.createChallengeParticipant(
                challenge.getId(),
                member.getId(),
                5,
                false
        ));

        // when & then
        assertThatThrownBy(() -> challengeProgressService.getCertificationInfo(challenge.getId(), member.getId()))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.PRECONDITION_FAILED);
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

    private Member saveMember(String nickname, String providerId) {
        return memberRepository.save(TestFixture.createUniqueMember(nickname, providerId));
    }

    private ChallengeDailyResult saveDailyResult(
            ChallengeParticipant participant,
            LocalDate date,
            ChallengeDailyStatus status
    ) {
        return challengeDailyResultRepository.save(TestFixture.createChallengeDailyResult(
                participant.getId(),
                date,
                status
        ));
    }
}
