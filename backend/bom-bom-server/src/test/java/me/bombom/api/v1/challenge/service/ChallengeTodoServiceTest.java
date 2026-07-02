package me.bombom.api.v1.challenge.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDate;
import java.util.List;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.challenge.domain.ChallengeDailyTodo;
import me.bombom.api.v1.challenge.domain.ChallengeDailyResult;
import me.bombom.api.v1.challenge.domain.ChallengeDailyStatus;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import me.bombom.api.v1.challenge.domain.ChallengeTodo;
import me.bombom.api.v1.challenge.domain.ChallengeTodoType;
import me.bombom.api.v1.challenge.repository.ChallengeDailyTodoRepository;
import me.bombom.api.v1.challenge.repository.ChallengeDailyResultRepository;
import me.bombom.api.v1.challenge.repository.ChallengeParticipantRepository;
import me.bombom.api.v1.challenge.repository.ChallengeRepository;
import me.bombom.api.v1.challenge.repository.ChallengeTodoRepository;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.CServerErrorException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterGroupRepository;
import me.bombom.support.integration.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
class ChallengeTodoServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 1, 5);

    @Autowired
    private ChallengeTodoService challengeTodoService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private ChallengeParticipantRepository challengeParticipantRepository;

    @Autowired
    private ChallengeDailyResultRepository challengeDailyResultRepository;

    @Autowired
    private ChallengeTodoRepository challengeTodoRepository;

    @Autowired
    private ChallengeDailyTodoRepository challengeDailyTodoRepository;

    @Autowired
    private NewsletterGroupRepository newsletterGroupRepository;

    @Test
    void 일일_투두_완료_시_completedDays와_streak가_증가한다() {
        // given
        ChallengeParticipant participant = saveParticipant();

        // when
        challengeTodoService.completeDailyTodo(participant.getId(), TODAY);

        // then
        ChallengeParticipant updated = challengeParticipantRepository.findById(participant.getId()).orElseThrow();
        List<ChallengeDailyResult> results = challengeDailyResultRepository.findAll();

        assertSoftly(softly -> {
            softly.assertThat(updated.getCompletedDays()).isEqualTo(3);
            softly.assertThat(updated.getStreak()).isEqualTo(3);
            softly.assertThat(updated.getLastParticipatedDate()).isEqualTo(TODAY);
            softly.assertThat(results).hasSize(1);
            softly.assertThat(results.getFirst().getStatus()).isEqualTo(ChallengeDailyStatus.COMPLETE);
        });
    }

    @Test
    void 댓글_투두_완료는_중복이_없을_때만_일일_투두를_저장한다() {
        // given
        LocalDate today = TODAY;
        ChallengeParticipant participant = saveParticipant();
        ChallengeTodo todo = saveTodo(participant.getChallengeId(), ChallengeTodoType.COMMENT);

        // when
        challengeTodoService.insertCommentDone(participant, today);
        challengeTodoService.insertCommentDone(participant, today);

        // then
        List<ChallengeDailyTodo> dailyTodos = challengeDailyTodoRepository.findAll();
        assertSoftly(softly -> {
            softly.assertThat(dailyTodos).hasSize(1);
            softly.assertThat(dailyTodos.getFirst().getParticipantId()).isEqualTo(participant.getId());
            softly.assertThat(dailyTodos.getFirst().getChallengeTodoId()).isEqualTo(todo.getId());
            softly.assertThat(dailyTodos.getFirst().getTodoDate()).isEqualTo(today);
        });
    }

    @Test
    void 다짐과_회고_투두_완료를_각각_저장한다() {
        // given
        LocalDate today = TODAY;
        ChallengeParticipant participant = saveParticipant();
        ChallengeTodo mindsetTodo = saveTodo(participant.getChallengeId(), ChallengeTodoType.MINDSET);
        ChallengeTodo reviewTodo = saveTodo(participant.getChallengeId(), ChallengeTodoType.REVIEW);

        // when
        challengeTodoService.insertMindsetDone(participant, today);
        challengeTodoService.insertReviewDone(participant, today);

        // then
        assertSoftly(softly -> {
            softly.assertThat(challengeDailyTodoRepository.findAll()).hasSize(2);
            softly.assertThat(challengeDailyTodoRepository.existsByParticipantIdAndTodoDateAndChallengeTodoId(
                    participant.getId(),
                    today,
                    mindsetTodo.getId()
            )).isTrue();
            softly.assertThat(challengeDailyTodoRepository.existsByParticipantIdAndTodoDateAndChallengeTodoId(
                    participant.getId(),
                    today,
                    reviewTodo.getId()
            )).isTrue();
        });
    }

    @Test
    void 챌린지_투두가_없으면_댓글_투두_완료를_저장할_수_없다() {
        // given
        ChallengeParticipant participant = saveParticipant();

        // when & then
        assertThatThrownBy(() -> challengeTodoService.insertCommentDone(participant, TODAY))
                .isInstanceOf(CServerErrorException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.ENTITY_NOT_FOUND);
    }

    @Test
    void 존재하지_않는_참가자는_일일_투두를_완료할_수_없다() {
        assertThatThrownBy(() -> challengeTodoService.completeDailyTodo(-1L, TODAY))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.ENTITY_NOT_FOUND);
    }

    private ChallengeParticipant saveParticipant() {
        var member = memberRepository.save(TestFixture.createUniqueMember("tester", "id"));
        var group = newsletterGroupRepository.save(TestFixture.createNewsletterGroup("그룹"));
        var challenge = challengeRepository.save(TestFixture.createChallenge(
                "Challenge", TODAY.minusDays(3), TODAY.plusDays(7), 10, group.getId()));

        return challengeParticipantRepository.save(ChallengeParticipant.builder()
                .challengeId(challenge.getId())
                .memberId(member.getId())
                .completedDays(2)
                .streak(2)
                .shield(0)
                .isSurvived(true)
                .build());
    }

    private ChallengeTodo saveTodo(Long challengeId, ChallengeTodoType todoType) {
        return challengeTodoRepository.save(TestFixture.createChallengeTodo(challengeId, todoType));
    }
}
