package me.bombom.api.v1.challenge.service;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDate;
import java.util.List;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.article.event.MarkAsReadEvent;
import me.bombom.api.v1.challenge.domain.Challenge;
import me.bombom.api.v1.challenge.domain.ChallengeDailyTodo;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import me.bombom.api.v1.challenge.domain.ChallengeTodo;
import me.bombom.api.v1.challenge.domain.ChallengeTodoType;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@IntegrationTest
class ChallengeDailyTodoServiceTest {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

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
    private ChallengeTodo readTodo;

    @BeforeEach
    void setUp() {
        challengeDailyTodoRepository.deleteAllInBatch();
        challengeTodoRepository.deleteAllInBatch();
        challengeParticipantRepository.deleteAllInBatch();
        challengeRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();

        member = memberRepository.save(TestFixture.createUniqueMember("tester", "12345"));

        LocalDate today = LocalDate.now();
        challenge = challengeRepository.save(TestFixture.createChallenge(
                "테스트 챌린지",
                today.minusDays(5),
                today.plusDays(5),
                10
        ));

        challengeParticipantRepository.save(
                TestFixture.createChallengeParticipant(
                        challenge.getId(),
                        member.getId(),
                        0
                )
        );

        readTodo = challengeTodoRepository.save(
                TestFixture.createChallengeTodo(challenge.getId(), ChallengeTodoType.READ)
        );
    }

    @Test
    void 아티클_읽기시_챌린지_투두_업데이트() {
        // given
        LocalDate today = LocalDate.now();
        ChallengeParticipant participant = challengeParticipantRepository.findByChallengeIdAndMemberId(
                challenge.getId(), member.getId()).orElseThrow();

        // when
        eventPublisher.publishEvent(new MarkAsReadEvent(member.getId(), 1L));
        TestTransaction.flagForCommit();
        TestTransaction.end();

        // then - 특정 멤버와 챌린지에 대한 todo만 확인
        List<ChallengeDailyTodo> dailyTodos = challengeDailyTodoRepository.findAll().stream()
                .filter(todo -> todo.getParticipantId().equals(participant.getId()))
                .filter(todo -> todo.getChallengeTodoId().equals(readTodo.getId()))
                .filter(todo -> todo.getTodoDate().equals(today))
                .toList();

        assertSoftly(softly -> {
            softly.assertThat(dailyTodos).hasSize(1);
            softly.assertThat(dailyTodos.get(0).getParticipantId()).isEqualTo(participant.getId());
            softly.assertThat(dailyTodos.get(0).getChallengeTodoId()).isEqualTo(readTodo.getId());
            softly.assertThat(dailyTodos.get(0).getTodoDate()).isEqualTo(today);
        });
    }

    @Test
    void 이미_존재하는_챌린지_투두_중복_생성_안함() {
        // given
        LocalDate today = LocalDate.now();
        ChallengeParticipant participant = challengeParticipantRepository.findByChallengeIdAndMemberId(
                challenge.getId(), member.getId()).orElseThrow();

        // 기존 todo 생성
        challengeDailyTodoRepository.save(
                TestFixture.createChallengeDailyTodo(
                        participant.getId(),
                        today,
                        readTodo.getId()
                )
        );

        // when
        eventPublisher.publishEvent(new MarkAsReadEvent(member.getId(), 1L));
        TestTransaction.flagForCommit();
        TestTransaction.end();

        // then - 특정 멤버와 챌린지에 대한 todo만 확인
        List<ChallengeDailyTodo> dailyTodos = challengeDailyTodoRepository.findAll().stream()
                .filter(todo -> todo.getParticipantId().equals(participant.getId()))
                .filter(todo -> todo.getChallengeTodoId().equals(readTodo.getId()))
                .filter(todo -> todo.getTodoDate().equals(today))
                .toList();

        assertSoftly(softly -> {
            softly.assertThat(dailyTodos).hasSize(1);
            softly.assertThat(dailyTodos.get(0).getParticipantId()).isEqualTo(participant.getId());
            softly.assertThat(dailyTodos.get(0).getChallengeTodoId()).isEqualTo(readTodo.getId());
            softly.assertThat(dailyTodos.get(0).getTodoDate()).isEqualTo(today);
        });
    }
}
