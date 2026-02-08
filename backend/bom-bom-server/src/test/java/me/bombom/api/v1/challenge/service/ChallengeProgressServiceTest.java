package me.bombom.api.v1.challenge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.challenge.domain.Challenge;
import me.bombom.api.v1.challenge.domain.ChallengeDailyResult;
import me.bombom.api.v1.challenge.domain.ChallengeDailyStatus;
import me.bombom.api.v1.challenge.domain.ChallengeDailyTodo;
import me.bombom.api.v1.challenge.domain.ChallengeGrade;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import me.bombom.api.v1.challenge.domain.ChallengeTeam;
import me.bombom.api.v1.challenge.domain.ChallengeTodo;
import me.bombom.api.v1.challenge.domain.ChallengeTodoStatus;
import me.bombom.api.v1.challenge.domain.ChallengeTodoType;
import me.bombom.api.v1.challenge.dto.response.CertificationInfoResponse;
import me.bombom.api.v1.challenge.dto.response.MemberChallengeProgressResponse;
import me.bombom.api.v1.challenge.dto.response.TeamChallengeProgressResponse;
import me.bombom.api.v1.challenge.dto.response.TodayTodoResponse;
import me.bombom.api.v1.challenge.repository.ChallengeDailyResultRepository;
import me.bombom.api.v1.challenge.repository.ChallengeDailyTodoRepository;
import me.bombom.api.v1.challenge.repository.ChallengeParticipantRepository;
import me.bombom.api.v1.challenge.repository.ChallengeRepository;
import me.bombom.api.v1.challenge.repository.ChallengeTeamRepository;
import me.bombom.api.v1.challenge.repository.ChallengeTodoRepository;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.common.exception.UnauthorizedException;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
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
    private ChallengeTodoRepository challengeTodoRepository;

    @Autowired
    private ChallengeDailyTodoRepository challengeDailyTodoRepository;

    @Autowired
    private ChallengeTeamRepository challengeTeamRepository;

    @Autowired
    private ChallengeDailyResultRepository challengeDailyResultRepository;

    private Member member;
    private Challenge challenge;

    @BeforeEach
    void setUp() {
        challengeDailyResultRepository.deleteAllInBatch();
        challengeDailyTodoRepository.deleteAllInBatch();
        challengeTodoRepository.deleteAllInBatch();
        challengeParticipantRepository.deleteAllInBatch();
        challengeTeamRepository.deleteAllInBatch();
        challengeRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();

        member = memberRepository.save(
                TestFixture.createUniqueMember("tester", java.util.UUID.randomUUID().toString()));

        challenge = challengeRepository.save(TestFixture.createChallenge(
                "Test Challenge",
                LocalDate.now().minusDays(5),
                LocalDate.now().plusDays(5),
                10));

        // 3일 완료한 참여자 생성
        ChallengeParticipant participant = challengeParticipantRepository
                .save(TestFixture.createChallengeParticipant(
                        challenge.getId(),
                        member.getId(),
                        3));

        ChallengeTodo readTodo = TestFixture.createChallengeTodo(challenge.getId(), ChallengeTodoType.READ);
        challengeTodoRepository.save(readTodo);

        ChallengeTodo commentTodo = TestFixture.createChallengeTodo(challenge.getId(),
                ChallengeTodoType.COMMENT);
        challengeTodoRepository.save(commentTodo);

        ChallengeDailyTodo dailyTodo = TestFixture.createChallengeDailyTodo(
                participant.getId(),
                LocalDate.now(),
                readTodo.getId());
        challengeDailyTodoRepository.save(dailyTodo);
    }

    @Test
    void 유저의_챌린지_진행상황을_조회한다() {
        // when
        MemberChallengeProgressResponse response = challengeProgressService.getMemberProgress(challenge.getId(),
                member);

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.nickname()).isEqualTo("tester");
            softly.assertThat(response.totalDays()).isEqualTo(10);
            softly.assertThat(response.isSurvived()).isEqualTo(true);
            softly.assertThat(response.completedDays()).isEqualTo(3);
            softly.assertThat(response.todayTodos()).hasSize(2);

            softly.assertThat(response.todayTodos())
                    .extracting(TodayTodoResponse::challengeTodoType,
                            TodayTodoResponse::challengeTodoStatus)
                    .contains(
                            tuple(ChallengeTodoType.READ, ChallengeTodoStatus.COMPLETE),
                            tuple(ChallengeTodoType.COMMENT,
                                    ChallengeTodoStatus.INCOMPLETE));
        });
    }

    @Test
    void 쉴드를_보유한_참가자는_결석_시_쉴드를_사용하여_생존한다() {
        // given
        LocalDate yesterday = LocalDate.now().minusDays(1);

        // Challenge: 총 10일. 80% = 8일. 최대 결석 = 2일
        Challenge survivalChallenge = challengeRepository.save(TestFixture.createChallenge(
                "Survival Challenge",
                yesterday.minusDays(4),
                yesterday.plusDays(5),
                10));

        ChallengeParticipant participant = challengeParticipantRepository.save(ChallengeParticipant.builder()
                .challengeId(survivalChallenge.getId())
                .memberId(member.getId())
                .completedDays(3)
                .shield(1)
                .isSurvived(true)
                .build());

        // when
        challengeProgressService.proceedDailySurvivalCheck(survivalChallenge, yesterday);

        // then
        ChallengeParticipant updatedParticipant = challengeParticipantRepository.findById(participant.getId())
                .orElseThrow();

        assertSoftly(softly -> {
            softly.assertThat(updatedParticipant.getShield()).isEqualTo(participant.getShield() - 1);
            softly.assertThat(updatedParticipant.getCompletedDays())
                    .isEqualTo(participant.getCompletedDays() + 1);
            softly.assertThat(updatedParticipant.isSurvived()).isTrue();

            List<ChallengeDailyResult> results = challengeDailyResultRepository.findAll();
            softly.assertThat(results).hasSize(1);
            softly.assertThat(results.getFirst().getStatus()).isEqualTo(ChallengeDailyStatus.SHIELD);
            softly.assertThat(results.getFirst().getDate()).isEqualTo(yesterday);
        });
    }

    @Test
    void 쉴드가_없어도_결석_허용일_이내라면_생존한다() {
        // given
        LocalDate yesterday = LocalDate.now().minusDays(1);

        // Challenge: 총 10일. 80% = 8일. 최대 결석 = 2일
        Challenge survivalChallenge = challengeRepository.save(TestFixture.createChallenge(
                "Survival Challenge 2",
                yesterday.minusDays(4),
                yesterday.plusDays(5),
                10));

        // Participant: 챌린지 3일 수행
        // 챌린지 시작한지 5일 지남, currentAbsent = 5 - 3 = 2, 2 >= 2 -> 생존.
        ChallengeParticipant participant = challengeParticipantRepository.save(ChallengeParticipant.builder()
                .challengeId(survivalChallenge.getId())
                .memberId(member.getId())
                .completedDays(3)
                .shield(0)
                .isSurvived(true)
                .build());

        // when
        challengeProgressService.proceedDailySurvivalCheck(survivalChallenge, yesterday);

        // then
        ChallengeParticipant updatedParticipant = challengeParticipantRepository.findById(participant.getId())
                .orElseThrow();
        assertSoftly(softly -> {
            softly.assertThat(updatedParticipant.isSurvived()).isTrue();
            softly.assertThat(updatedParticipant.getCompletedDays()).isEqualTo(3);
        });
    }

    @Test
    void 결석_허용일을_초과하면_생존에_실패한다() {
        // given
        LocalDate yesterday = LocalDate.of(2026, 1, 10).minusDays(1);

        // Challenge: 총 10일. 80% = 8일. 최대 결석 = 2일
        Challenge survivalChallenge = challengeRepository.save(TestFixture.createChallenge(
                "Survival Challenge 3",
                yesterday.minusDays(4),
                yesterday.plusDays(5),
                10));

        // Participant: 챌린지 2일 수행
        // 챌린지 시작한지 5일 지남, currentAbsent = 5 - 2 = 3, 3 > 2 -> 탈락.
        ChallengeParticipant participant = challengeParticipantRepository.save(ChallengeParticipant.builder()
                .challengeId(survivalChallenge.getId())
                .memberId(member.getId())
                .completedDays(2)
                .shield(0)
                .isSurvived(true)
                .build());

        // when
        challengeProgressService.proceedDailySurvivalCheck(survivalChallenge, yesterday);

        // then
        ChallengeParticipant updatedParticipant = challengeParticipantRepository.findById(participant.getId())
                .orElseThrow();
        assertThat(updatedParticipant.isSurvived()).isFalse();
    }

    @Test
    void 특정_팀_진행상황을_조회한다() {
        // given
        ChallengeTeam team = challengeTeamRepository.save(createChallengeTeam(challenge.getId(), 77));

        Member memberA = memberRepository.save(TestFixture.createUniqueMember("userA", "A"));
        Member memberB = memberRepository.save(TestFixture.createUniqueMember("userB", "B"));

        ChallengeParticipant participant1 = challengeParticipantRepository.save(
                createTeamParticipant(challenge.getId(), memberA.getId(), team.getId(), 2, false));
        ChallengeParticipant participant2 = challengeParticipantRepository.save(
                createTeamParticipant(challenge.getId(), memberB.getId(), team.getId(), 3, true));

        ChallengeDailyResult result1 = createChallengeDailyResult(participant1.getId(), LocalDate.now(),
                ChallengeDailyStatus.SHIELD);
        ChallengeDailyResult result2 = createChallengeDailyResult(participant1.getId(),
                LocalDate.now().plusDays(1),
                ChallengeDailyStatus.COMPLETE);
        ChallengeDailyResult result3 = createChallengeDailyResult(participant2.getId(), LocalDate.now(),
                ChallengeDailyStatus.COMPLETE);
        ChallengeDailyResult result4 = createChallengeDailyResult(participant2.getId(),
                LocalDate.now().plusDays(1),
                ChallengeDailyStatus.SHIELD);
        ChallengeDailyResult result5 = createChallengeDailyResult(participant2.getId(),
                LocalDate.now().plusDays(2),
                ChallengeDailyStatus.COMPLETE);
        challengeDailyResultRepository.saveAll(List.of(result1, result2, result3, result4, result5));

        // when
        TeamChallengeProgressResponse response = challengeProgressService.getTeamProgressByTeamId(
                challenge.getId(), team.getId(), memberA);

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.teamSummary().achievementAverage()).isEqualTo(77);
            softly.assertThat(response.members()).hasSize(2);

            // 정렬 순서 검증 (completedDays DESC)
            softly.assertThat(response.members())
                    .extracting("nickname")
                    .containsExactly(memberB.getNickname(), memberA.getNickname());
        });
    }

    @Test
    void 특정_팀_진행상황_조회_실패_참가하지_않음() {
        // given
        ChallengeTeam team = challengeTeamRepository.save(createChallengeTeam(challenge.getId(), 77));
        Member nonParticipant = memberRepository.save(TestFixture.createUniqueMember("nonParticipant", "NP"));

        // when & then
        assertThatThrownBy(() -> challengeProgressService.getTeamProgressByTeamId(
                challenge.getId(), team.getId(), nonParticipant))
                .isInstanceOf(UnauthorizedException.class)
                .satisfies(e -> {
                    UnauthorizedException exception = (UnauthorizedException) e;
                    assertThat(exception.getErrorDetail())
                            .isEqualTo(ErrorDetail.FORBIDDEN_RESOURCE);
                });
    }

    @Test
    void 특정_팀_진행상황_조회_실패_챌린지_없음() {
        // given
        ChallengeTeam team = challengeTeamRepository.save(createChallengeTeam(challenge.getId(), 77));
        Long nonExistentChallengeId = 0L;

        // when & then
        assertThatThrownBy(() -> challengeProgressService.getTeamProgressByTeamId(
                nonExistentChallengeId, team.getId(), member))
                .isInstanceOf(CIllegalArgumentException.class)
                .satisfies(e -> {
                    CIllegalArgumentException exception = (CIllegalArgumentException) e;
                    assertThat(exception.getContext().get(ErrorContextKeys.ENTITY_TYPE.getKey()))
                            .isEqualTo("challenge");
                });
    }

    @Test
    void 특정_팀_진행상황_조회_실패_팀_없음() {
        // given
        Long nonExistentTeamId = 0L;

        // when & then
        assertThatThrownBy(() -> challengeProgressService.getTeamProgressByTeamId(
                challenge.getId(), nonExistentTeamId, member))
                .isInstanceOf(CIllegalArgumentException.class)
                .satisfies(e -> {
                    CIllegalArgumentException exception = (CIllegalArgumentException) e;
                    assertThat(exception.getContext().get(ErrorContextKeys.ENTITY_TYPE.getKey()))
                            .isEqualTo("challengeTeam");
                });
    }

    @Test
    void 특정_팀_진행상황_조회_실패_팀이_챌린지에_속하지_않음() {
        // given
        Challenge otherChallenge = challengeRepository.save(TestFixture.createChallenge(
                "Other Challenge",
                LocalDate.now().minusDays(5),
                LocalDate.now().plusDays(5),
                10));
        ChallengeTeam otherTeam = challengeTeamRepository.save(createChallengeTeam(otherChallenge.getId(), 50));

        // when & then
        assertThatThrownBy(() -> challengeProgressService.getTeamProgressByTeamId(
                challenge.getId(), otherTeam.getId(), member))
                .isInstanceOf(CIllegalArgumentException.class)
                .satisfies(e -> {
                    CIllegalArgumentException exception = (CIllegalArgumentException) e;
                    assertThat(exception.getContext().get(ErrorContextKeys.ENTITY_TYPE.getKey()))
                            .isEqualTo("challengeTeam");
                });
    }

    private ChallengeParticipant createTeamParticipant(Long challengeId, Long memberId, Long teamId,
                                                       int completedDays, boolean isSurvived) {
        return ChallengeParticipant.builder()
                .challengeId(challengeId)
                .memberId(memberId)
                .challengeTeamId(teamId)
                .completedDays(completedDays)
                .isSurvived(isSurvived)
                .shield(0)
                .build();
    }

    private ChallengeTeam createChallengeTeam(Long challengeId, int progress) {
        return ChallengeTeam.builder()
                .challengeId(challengeId)
                .progress(progress)
                .build();
    }

    private ChallengeDailyResult createChallengeDailyResult(Long participantId, LocalDate date,
                                                            ChallengeDailyStatus status) {
        return ChallengeDailyResult.builder()
                .participantId(participantId)
                .date(date)
                .status(status)
                .build();
    }

    @Test
    void 주말을_제외한_평일만_계산하여_생존_처리한다() {
        // given
        // 금요일(1일차) 시작
        LocalDate friday = LocalDate.of(2024, 1, 5);
        // 다음주 월요일(4일차)에 체크 (어제인 월요일까지의 생존 여부 판단)
        LocalDate monday = LocalDate.of(2024, 1, 8);

        // 총 10일 (영업일 기준). 2024-01-05 ~ 2024-01-18 (금요일~다음다음 목요일, 14일간, 주말 4일 제외 = 10일)
        Challenge weekendChallenge = challengeRepository.save(TestFixture.createChallenge(
                "Weekend Challenge",
                friday,
                friday.plusDays(13), // 2 weeks
                10 // total days (business only)
        ));

        // User completed 0 days.
        // 금요일(1일) + 월요일(1일) = 총 2일 경과.
        // 결석 = 2일.
        // 전체 10일의 80% = 8일 출석 필요 -> 최대 2일 결석 허용.
        // 2일 결석 <= 2일 허용 -> 생존해야 함.
        ChallengeParticipant participant = challengeParticipantRepository.save(
                ChallengeParticipant.builder()
                        .challengeId(weekendChallenge.getId())
                        .memberId(member.getId())
                        .completedDays(0)
                        .isSurvived(true)
                        .build());

        // when
        challengeProgressService.proceedDailySurvivalCheck(weekendChallenge, monday);

        // then
        ChallengeParticipant updated = challengeParticipantRepository.findById(participant.getId()).get();
        assertThat(updated.isSurvived()).isTrue();
    }

    @Test
    void 종료된_챌린지의_수료증_정보를_조회한다() {
        // given
        LocalDate yesterday = LocalDate.now().minusDays(1);
        Challenge completedChallenge = challengeRepository.save(
                TestFixture.createChallenge(
                        "Completed Challenge",
                        yesterday.minusDays(10),
                        yesterday,
                        10
                )
        );

        challengeParticipantRepository.save(
                TestFixture.createChallengeParticipant(
                        completedChallenge.getId(),
                        member.getId(),
                        10,
                        true
                )
        );

        // when
        CertificationInfoResponse response = challengeProgressService.getCertificationInfo(completedChallenge.getId(),
                member.getId());

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.nickname()).isEqualTo(member.getNickname());
            softly.assertThat(response.challengeName()).isEqualTo("Completed Challenge");
            softly.assertThat(response.generation()).isEqualTo(1);
            softly.assertThat(response.startDate()).isEqualTo(yesterday.minusDays(10));
            softly.assertThat(response.endDate()).isEqualTo(yesterday);
            softly.assertThat(response.medal()).isEqualTo(ChallengeGrade.GOLD);
            softly.assertThat(response.medalCondition()).isEqualTo(100);
        });
    }

    @Test
    void 진행_중인_챌린지의_수료증_조회시_예외_발생() {
        // when & then
        assertThatThrownBy(() -> challengeProgressService.getCertificationInfo(challenge.getId(), member.getId()))
                .isInstanceOf(CIllegalArgumentException.class)
                .satisfies(e -> {
                    CIllegalArgumentException exception = (CIllegalArgumentException) e;
                    assertThat(exception.getErrorDetail()).isEqualTo(ErrorDetail.PRECONDITION_FAILED);
                });
    }

    @Test
    void 존재하지_않는_챌린지의_수료증_조회시_예외_발생() {
        // given
        Long nonExistentChallengeId = 0L;

        // when & then
        assertThatThrownBy(() -> challengeProgressService.getCertificationInfo(nonExistentChallengeId, member.getId()))
                .isInstanceOf(CIllegalArgumentException.class)
                .satisfies(e -> {
                    CIllegalArgumentException exception = (CIllegalArgumentException) e;
                    assertThat(exception.getErrorDetail()).isEqualTo(ErrorDetail.ENTITY_NOT_FOUND);
                    assertThat(exception.getContext().get(ErrorContextKeys.ENTITY_TYPE.getKey())).isEqualTo("challenge");
                });
    }

    @Test
    void 참가하지_않은_챌린지의_수료증_조회시_예외_발생() {
        // given
        LocalDate yesterday = LocalDate.now().minusDays(1);
        Challenge completedChallenge = challengeRepository.save(
                TestFixture.createChallenge(
                        "Other Challenge",
                        yesterday.minusDays(10),
                        yesterday,
                        10
                )
        );

        Member otherMember = memberRepository.save(
                TestFixture.createUniqueMember(
                        "other",
                        UUID.randomUUID().toString()
                )
        );

        // when & then
        assertThatThrownBy(() -> challengeProgressService.getCertificationInfo(completedChallenge.getId(), otherMember.getId()))
                .isInstanceOf(CIllegalArgumentException.class)
                .satisfies(e -> {
                    CIllegalArgumentException exception = (CIllegalArgumentException) e;
                    assertThat(exception.getErrorDetail()).isEqualTo(ErrorDetail.ENTITY_NOT_FOUND);
                    assertThat(exception.getContext().get(ErrorContextKeys.ENTITY_TYPE.getKey()))
                            .isEqualTo("challengeParticipant");
                });
    }

    @Test
    void 생존하지_못한_참가자는_FAIL_등급을_받는다() {
        // given
        LocalDate yesterday = LocalDate.now().minusDays(1);
        Challenge completedChallenge = challengeRepository.save(
                TestFixture.createChallenge(
                        "Failed Challenge",
                        yesterday.minusDays(10),
                        yesterday,
                        10
                )
        );

        challengeParticipantRepository.save(
                TestFixture.createChallengeParticipant(
                        completedChallenge.getId(),
                        member.getId(),
                        5,
                        false
                )
        );

        // when & then
        assertThatThrownBy(() -> challengeProgressService.getCertificationInfo(completedChallenge.getId(), member.getId()))
                .isInstanceOf(CIllegalArgumentException.class)
                .satisfies(e -> {
                    CIllegalArgumentException exception = (CIllegalArgumentException) e;
                    assertThat(exception.getErrorDetail()).isEqualTo(ErrorDetail.PRECONDITION_FAILED);
                });
    }

    @Test
    void 진행률에_따라_올바른_등급을_받는다() {
        // given
        LocalDate yesterday = LocalDate.now().minusDays(1);
        Challenge completedChallenge = challengeRepository.save(
                TestFixture.createChallenge(
                        "Grade Challenge",
                        yesterday.minusDays(10),
                        yesterday,
                        10
                )
        );

        // 90% 달성 (은메달)
        challengeParticipantRepository.save(
                TestFixture.createChallengeParticipant(
                        completedChallenge.getId(),
                        member.getId(),
                        9,
                        true
                )
        );

        // when
        CertificationInfoResponse response = challengeProgressService.getCertificationInfo(completedChallenge.getId(),
                member.getId());

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.medal()).isEqualTo(ChallengeGrade.SILVER);
            softly.assertThat(response.medalCondition()).isEqualTo(90);
        });
    }
}
