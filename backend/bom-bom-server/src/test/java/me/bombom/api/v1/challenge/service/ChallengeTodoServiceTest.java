package me.bombom.api.v1.challenge.service;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDate;
import java.util.List;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.challenge.domain.ChallengeDailyResult;
import me.bombom.api.v1.challenge.domain.ChallengeDailyStatus;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import me.bombom.api.v1.challenge.domain.ChallengeTodoType;
import me.bombom.api.v1.challenge.repository.ChallengeDailyResultRepository;
import me.bombom.api.v1.challenge.repository.ChallengeDailyTodoRepository;
import me.bombom.api.v1.challenge.repository.ChallengeParticipantRepository;
import me.bombom.api.v1.challenge.repository.ChallengeRepository;
import me.bombom.api.v1.challenge.repository.ChallengeTodoRepository;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterGroupRepository;
import me.bombom.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
class ChallengeTodoServiceTest {

    @Autowired
    private ChallengeTodoService challengeTodoService;

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
    private ChallengeDailyResultRepository challengeDailyResultRepository;

    @Autowired
    private NewsletterGroupRepository newsletterGroupRepository;

    private ChallengeParticipant participant;

    @BeforeEach
    void setUp() {
        challengeDailyResultRepository.deleteAllInBatch();
        challengeDailyTodoRepository.deleteAllInBatch();
        challengeTodoRepository.deleteAllInBatch();
        challengeParticipantRepository.deleteAllInBatch();
        challengeRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
        newsletterGroupRepository.deleteAllInBatch();

        var member = memberRepository.save(TestFixture.createUniqueMember("tester", "id"));
        var group = newsletterGroupRepository.save(TestFixture.createNewsletterGroup("그룹"));
        var challenge = challengeRepository.save(TestFixture.createChallenge(
                "Challenge", LocalDate.now().minusDays(3), LocalDate.now().plusDays(7), 10, group.getId()));

        challengeTodoRepository.save(TestFixture.createChallengeTodo(challenge.getId(), ChallengeTodoType.COMMENT));

        participant = challengeParticipantRepository.save(ChallengeParticipant.builder()
                .challengeId(challenge.getId())
                .memberId(member.getId())
                .completedDays(2)
                .streak(2)
                .shield(0)
                .isSurvived(true)
                .build());
    }

    @Test
    void 일일_투두_완료_시_completedDays와_streak가_증가한다() {
        // when
        challengeTodoService.completeDailyTodo(participant.getId(), LocalDate.now());

        // then
        ChallengeParticipant updated = challengeParticipantRepository.findById(participant.getId()).orElseThrow();
        List<ChallengeDailyResult> results = challengeDailyResultRepository.findAll();

        assertSoftly(softly -> {
            softly.assertThat(updated.getCompletedDays()).isEqualTo(3);
            softly.assertThat(updated.getStreak()).isEqualTo(3);
            softly.assertThat(updated.getLastParticipatedDate()).isEqualTo(LocalDate.now());
            softly.assertThat(results).hasSize(1);
            softly.assertThat(results.getFirst().getStatus()).isEqualTo(ChallengeDailyStatus.COMPLETE);
        });
    }
}
