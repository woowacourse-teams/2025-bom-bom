package me.bombom.api.v1.challenge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDate;
import java.util.List;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.challenge.domain.Challenge;
import me.bombom.api.v1.challenge.domain.ChallengeDailyResult;
import me.bombom.api.v1.challenge.domain.ChallengeDailyStatus;
import me.bombom.api.v1.challenge.domain.ChallengeDailyTodo;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import me.bombom.api.v1.challenge.domain.ChallengeTeam;
import me.bombom.api.v1.challenge.domain.ChallengeTodo;
import me.bombom.api.v1.challenge.domain.ChallengeTodoStatus;
import me.bombom.api.v1.challenge.domain.ChallengeTodoType;
import me.bombom.api.v1.challenge.dto.response.MemberChallengeProgressResponse;
import me.bombom.api.v1.challenge.dto.response.MemberDailyResultResponse;
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
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.support.IntegrationTest;
import org.junit.jupiter.api.AfterEach;
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

    @AfterEach
    void tearDown() {
        challengeDailyResultRepository.deleteAllInBatch();
        challengeDailyTodoRepository.deleteAllInBatch();
        challengeTodoRepository.deleteAllInBatch();
        challengeParticipantRepository.deleteAllInBatch();
        challengeTeamRepository.deleteAllInBatch();
        challengeRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @BeforeEach
    void setUp() {
        member = memberRepository.save(TestFixture.createUniqueMember("tester", "12345"));

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
        MemberChallengeProgressResponse response = challengeProgressService.getMemberProgress(challenge.getId(), member);

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.nickname()).isEqualTo("tester");
            softly.assertThat(response.totalDays()).isEqualTo(10);
            softly.assertThat(response.completedDays()).isEqualTo(3);
            softly.assertThat(response.todayTodos()).hasSize(2);

            softly.assertThat(response.todayTodos())
                    .extracting(TodayTodoResponse::challengeTodoType, TodayTodoResponse::challengeTodoStatus)
                    .contains(
                            tuple(ChallengeTodoType.READ, ChallengeTodoStatus.COMPLETE),
                            tuple(ChallengeTodoType.COMMENT, ChallengeTodoStatus.INCOMPLETE)
                    );
        });
    }

    @Test
    void 팀_챌린지_진행상황을_조회한다() {
        // given
        ChallengeTeam team = challengeTeamRepository.save(createChallengeTeam(challenge.getId(), 77));

        Member memberA = memberRepository.save(TestFixture.createUniqueMember("userA", "A"));
        Member memberB = memberRepository.save(TestFixture.createUniqueMember("userB", "B"));

        ChallengeParticipant participant1 = challengeParticipantRepository.save(
                createTeamParticipant(
                        challenge.getId(),
                        memberA.getId(),
                        team.getId(),
                        3,
                        true
                )
        );
        challengeParticipantRepository.save(
                createTeamParticipant(
                        challenge.getId(),
                        memberB.getId(),
                        team.getId(),
                        0,
                        false
                )
        );

        ChallengeDailyResult resultA = createChallengeDailyResult(participant1.getId(), LocalDate.now(), ChallengeDailyStatus.COMPLETE);
        ChallengeDailyResult resultB = createChallengeDailyResult(participant1.getId(), LocalDate.now().plusDays(1), ChallengeDailyStatus.SHIELD);
        challengeDailyResultRepository.saveAll(List.of(resultA, resultB));

        // when
        TeamChallengeProgressResponse response = challengeProgressService.getTeamProgress(challenge.getId(), memberA);

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.teamSummary().achievementAverage()).isEqualTo(77);
            softly.assertThat(response.members()).hasSize(2);

            softly.assertThat(response.members())
                    .extracting("nickname", "isSurvived")
                    .containsExactlyInAnyOrder(
                            tuple(memberA.getNickname(), true),
                            tuple(memberB.getNickname(), false));

            MemberDailyResultResponse responseA = response.members().stream()
                    .filter(m -> "userA".equals(m.nickname()))
                    .findFirst()
                    .orElseThrow();

            softly.assertThat(responseA.dailyProgresses())
                    .hasSize(2)
                    .extracting("status")
                    .containsExactlyInAnyOrder(ChallengeDailyStatus.COMPLETE, ChallengeDailyStatus.SHIELD);
        });
    }

    @Test
    void 팀_챌린지_진행상황_조회_실패_참가정보_없음() {
        // given
        Long nonExistentChallengeId = 0L;

        // when & then
        assertThatThrownBy(() -> challengeProgressService.getTeamProgress(nonExistentChallengeId, member))
                .isInstanceOf(CIllegalArgumentException.class)
                .satisfies(e -> {
                    CIllegalArgumentException exception = (CIllegalArgumentException) e;
                    assertThat(exception.getContext().get(ErrorContextKeys.ENTITY_TYPE.getKey()))
                            .isEqualTo("challenge");
                });
    }

    @Test
    void 팀_챌린지_진행상황_조회_실패_팀_없음() {
        // given
        Member newMember = memberRepository.save(TestFixture.createUniqueMember("new", "new"));

        // when & then
        assertThatThrownBy(() -> challengeProgressService.getTeamProgress(challenge.getId(), newMember))
                .isInstanceOf(CIllegalArgumentException.class)
                .satisfies(e -> {
                    CIllegalArgumentException exception = (CIllegalArgumentException) e;
                    assertThat(exception.getContext().get(ErrorContextKeys.ENTITY_TYPE.getKey()))
                            .isEqualTo("challengeParticipant");
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
}
