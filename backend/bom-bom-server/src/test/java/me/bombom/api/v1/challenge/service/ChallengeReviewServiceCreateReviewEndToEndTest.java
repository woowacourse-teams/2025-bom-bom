package me.bombom.api.v1.challenge.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.challenge.domain.Challenge;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import me.bombom.api.v1.challenge.domain.ChallengeReview;
import me.bombom.api.v1.challenge.domain.ChallengeTeam;
import me.bombom.api.v1.challenge.domain.ChallengeTodo;
import me.bombom.api.v1.challenge.domain.ChallengeTodoType;
import me.bombom.api.v1.challenge.dto.request.CreateChallengeReviewRequest;
import me.bombom.api.v1.challenge.repository.ChallengeDailyResultRepository;
import me.bombom.api.v1.challenge.repository.ChallengeDailyTodoRepository;
import me.bombom.api.v1.challenge.repository.ChallengeParticipantRepository;
import me.bombom.api.v1.challenge.repository.ChallengeRepository;
import me.bombom.api.v1.challenge.repository.ChallengeReviewRepository;
import me.bombom.api.v1.challenge.repository.ChallengeTeamRepository;
import me.bombom.api.v1.challenge.repository.ChallengeTodoRepository;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.newsletter.domain.NewsletterGroup;
import me.bombom.api.v1.newsletter.repository.NewsletterGroupRepository;
import me.bombom.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * 리뷰 생성 → 이벤트 발행 → 출석 처리 시나리오 검증
 */
@IntegrationTest
class ChallengeReviewServiceCreateReviewEndToEndTest {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final LocalDate END_DATE = LocalDate.of(2025, 6, 6);
    private static final LocalDate START_DATE = END_DATE.minusDays(7);

    @Autowired
    private ChallengeReviewService challengeReviewService;

    @Autowired
    private ChallengeReviewRepository challengeReviewRepository;

    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private ChallengeParticipantRepository challengeParticipantRepository;

    @Autowired
    private ChallengeTeamRepository challengeTeamRepository;

    @Autowired
    private ChallengeTodoRepository challengeTodoRepository;

    @Autowired
    private ChallengeDailyTodoRepository challengeDailyTodoRepository;

    @Autowired
    private ChallengeDailyResultRepository challengeDailyResultRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private NewsletterGroupRepository newsletterGroupRepository;

    @MockitoBean
    private Clock clock;

    private Member viewer;
    private Challenge challenge;
    private ChallengeTeam team;
    private ChallengeTodo reviewTodo;
    private ChallengeParticipant participant;

    @BeforeEach
    void setUp() {
        pinClockTo(END_DATE.atTime(10, 0));

        challengeDailyResultRepository.deleteAllInBatch();
        challengeDailyTodoRepository.deleteAllInBatch();
        challengeReviewRepository.deleteAllInBatch();
        challengeTodoRepository.deleteAllInBatch();
        challengeParticipantRepository.deleteAllInBatch();
        challengeTeamRepository.deleteAllInBatch();
        challengeRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
        newsletterGroupRepository.deleteAllInBatch();

        viewer = memberRepository.save(TestFixture.createUniqueMember("나밍곰", "viewer-provider"));
        NewsletterGroup group = newsletterGroupRepository.save(TestFixture.createNewsletterGroup("그룹"));

        challenge = challengeRepository.save(TestFixture.createChallenge(
                "마지막 날 챌린지", START_DATE, END_DATE, 8, group.getId()
        ));
        team = challengeTeamRepository.save(TestFixture.createChallengeTeam(challenge.getId(), 0));
        reviewTodo = challengeTodoRepository.save(
                TestFixture.createChallengeTodo(challenge.getId(), ChallengeTodoType.REVIEW)
        );
        participant = challengeParticipantRepository.save(
                TestFixture.createChallengeParticipantWithTeam(
                        challenge.getId(), viewer.getId(), team.getId(), 0, 0
                )
        );
    }

    @Test
    void createReview_정상_케이스_리뷰_저장과_출석_처리까지_성공한다() {
        // when
        challengeReviewService.createReview(
                challenge.getId(), viewer, new CreateChallengeReviewRequest("좋았어요", true)
        );

        // then
        ChallengeReview savedReview = challengeReviewRepository
                .findByChallengeIdAndMemberId(challenge.getId(), viewer.getId())
                .orElseThrow();
        ChallengeParticipant updatedParticipant = challengeParticipantRepository
                .findById(participant.getId()).orElseThrow();
        ChallengeTeam updatedTeam = challengeTeamRepository.findById(team.getId()).orElseThrow();

        assertSoftly(softly -> {
            // 1. 서비스: 리뷰 저장 성공
            softly.assertThat(savedReview.getComment()).isEqualTo("좋았어요");
            softly.assertThat(savedReview.isPrivate()).isTrue();

            // 2. 리스너: REVIEW 일일 TODO 기록
            softly.assertThat(challengeDailyTodoRepository
                    .existsByParticipantIdAndTodoDateAndChallengeTodoId(
                            participant.getId(), END_DATE, reviewTodo.getId()
                    )).isTrue();

            // 3. 리스너: COMPLETE 일일 결과 기록
            softly.assertThat(challengeDailyResultRepository
                    .existsByParticipantIdAndDate(participant.getId(), END_DATE)).isTrue();

            // 4. 리스너: 출석 일수/스트릭/최종 참여일 갱신
            softly.assertThat(updatedParticipant.getCompletedDays()).isEqualTo(1);
            softly.assertThat(updatedParticipant.getStreak()).isEqualTo(1);
            softly.assertThat(updatedParticipant.getLastParticipatedDate()).isEqualTo(END_DATE);

            // 5. 리스너: 팀 진행률 갱신 (오늘 COMPLETE 1명 / 생존자 1명 * 100)
            softly.assertThat(updatedTeam.getProgress()).isEqualTo(100);
        });
    }

    @Test
    void createReview_자정_경계_마지막_날_23시_59분에_생성해도_endDate로_출석_처리된다() {
        // given — 챌린지 종료일(endDate) 당일 23:59:30 으로 시간을 고정
        pinClockTo(END_DATE.atTime(23, 59, 30));

        // when
        challengeReviewService.createReview(
                challenge.getId(), viewer, new CreateChallengeReviewRequest("좋았어요", true)
        );

        // then — 출석 기록은 그대로 endDate 로 남는다
        ChallengeParticipant updatedParticipant = challengeParticipantRepository
                .findById(participant.getId()).orElseThrow();
        ChallengeTeam updatedTeam = challengeTeamRepository.findById(team.getId()).orElseThrow();

        assertSoftly(softly -> {
            softly.assertThat(challengeDailyTodoRepository
                    .existsByParticipantIdAndTodoDateAndChallengeTodoId(
                            participant.getId(), END_DATE, reviewTodo.getId()
                    )).isTrue();
            softly.assertThat(challengeDailyResultRepository
                    .existsByParticipantIdAndDate(participant.getId(), END_DATE)).isTrue();
            softly.assertThat(updatedParticipant.getCompletedDays()).isEqualTo(1);
            softly.assertThat(updatedParticipant.getLastParticipatedDate()).isEqualTo(END_DATE);
            softly.assertThat(updatedTeam.getProgress()).isEqualTo(100);
        });
    }

    @Test
    void createReview_자정_경계_endDate_다음날_0시에_생성하면_리뷰만_저장되고_리스너_사이드이펙트는_없다() {
        // given — endDate 가 지난 직후. 리뷰 저장은 허용되지만 출석 인정 기간을 벗어남
        LocalDate dayAfterEnd = END_DATE.plusDays(1);
        pinClockTo(dayAfterEnd.atStartOfDay().plusSeconds(30));

        // when
        challengeReviewService.createReview(
                challenge.getId(), viewer, new CreateChallengeReviewRequest("좋았어요", true)
        );

        // then — 리뷰는 저장되지만 이벤트 미발행으로 리스너 사이드이펙트는 발생하지 않는다
        ChallengeReview savedReview = challengeReviewRepository
                .findByChallengeIdAndMemberId(challenge.getId(), viewer.getId())
                .orElseThrow();
        ChallengeParticipant updatedParticipant = challengeParticipantRepository
                .findById(participant.getId()).orElseThrow();
        ChallengeTeam updatedTeam = challengeTeamRepository.findById(team.getId()).orElseThrow();

        assertSoftly(softly -> {
            softly.assertThat(savedReview.getComment()).isEqualTo("좋았어요");
            softly.assertThat(challengeDailyTodoRepository.count()).isZero();
            softly.assertThat(challengeDailyResultRepository.count()).isZero();
            softly.assertThat(updatedParticipant.getCompletedDays()).isZero();
            softly.assertThat(updatedParticipant.getStreak()).isZero();
            softly.assertThat(updatedParticipant.getLastParticipatedDate()).isNull();
            softly.assertThat(updatedTeam.getProgress()).isZero();
        });
    }

    @Test
    void createReview_실패_비참여자_요청은_리뷰도_저장되지_않고_리스너_사이드이펙트도_없다() {
        // given — 참여자 레코드를 제거해 IDOR 상황 재현
        challengeParticipantRepository.deleteById(participant.getId());

        // when // then
        assertThatThrownBy(() -> challengeReviewService.createReview(
                challenge.getId(), viewer, new CreateChallengeReviewRequest("좋았어요", true)
        )).isInstanceOf(CIllegalArgumentException.class);

        ChallengeTeam updatedTeam = challengeTeamRepository.findById(team.getId()).orElseThrow();

        assertSoftly(softly -> {
            softly.assertThat(challengeReviewRepository.count()).isZero();
            softly.assertThat(challengeDailyTodoRepository.count()).isZero();
            softly.assertThat(challengeDailyResultRepository.count()).isZero();
            softly.assertThat(updatedTeam.getProgress()).isZero();
        });
    }

    private void pinClockTo(LocalDateTime localDateTime) {
        when(clock.instant()).thenReturn(localDateTime.atZone(SEOUL).toInstant());
        when(clock.getZone()).thenReturn(SEOUL);
    }
}
