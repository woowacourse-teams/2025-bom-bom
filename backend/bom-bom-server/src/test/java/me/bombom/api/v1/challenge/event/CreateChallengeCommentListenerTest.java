package me.bombom.api.v1.challenge.event;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDate;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.challenge.domain.Challenge;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import me.bombom.api.v1.challenge.domain.ChallengeTodo;
import me.bombom.api.v1.challenge.domain.ChallengeTodoType;
import me.bombom.api.v1.challenge.repository.ChallengeDailyResultRepository;
import me.bombom.api.v1.challenge.repository.ChallengeDailyTodoRepository;
import me.bombom.api.v1.challenge.repository.ChallengeParticipantRepository;
import me.bombom.api.v1.challenge.repository.ChallengeRepository;
import me.bombom.api.v1.challenge.repository.ChallengeTodoRepository;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
class CreateChallengeCommentListenerTest {

    @Autowired
    private CreateChallengeCommentListener listener;

    @Autowired
    private ChallengeDailyTodoRepository challengeDailyTodoRepository;

    @Autowired
    private ChallengeDailyResultRepository challengeDailyResultRepository;

    @Autowired
    private ChallengeTodoRepository challengeTodoRepository;

    @Autowired
    private ChallengeParticipantRepository challengeParticipantRepository;

    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private MemberRepository memberRepository;

    private ChallengeParticipant participant;
    private ChallengeTodo commentTodo;

    @BeforeEach
    void setUp() {
        challengeDailyResultRepository.deleteAllInBatch();
        challengeDailyTodoRepository.deleteAllInBatch();
        challengeParticipantRepository.deleteAllInBatch();
        challengeTodoRepository.deleteAllInBatch();
        challengeRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();

        Member member = memberRepository.save(TestFixture.normalMemberFixture());

        Challenge challenge = challengeRepository.save(
                TestFixture.createChallenge(
                        "테스트 챌린지",
                        LocalDate.now().minusDays(1),
                        LocalDate.now().plusDays(10),
                        11
                )
        );

        commentTodo = challengeTodoRepository.save(
                TestFixture.createChallengeTodo(challenge.getId(), ChallengeTodoType.COMMENT)
        );

        participant = challengeParticipantRepository.save(
                TestFixture.createChallengeParticipant(challenge.getId(), member.getId(), 0)
        );
    }

    @Test
    void 코멘트_작성_이벤트로_일일_TODO와_결과를_저장하고_완료일수를_증가시킨다() {
        // given
        LocalDate today = LocalDate.now();

        // when
        listener.on(new CreateChallengeCommentEvent(participant.getId()));

        // then
        ChallengeParticipant updated = challengeParticipantRepository.findById(participant.getId()).orElseThrow();
        assertSoftly(softly -> {
            softly.assertThat(challengeDailyTodoRepository.existsByParticipantIdAndTodoDateAndChallengeTodoId(
                    participant.getId(),
                    today,
                    commentTodo.getId()
            )).isTrue();
            softly.assertThat(challengeDailyResultRepository.existsByParticipantIdAndDate(
                    participant.getId(),
                    today
            )).isTrue();
            softly.assertThat(updated.getCompletedDays()).isEqualTo(1);
        });
    }

    @Test
    void 이미_출석한_날은_중복_처리하지_않는다() {
        // given
        LocalDate today = LocalDate.now();
        listener.on(new CreateChallengeCommentEvent(participant.getId()));
        long dailyTodoCount = challengeDailyTodoRepository.count();
        long dailyResultCount = challengeDailyResultRepository.count();
        int completedDays = challengeParticipantRepository.findById(participant.getId())
                .orElseThrow()
                .getCompletedDays();

        // when
        listener.on(new CreateChallengeCommentEvent(participant.getId()));

        // then
        ChallengeParticipant updated = challengeParticipantRepository.findById(participant.getId()).orElseThrow();
        assertSoftly(softly -> {
            softly.assertThat(challengeDailyTodoRepository.count()).isEqualTo(dailyTodoCount);
            softly.assertThat(challengeDailyResultRepository.count()).isEqualTo(dailyResultCount);
            softly.assertThat(challengeDailyResultRepository.existsByParticipantIdAndDate(
                    participant.getId(),
                    today
            )).isTrue();
            softly.assertThat(updated.getCompletedDays()).isEqualTo(completedDays);
        });
    }
}
