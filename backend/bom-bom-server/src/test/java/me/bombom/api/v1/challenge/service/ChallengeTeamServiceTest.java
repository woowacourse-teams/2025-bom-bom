package me.bombom.api.v1.challenge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.challenge.domain.Challenge;
import me.bombom.api.v1.challenge.domain.ChallengeDailyStatus;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import me.bombom.api.v1.challenge.domain.ChallengeTeam;
import me.bombom.api.v1.challenge.repository.ChallengeDailyResultRepository;
import me.bombom.api.v1.challenge.repository.ChallengeParticipantRepository;
import me.bombom.api.v1.challenge.repository.ChallengeRepository;
import me.bombom.api.v1.challenge.repository.ChallengeTeamRepository;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterGroupRepository;
import me.bombom.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
class ChallengeTeamServiceTest {

    @Autowired
    private ChallengeTeamService challengeTeamService;

    @Autowired
    private ChallengeTeamRepository challengeTeamRepository;

    @Autowired
    private ChallengeParticipantRepository challengeParticipantRepository;

    @Autowired
    private ChallengeDailyResultRepository challengeDailyResultRepository;

    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private NewsletterGroupRepository newsletterGroupRepository;

    @Autowired
    private Clock clock;

    private Challenge challenge;
    private ChallengeTeam team;

    @BeforeEach
    void setUp() {
        challengeDailyResultRepository.deleteAllInBatch();
        challengeParticipantRepository.deleteAllInBatch();
        challengeTeamRepository.deleteAllInBatch();
        challengeRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
        newsletterGroupRepository.deleteAllInBatch();

        var group = newsletterGroupRepository.save(TestFixture.createNewsletterGroup("테스트 그룹"));
        challenge = challengeRepository.save(TestFixture.createChallenge(
                "테스트 챌린지",
                LocalDate.now().minusDays(5),
                LocalDate.now().plusDays(5),
                10,
                group.getId()
        ));
        team = challengeTeamRepository.save(TestFixture.createChallengeTeam(challenge.getId(), 0));
    }

    @Test
    void 오늘_COMPLETE_인증자_비율로_팀_평균_달성률을_계산한다() {
        // given: 생존자 3명 중 2명이 오늘 COMPLETE
        LocalDate today = LocalDate.now(clock);
        ChallengeParticipant p1 = challengeParticipantRepository.save(createSurvivedParticipant());
        ChallengeParticipant p2 = challengeParticipantRepository.save(createSurvivedParticipant());
        ChallengeParticipant p3 = challengeParticipantRepository.save(createSurvivedParticipant());

        challengeDailyResultRepository.saveAll(List.of(
                TestFixture.createChallengeDailyResult(p1.getId(), today, ChallengeDailyStatus.COMPLETE),
                TestFixture.createChallengeDailyResult(p2.getId(), today, ChallengeDailyStatus.COMPLETE)
        ));

        // when
        challengeTeamService.updateTeamProgress(team);

        // then: 2/3 * 100 = 66
        ChallengeTeam updated = challengeTeamRepository.findById(team.getId()).orElseThrow();
        assertThat(updated.getProgress()).isEqualTo(66);
    }

    @Test
    void 탈락자는_팀_평균_달성률_계산에서_제외된다() {
        // given: 생존자 2명 + 탈락자 1명, 생존자 중 1명만 오늘 COMPLETE
        LocalDate today = LocalDate.now(clock);
        ChallengeParticipant survived1 = challengeParticipantRepository.save(createSurvivedParticipant());
        ChallengeParticipant survived2 = challengeParticipantRepository.save(createSurvivedParticipant());
        challengeParticipantRepository.save(createEliminatedParticipant());

        challengeDailyResultRepository.save(TestFixture.createChallengeDailyResult(survived1.getId(), today, ChallengeDailyStatus.COMPLETE));

        // when
        challengeTeamService.updateTeamProgress(team);

        // then: 1/2 * 100 = 50 (탈락자 제외)
        ChallengeTeam updated = challengeTeamRepository.findById(team.getId()).orElseThrow();
        assertThat(updated.getProgress()).isEqualTo(50);
    }

    @Test
    void SHIELD_인증은_팀_평균_달성률에_포함되지_않는다() {
        // given: 생존자 2명, 1명 COMPLETE, 1명 SHIELD
        LocalDate today = LocalDate.now(clock);
        ChallengeParticipant p1 = challengeParticipantRepository.save(createSurvivedParticipant());
        ChallengeParticipant p2 = challengeParticipantRepository.save(createSurvivedParticipant());

        challengeDailyResultRepository.saveAll(List.of(
                TestFixture.createChallengeDailyResult(p1.getId(), today, ChallengeDailyStatus.COMPLETE),
                TestFixture.createChallengeDailyResult(p2.getId(), today, ChallengeDailyStatus.SHIELD)
        ));

        // when
        challengeTeamService.updateTeamProgress(team);

        // then: 1/2 * 100 = 50
        ChallengeTeam updated = challengeTeamRepository.findById(team.getId()).orElseThrow();
        assertThat(updated.getProgress()).isEqualTo(50);
    }

    @Test
    void 생존자가_없으면_팀_평균_달성률은_0이다() {
        // given: 전원 탈락
        challengeParticipantRepository.save(createEliminatedParticipant());
        challengeParticipantRepository.save(createEliminatedParticipant());

        // when
        challengeTeamService.updateTeamProgress(team);

        // then
        ChallengeTeam updated = challengeTeamRepository.findById(team.getId()).orElseThrow();
        assertThat(updated.getProgress()).isEqualTo(0);
    }

    @Test
    void 진행_중인_챌린지_팀의_달성률을_0으로_초기화한다() {
        // given
        ChallengeTeam team1 = challengeTeamRepository.save(TestFixture.createChallengeTeam(challenge.getId(), 100));
        ChallengeTeam team2 = challengeTeamRepository.save(TestFixture.createChallengeTeam(challenge.getId(), 75));

        // when
        challengeTeamService.resetTeamsProgress(List.of(challenge));

        // then
        assertSoftly(softly -> {
            softly.assertThat(challengeTeamRepository.findById(team1.getId()).orElseThrow().getProgress()).isEqualTo(0);
            softly.assertThat(challengeTeamRepository.findById(team2.getId()).orElseThrow().getProgress()).isEqualTo(0);
        });
    }

    @Test
    void 진행_중인_챌린지가_없으면_초기화를_건너뛴다() {
        // given
        ChallengeTeam existingTeam = challengeTeamRepository.save(TestFixture.createChallengeTeam(challenge.getId(), 50));

        // when
        challengeTeamService.resetTeamsProgress(List.of());

        // then
        ChallengeTeam unchanged = challengeTeamRepository.findById(existingTeam.getId()).orElseThrow();
        assertThat(unchanged.getProgress()).isEqualTo(50);
    }

    private ChallengeParticipant createSurvivedParticipant() {
        Member member = memberRepository.save(
                TestFixture.createUniqueMember(
                        UUID.randomUUID().toString().substring(0, 8),
                        UUID.randomUUID().toString()
                )
        );
        return TestFixture.createChallengeParticipantWithTeam(
                challenge.getId(),
                member.getId(),
                team.getId(),
                0,
                0,
                true
        );
    }

    private ChallengeParticipant createEliminatedParticipant() {
        Member member = memberRepository.save(
                TestFixture.createUniqueMember(
                        UUID.randomUUID().toString().substring(0, 8),
                        UUID.randomUUID().toString()
                )
        );
        return TestFixture.createChallengeParticipantWithTeam(
                challenge.getId(),
                member.getId(),
                team.getId(),
                0,
                0,
                false
        );
    }
}
