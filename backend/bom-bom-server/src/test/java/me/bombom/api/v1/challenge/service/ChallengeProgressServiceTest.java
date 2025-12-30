package me.bombom.api.v1.challenge.service;

import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDate;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.challenge.domain.Challenge;
import me.bombom.api.v1.challenge.domain.ChallengeDailyTodo;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import me.bombom.api.v1.challenge.domain.ChallengeTodo;
import me.bombom.api.v1.challenge.domain.ChallengeTodoStatus;
import me.bombom.api.v1.challenge.domain.ChallengeTodoType;
import me.bombom.api.v1.challenge.dto.MemberChallengeProgressResponse;
import me.bombom.api.v1.challenge.dto.TodayTodoResponse;
import me.bombom.api.v1.challenge.repository.ChallengeDailyTodoRepository;
import me.bombom.api.v1.challenge.repository.ChallengeParticipantRepository;
import me.bombom.api.v1.challenge.repository.ChallengeTodoRepository;
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

    private Member member;
    private Challenge challenge;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(TestFixture.createUniqueMember("tester", "12345"));

        challenge = challengeRepository.save(TestFixture.createChallenge(
                "Test Challenge",
                LocalDate.now().minusDays(5),
                LocalDate.now().plusDays(5),
                10));

        // 3일 완료한 참여자 생성
        ChallengeParticipant participant = challengeParticipantRepository.save(
                TestFixture.createChallengeParticipant(challenge.getId(), member.getId(), 3));

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
}
