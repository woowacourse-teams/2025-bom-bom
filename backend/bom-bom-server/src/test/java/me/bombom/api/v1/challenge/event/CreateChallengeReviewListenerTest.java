package me.bombom.api.v1.challenge.event;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.Clock;
import java.time.LocalDate;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.challenge.domain.Challenge;
import me.bombom.api.v1.challenge.domain.ChallengeDailyStatus;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import me.bombom.api.v1.challenge.domain.ChallengeTeam;
import me.bombom.api.v1.challenge.domain.ChallengeTodo;
import me.bombom.api.v1.challenge.domain.ChallengeTodoType;
import me.bombom.api.v1.challenge.repository.ChallengeDailyResultRepository;
import me.bombom.api.v1.challenge.repository.ChallengeDailyTodoRepository;
import me.bombom.api.v1.challenge.repository.ChallengeParticipantRepository;
import me.bombom.api.v1.challenge.repository.ChallengeRepository;
import me.bombom.api.v1.challenge.repository.ChallengeTeamRepository;
import me.bombom.api.v1.challenge.repository.ChallengeTodoRepository;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.newsletter.domain.NewsletterGroup;
import me.bombom.api.v1.newsletter.repository.NewsletterGroupRepository;
import me.bombom.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
class CreateChallengeReviewListenerTest {

    @Autowired
    private CreateChallengeReviewListener listener;

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
    private ChallengeTeamRepository challengeTeamRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private NewsletterGroupRepository newsletterGroupRepository;

    @Autowired
    private Clock clock;

    private ChallengeParticipant participant;
    private ChallengeTodo reviewTodo;

    @BeforeEach
    void setUp() {
        challengeDailyResultRepository.deleteAllInBatch();
        challengeDailyTodoRepository.deleteAllInBatch();
        challengeParticipantRepository.deleteAllInBatch();
        challengeTodoRepository.deleteAllInBatch();
        challengeTeamRepository.deleteAllInBatch();
        challengeRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
        newsletterGroupRepository.deleteAllInBatch();

        Member member = memberRepository.save(TestFixture.normalMemberFixture());

        NewsletterGroup group = TestFixture.createNewsletterGroup("그룹");
        newsletterGroupRepository.save(group);
        Challenge challenge = challengeRepository.save(
                TestFixture.createChallenge(
                        "테스트 챌린지",
                        LocalDate.now(clock).minusDays(1),
                        LocalDate.now(clock).plusDays(8),
                        10,
                        group.getId()
                )
        );

        ChallengeTeam challengeTeam = challengeTeamRepository.save(
                TestFixture.createChallengeTeam(challenge.getId(), 0));

        reviewTodo = challengeTodoRepository.save(
                TestFixture.createChallengeTodo(challenge.getId(), ChallengeTodoType.REVIEW)
        );

        participant = challengeParticipantRepository.save(
                TestFixture.createChallengeParticipantWithTeam(
                        challenge.getId(),
                        member.getId(),
                        challengeTeam.getId(),
                        0,
                        0
                )
        );

        // 팀 평균 계산을 위해 다른 팀원 추가
        challengeParticipantRepository.save(
                TestFixture.createChallengeParticipantWithTeam(
                        challenge.getId(),
                        memberRepository.save(TestFixture.createMemberFixture("otherEmail", "otherNickname")).getId(),
                        challengeTeam.getId(),
                        5,
                        0
                )
        );
    }

    @Test
    void 리뷰_작성_이벤트로_REVIEW_일일_TODO와_결과를_저장하고_완료일수와_팀_평균을_갱신한다() {
        // given
        LocalDate today = LocalDate.now(clock);

        // when
        listener.on(new CreateChallengeReviewEvent(participant.getId(), today));

        // then
        ChallengeParticipant updated = challengeParticipantRepository.findById(participant.getId()).orElseThrow();
        ChallengeTeam updatedTeam = challengeTeamRepository.findById(participant.getChallengeTeamId()).orElseThrow();
        assertSoftly(softly -> {
            softly.assertThat(challengeDailyTodoRepository.existsByParticipantIdAndTodoDateAndChallengeTodoId(
                    participant.getId(),
                    today,
                    reviewTodo.getId()
            )).isTrue();
            softly.assertThat(challengeDailyResultRepository.existsByParticipantIdAndDate(
                    participant.getId(),
                    today
            )).isTrue();
            softly.assertThat(updated.getCompletedDays()).isEqualTo(1);
            softly.assertThat(updatedTeam.getProgress()).isEqualTo(50); // 오늘 COMPLETE 1명 / 생존자 2명 * 100 = 50
        });
    }

    @Test
    void 이미_출석한_날은_중복_처리하지_않는다() {
        // given
        LocalDate today = LocalDate.now(clock);
        listener.on(new CreateChallengeReviewEvent(participant.getId(), today));
        long dailyTodoCount = challengeDailyTodoRepository.count();
        long dailyResultCount = challengeDailyResultRepository.count();
        int completedDays = challengeParticipantRepository.findById(participant.getId())
                .orElseThrow()
                .getCompletedDays();

        // when
        listener.on(new CreateChallengeReviewEvent(participant.getId(), today));

        // then
        ChallengeParticipant updated = challengeParticipantRepository.findById(participant.getId()).orElseThrow();
        ChallengeTeam updatedTeam = challengeTeamRepository.findById(participant.getChallengeTeamId()).orElseThrow();
        assertSoftly(softly -> {
            softly.assertThat(challengeDailyTodoRepository.count()).isEqualTo(dailyTodoCount);
            softly.assertThat(challengeDailyResultRepository.count()).isEqualTo(dailyResultCount);
            softly.assertThat(challengeDailyResultRepository.existsByParticipantIdAndDate(
                    participant.getId(),
                    today
            )).isTrue();
            softly.assertThat(updated.getCompletedDays()).isEqualTo(completedDays);
            softly.assertThat(updatedTeam.getProgress()).isEqualTo(50); // 두 번째 호출은 early return → 첫 번째 결과(50) 유지
        });
    }

    @Test
    void 이미_출석한_날이어도_REVIEW_일일_TODO는_저장한다() {
        // given
        LocalDate today = LocalDate.now(clock);
        challengeDailyResultRepository.save(TestFixture.createChallengeDailyResult(
                participant.getId(),
                today,
                ChallengeDailyStatus.COMPLETE
        ));

        // when
        listener.on(new CreateChallengeReviewEvent(participant.getId(), today));

        // then
        ChallengeParticipant updated = challengeParticipantRepository.findById(participant.getId()).orElseThrow();
        ChallengeTeam updatedTeam = challengeTeamRepository.findById(participant.getChallengeTeamId()).orElseThrow();
        assertSoftly(softly -> {
            softly.assertThat(challengeDailyTodoRepository.existsByParticipantIdAndTodoDateAndChallengeTodoId(
                    participant.getId(),
                    today,
                    reviewTodo.getId()
            )).isTrue();
            softly.assertThat(challengeDailyResultRepository.count()).isEqualTo(1);
            softly.assertThat(updated.getCompletedDays()).isZero();
            softly.assertThat(updatedTeam.getProgress()).isZero();
        });
    }

    @Test
    void 이벤트의_reviewDate_가_과거여도_그_날짜로_출석_처리된다() {
        // given — Listener가 event.reviewDate() 를 사용함을 검증 (자정 경계/재시도 시나리오)
        LocalDate yesterday = LocalDate.now(clock).minusDays(1);

        // when
        listener.on(new CreateChallengeReviewEvent(participant.getId(), yesterday));

        // then
        assertSoftly(softly -> {
            softly.assertThat(challengeDailyTodoRepository.existsByParticipantIdAndTodoDateAndChallengeTodoId(
                    participant.getId(),
                    yesterday,
                    reviewTodo.getId()
            )).isTrue();
            softly.assertThat(challengeDailyResultRepository.existsByParticipantIdAndDate(
                    participant.getId(),
                    yesterday
            )).isTrue();
        });
    }
}
