package me.bombom.api.v1.challenge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.BDDMockito.given;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
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
import me.bombom.api.v1.challenge.dto.response.ChallengeStreakResponse;
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
import me.bombom.api.v1.newsletter.domain.NewsletterGroup;
import me.bombom.api.v1.newsletter.repository.NewsletterGroupRepository;
import me.bombom.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

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

    @Autowired
    private NewsletterGroupRepository newsletterGroupRepository;

    @MockitoBean
    private Clock clock;

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
        newsletterGroupRepository.deleteAllInBatch();

        member = memberRepository.save(
                TestFixture.createUniqueMember("tester", java.util.UUID.randomUUID().toString()));

        NewsletterGroup group = TestFixture.createNewsletterGroup("그룹");
        newsletterGroupRepository.save(group);

        challenge = challengeRepository.save(TestFixture.createChallenge(
                "Test Challenge",
                LocalDate.now().minusDays(5),
                LocalDate.now().plusDays(5),
                10,
                group.getId()));

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

        // Mock Clock behavior
        // Default: LocalDate.now(clock) returns today
        Instant instant = java.time.Instant.now();
        ZoneId zoneId = java.time.ZoneId.systemDefault();
        given(clock.instant()).willReturn(instant);
        given(clock.getZone()).willReturn(zoneId);
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
            softly.assertThat(response.streak()).isEqualTo(0);
            softly.assertThat(response.shield()).isEqualTo(0);
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
        NewsletterGroup survivalGroup = TestFixture.createNewsletterGroup("생존 그룹");
        newsletterGroupRepository.save(survivalGroup);
        Challenge survivalChallenge = challengeRepository.save(TestFixture.createChallenge(
                "Survival Challenge",
                yesterday.minusDays(4),
                yesterday.plusDays(5),
                10,
                survivalGroup.getId()));

        ChallengeParticipant participant = challengeParticipantRepository.save(ChallengeParticipant.builder()
                .challengeId(survivalChallenge.getId())
                .memberId(member.getId())
                .completedDays(3)
                .shield(1)
                .streak(3)
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
            softly.assertThat(updatedParticipant.getStreak()).isEqualTo(3); // 쉴드 사용 시 스트릭 유지

            List<ChallengeDailyResult> results = challengeDailyResultRepository.findAll();
            softly.assertThat(results).hasSize(1);
            softly.assertThat(results.getFirst().getStatus()).isEqualTo(ChallengeDailyStatus.SHIELD);
            softly.assertThat(results.getFirst().getDate()).isEqualTo(yesterday);
        });
    }

    @Test
    void 쉴드_사용_시_스트릭을_유지한다() {
        // given
        LocalDate yesterday = LocalDate.now().minusDays(1);

        NewsletterGroup group = TestFixture.createNewsletterGroup("쉴드 스트릭 그룹");
        newsletterGroupRepository.save(group);
        Challenge streakChallenge = challengeRepository.save(TestFixture.createChallenge(
                "Shield Streak Challenge",
                yesterday.minusDays(4),
                yesterday.plusDays(5),
                10,
                group.getId()));

        ChallengeParticipant participant = challengeParticipantRepository.save(ChallengeParticipant.builder()
                .challengeId(streakChallenge.getId())
                .memberId(member.getId())
                .completedDays(3)
                .shield(1)
                .streak(5)
                .isSurvived(true)
                .build());

        // when
        challengeProgressService.proceedDailySurvivalCheck(streakChallenge, yesterday);

        // then
        ChallengeParticipant updated = challengeParticipantRepository.findById(participant.getId()).orElseThrow();
        assertSoftly(softly -> {
            softly.assertThat(updated.getShield()).isEqualTo(0);
            softly.assertThat(updated.getStreak()).isEqualTo(5);
        });
    }

    @Test
    void 쉴드가_없어도_결석_허용일_이내라면_생존한다() {
        // given
        LocalDate yesterday = LocalDate.now().minusDays(1);

        // Challenge: 총 10일. 80% = 8일. 최대 결석 = 2일
        NewsletterGroup survivalGroup2 = TestFixture.createNewsletterGroup("생존 그룹2");
        newsletterGroupRepository.save(survivalGroup2);
        Challenge survivalChallenge = challengeRepository.save(TestFixture.createChallenge(
                "Survival Challenge 2",
                yesterday.minusDays(4),
                yesterday.plusDays(5),
                10,
                survivalGroup2.getId()));

        // Participant: 챌린지 3일 수행
        // 챌린지 시작한지 5일 지남, currentAbsent = 5 - 3 = 2, 2 >= 2 -> 생존.
        ChallengeParticipant participant = challengeParticipantRepository.save(ChallengeParticipant.builder()
                .challengeId(survivalChallenge.getId())
                .memberId(member.getId())
                .completedDays(3)
                .shield(0)
                .streak(3)
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
            softly.assertThat(updatedParticipant.getStreak()).isEqualTo(0); // 결석 시 스트릭 리셋
        });
    }

    @Test
    void 결석_허용일을_초과하면_생존에_실패한다() {
        // given
        LocalDate yesterday = LocalDate.of(2026, 1, 10).minusDays(1);

        // Challenge: 총 10일. 80% = 8일. 최대 결석 = 2일
        NewsletterGroup survivalGroup3 = TestFixture.createNewsletterGroup("생존 그룹3");
        newsletterGroupRepository.save(survivalGroup3);
        Challenge survivalChallenge = challengeRepository.save(TestFixture.createChallenge(
                "Survival Challenge 3",
                yesterday.minusDays(4),
                yesterday.plusDays(5),
                10,
                survivalGroup3.getId()));

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
        NewsletterGroup otherGroup = TestFixture.createNewsletterGroup("다른 그룹");
        newsletterGroupRepository.save(otherGroup);
        Challenge otherChallenge = challengeRepository.save(TestFixture.createChallenge(
                "Other Challenge",
                LocalDate.now().minusDays(5),
                LocalDate.now().plusDays(5),
                10,
                otherGroup.getId()));
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

    @Test
    void 스트릭이_0이면_빈_배열을_반환한다() {
        // given - streak=0인 참여자 (daily result 없음)
        Challenge streakChallenge = createStreakChallenge("스트릭 0 챌린지");
        challengeParticipantRepository.save(ChallengeParticipant.builder()
                .challengeId(streakChallenge.getId())
                .memberId(member.getId())
                .completedDays(0)
                .streak(0)
                .shield(0)
                .isSurvived(true)
                .build());

        // when
        ChallengeStreakResponse response = challengeProgressService.getMemberStreak(streakChallenge.getId(), member, 5);

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.streak()).isEqualTo(0);
            softly.assertThat(response.streakDays()).hasSize(1);
            softly.assertThat(response.streakDays().getFirst().date()).isEqualTo(LocalDate.now());
            softly.assertThat(response.streakDays().getFirst().isCompleted()).isFalse();
        });
    }

    @Test
    void limit보다_스트릭이_작으면_스트릭_수만큼만_반환한다() {
        // given - streak=3, limit=5
        Challenge streakChallenge = createStreakChallenge("스트릭 3 챌린지");
        ChallengeParticipant participant = challengeParticipantRepository.save(ChallengeParticipant.builder()
                .challengeId(streakChallenge.getId())
                .memberId(member.getId())
                .completedDays(3)
                .streak(3)
                .shield(0)
                .isSurvived(true)
                .build());

        challengeDailyResultRepository.saveAll(List.of(
                createChallengeDailyResult(participant.getId(), LocalDate.now().minusDays(2), ChallengeDailyStatus.COMPLETE),
                createChallengeDailyResult(participant.getId(), LocalDate.now().minusDays(1), ChallengeDailyStatus.COMPLETE),
                createChallengeDailyResult(participant.getId(), LocalDate.now(), ChallengeDailyStatus.COMPLETE)
        ));

        // when
        ChallengeStreakResponse response = challengeProgressService.getMemberStreak(streakChallenge.getId(), member, 5);

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.streak()).isEqualTo(3);
            softly.assertThat(response.streakDays()).hasSize(3);
        });
    }

    @Test
    void limit가_스트릭보다_작으면_limit만큼만_반환한다() {
        // given - streak=7, limit=3
        Challenge streakChallenge = createStreakChallenge("스트릭 7 챌린지");
        ChallengeParticipant participant = challengeParticipantRepository.save(ChallengeParticipant.builder()
                .challengeId(streakChallenge.getId())
                .memberId(member.getId())
                .completedDays(7)
                .streak(7)
                .shield(0)
                .isSurvived(true)
                .build());

        for (int i = 6; i >= 0; i--) {
            challengeDailyResultRepository.save(
                    createChallengeDailyResult(participant.getId(), LocalDate.now().minusDays(i), ChallengeDailyStatus.COMPLETE));
        }

        // when
        ChallengeStreakResponse response = challengeProgressService.getMemberStreak(streakChallenge.getId(), member, 3);

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.streak()).isEqualTo(7);
            softly.assertThat(response.streakDays()).hasSize(3);
        });
    }

    @Test
    void 스트릭_날짜는_오름차순으로_반환한다() {
        // given
        Challenge streakChallenge = createStreakChallenge("날짜 정렬 챌린지");
        ChallengeParticipant participant = challengeParticipantRepository.save(ChallengeParticipant.builder()
                .challengeId(streakChallenge.getId())
                .memberId(member.getId())
                .completedDays(3)
                .streak(3)
                .shield(0)
                .isSurvived(true)
                .build());

        LocalDate today = LocalDate.now();
        challengeDailyResultRepository.saveAll(List.of(
                createChallengeDailyResult(participant.getId(), today.minusDays(2), ChallengeDailyStatus.COMPLETE),
                createChallengeDailyResult(participant.getId(), today.minusDays(1), ChallengeDailyStatus.COMPLETE),
                createChallengeDailyResult(participant.getId(), today, ChallengeDailyStatus.COMPLETE)
        ));

        // when
        ChallengeStreakResponse response = challengeProgressService.getMemberStreak(streakChallenge.getId(), member, 5);

        // then
        assertThat(response.streakDays())
                .extracting("date")
                .containsExactly(today.minusDays(2), today.minusDays(1), today);
    }

    @Test
    void 쉴드_사용_날은_isShieldApplied가_true다() {
        // given
        Challenge streakChallenge = createStreakChallenge("쉴드 확인 챌린지");
        ChallengeParticipant participant = challengeParticipantRepository.save(ChallengeParticipant.builder()
                .challengeId(streakChallenge.getId())
                .memberId(member.getId())
                .completedDays(2)
                .streak(2)
                .shield(0)
                .isSurvived(true)
                .build());

        LocalDate today = LocalDate.now();
        challengeDailyResultRepository.saveAll(List.of(
                createChallengeDailyResult(participant.getId(), today.minusDays(1), ChallengeDailyStatus.SHIELD),
                createChallengeDailyResult(participant.getId(), today, ChallengeDailyStatus.COMPLETE)
        ));

        // when
        ChallengeStreakResponse response = challengeProgressService.getMemberStreak(streakChallenge.getId(), member, 5);

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.streakDays().get(0).isShieldApplied()).isTrue();   // yesterday: SHIELD
            softly.assertThat(response.streakDays().get(1).isShieldApplied()).isFalse();  // today: COMPLETE
        });
    }

    @Test
    void 주말을_사이에_둔_금요일과_월요일도_연속_스트릭으로_조회된다() {
        // given
        // 금→(토일)→월 구간이 포함된 스트릭 (gap 3일 이하 = 연속)
        LocalDate monday = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
        LocalDate friday = monday.minusDays(3); // 직전 금요일

        Challenge streakChallenge = createStreakChallenge("주말 낀 스트릭 챌린지");
        ChallengeParticipant participant = challengeParticipantRepository.save(ChallengeParticipant.builder()
                .challengeId(streakChallenge.getId())
                .memberId(member.getId())
                .completedDays(2)
                .streak(2)
                .shield(0)
                .isSurvived(true)
                .build());

        challengeDailyResultRepository.saveAll(List.of(
                createChallengeDailyResult(participant.getId(), friday, ChallengeDailyStatus.COMPLETE),
                createChallengeDailyResult(participant.getId(), monday, ChallengeDailyStatus.COMPLETE)
        ));

        // when
        ChallengeStreakResponse response = challengeProgressService.getMemberStreak(streakChallenge.getId(), member, 5);

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.streak()).isEqualTo(2);
            softly.assertThat(response.streakDays()).hasSize(2);
            softly.assertThat(response.streakDays().get(0).date()).isEqualTo(friday);
            softly.assertThat(response.streakDays().get(0).dayOfWeek()).isEqualTo(java.time.DayOfWeek.FRIDAY);
            softly.assertThat(response.streakDays().get(1).date()).isEqualTo(monday);
            softly.assertThat(response.streakDays().get(1).dayOfWeek()).isEqualTo(java.time.DayOfWeek.MONDAY);
        });
    }

    @Test
    void 참가하지_않은_챌린지_스트릭_조회시_예외_발생() {
        // given
        Member nonParticipant = memberRepository.save(TestFixture.createUniqueMember("nonParticipant", UUID.randomUUID().toString()));

        // when & then
        assertThatThrownBy(() -> challengeProgressService.getMemberStreak(challenge.getId(), nonParticipant, 5))
                .isInstanceOf(UnauthorizedException.class)
                .satisfies(e -> {
                    UnauthorizedException exception = (UnauthorizedException) e;
                    assertThat(exception.getErrorDetail()).isEqualTo(ErrorDetail.FORBIDDEN_RESOURCE);
                });
    }

    @Test
    void 오늘_미완료시_이전_기록과_오늘_미완료_항목이_함께_반환된다() {
        // given - streak=3, 오늘 미완료
        Challenge streakChallenge = createStreakChallenge("오늘 미완료 챌린지");
        ChallengeParticipant participant = challengeParticipantRepository.save(ChallengeParticipant.builder()
                .challengeId(streakChallenge.getId())
                .memberId(member.getId())
                .completedDays(3)
                .streak(3)
                .shield(0)
                .isSurvived(true)
                .build());

        LocalDate today = LocalDate.now();
        challengeDailyResultRepository.saveAll(List.of(
                createChallengeDailyResult(participant.getId(), today.minusDays(3), ChallengeDailyStatus.COMPLETE),
                createChallengeDailyResult(participant.getId(), today.minusDays(2), ChallengeDailyStatus.COMPLETE),
                createChallengeDailyResult(participant.getId(), today.minusDays(1), ChallengeDailyStatus.COMPLETE)
        ));

        // when - limit=5, 오늘 미완료이므로 최근 4개 + 오늘 미완료 = 4개(streak=3이라 3개) + 오늘
        ChallengeStreakResponse response = challengeProgressService.getMemberStreak(streakChallenge.getId(), member, 5);

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.streak()).isEqualTo(3);
            softly.assertThat(response.streakDays()).hasSize(4);
            softly.assertThat(response.streakDays().getLast().date()).isEqualTo(today);
            softly.assertThat(response.streakDays().getLast().isCompleted()).isFalse();
        });
    }

    @Test
    void 오늘_미완료시_limit_적용은_오늘_포함_기준이다() {
        // given - streak=5, limit=3, 오늘 미완료 → limit-1=2개 + 오늘 = 3개
        Challenge streakChallenge = createStreakChallenge("limit 확인 챌린지");
        ChallengeParticipant participant = challengeParticipantRepository.save(ChallengeParticipant.builder()
                .challengeId(streakChallenge.getId())
                .memberId(member.getId())
                .completedDays(5)
                .streak(5)
                .shield(0)
                .isSurvived(true)
                .build());

        LocalDate today = LocalDate.now();
        for (int i = 5; i >= 1; i--) {
            challengeDailyResultRepository.save(
                    createChallengeDailyResult(participant.getId(), today.minusDays(i), ChallengeDailyStatus.COMPLETE));
        }

        // when
        ChallengeStreakResponse response = challengeProgressService.getMemberStreak(streakChallenge.getId(), member, 3);

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.streakDays()).hasSize(3);
            softly.assertThat(response.streakDays().getLast().date()).isEqualTo(today);
            softly.assertThat(response.streakDays().getLast().isCompleted()).isFalse();
        });
    }

    @Test
    void 오늘_완료시_isCompleted가_true다() {
        // given
        Challenge streakChallenge = createStreakChallenge("오늘 완료 챌린지");
        ChallengeParticipant participant = challengeParticipantRepository.save(ChallengeParticipant.builder()
                .challengeId(streakChallenge.getId())
                .memberId(member.getId())
                .completedDays(2)
                .streak(2)
                .shield(0)
                .isSurvived(true)
                .build());

        LocalDate today = LocalDate.now();
        challengeDailyResultRepository.saveAll(List.of(
                createChallengeDailyResult(participant.getId(), today.minusDays(1), ChallengeDailyStatus.COMPLETE),
                createChallengeDailyResult(participant.getId(), today, ChallengeDailyStatus.COMPLETE)
        ));

        // when
        ChallengeStreakResponse response = challengeProgressService.getMemberStreak(streakChallenge.getId(), member, 5);

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.streakDays()).hasSize(2);
            softly.assertThat(response.streakDays().getLast().date()).isEqualTo(today);
            softly.assertThat(response.streakDays().getLast().isCompleted()).isTrue();
        });
    }

    @Test
    void 연속_참여_스트릭이_응답에_포함된다() {
        // given
        NewsletterGroup group = newsletterGroupRepository.save(TestFixture.createNewsletterGroup("스트릭 그룹"));
        Challenge streakChallenge = challengeRepository.save(TestFixture.createChallenge(
                "Streak Challenge",
                LocalDate.now().minusDays(5),
                LocalDate.now().plusDays(5),
                10,
                group.getId()));

        challengeParticipantRepository.save(ChallengeParticipant.builder()
                .challengeId(streakChallenge.getId())
                .memberId(member.getId())
                .completedDays(3)
                .isSurvived(true)
                .shield(2)
                .streak(5)
                .build());

        challengeTodoRepository.save(TestFixture.createChallengeTodo(streakChallenge.getId(), ChallengeTodoType.READ));
        challengeTodoRepository.save(TestFixture.createChallengeTodo(streakChallenge.getId(), ChallengeTodoType.COMMENT));

        // when
        MemberChallengeProgressResponse response = challengeProgressService.getMemberProgress(streakChallenge.getId(), member);

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.streak()).isEqualTo(5);
            softly.assertThat(response.shield()).isEqualTo(2);
        });
    }

    @Test
    void 주말을_제외한_평일만_계산하여_생존_처리한다() {
        // given
        // 금요일(1일차) 시작
        LocalDate friday = LocalDate.of(2024, 1, 5);
        // 다음주 월요일(4일차)에 체크 (어제인 월요일까지의 생존 여부 판단)
        LocalDate monday = LocalDate.of(2024, 1, 8);

        // 총 10일 (영업일 기준). 2024-01-05 ~ 2024-01-18 (금요일~다음다음 목요일, 14일간, 주말 4일 제외 = 10일)
        NewsletterGroup weekendGroup = TestFixture.createNewsletterGroup("주말 그룹");
        newsletterGroupRepository.save(weekendGroup);
        Challenge weekendChallenge = challengeRepository.save(TestFixture.createChallenge(
                "Weekend Challenge",
                friday,
                friday.plusDays(13), // 2 weeks
                10, // total days (business only)
                weekendGroup.getId()
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
        NewsletterGroup completedGroup = TestFixture.createNewsletterGroup("완료 그룹");
        newsletterGroupRepository.save(completedGroup);
        Challenge completedChallenge = challengeRepository.save(
                TestFixture.createChallenge(
                        "Completed Challenge",
                        yesterday.minusDays(10),
                        yesterday,
                        10,
                        completedGroup.getId()
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
        NewsletterGroup otherCompletedGroup = TestFixture.createNewsletterGroup("다른 완료 그룹");
        newsletterGroupRepository.save(otherCompletedGroup);
        Challenge completedChallenge = challengeRepository.save(
                TestFixture.createChallenge(
                        "Other Challenge",
                        yesterday.minusDays(10),
                        yesterday,
                        10,
                        otherCompletedGroup.getId()
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
        NewsletterGroup failedGroup = TestFixture.createNewsletterGroup("실패 그룹");
        newsletterGroupRepository.save(failedGroup);
        Challenge completedChallenge = challengeRepository.save(
                TestFixture.createChallenge(
                        "Failed Challenge",
                        yesterday.minusDays(10),
                        yesterday,
                        10,
                        failedGroup.getId()
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
        NewsletterGroup gradeGroup = TestFixture.createNewsletterGroup("등급 그룹");
        newsletterGroupRepository.save(gradeGroup);
        Challenge completedChallenge = challengeRepository.save(
                TestFixture.createChallenge(
                        "Grade Challenge",
                        yesterday.minusDays(10),
                        yesterday,
                        10,
                        gradeGroup.getId()
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

    @Test
    void 챌린지_1일차에는_MINDSET_투두만_조회된다() {
        // given
        LocalDate today = LocalDate.now();
        NewsletterGroup day1Group = TestFixture.createNewsletterGroup("1일차 그룹");
        newsletterGroupRepository.save(day1Group);
        Challenge day1Challenge = challengeRepository.save(TestFixture.createChallenge(
                "Day 1 Challenge",
                today,
                today.plusDays(9),
                10,
                day1Group.getId()));

        challengeParticipantRepository.save(TestFixture.createChallengeParticipant(
                day1Challenge.getId(),
                member.getId(),
                0));

        // Todos: MINDSET, READ, COMMENT 모두 생성
        challengeTodoRepository
                .save(TestFixture.createChallengeTodo(day1Challenge.getId(),
                        ChallengeTodoType.MINDSET));
        challengeTodoRepository
                .save(TestFixture.createChallengeTodo(day1Challenge.getId(), ChallengeTodoType.READ));
        challengeTodoRepository
                .save(TestFixture.createChallengeTodo(day1Challenge.getId(),
                        ChallengeTodoType.COMMENT));

        // when
        MemberChallengeProgressResponse response = challengeProgressService
                .getMemberProgress(day1Challenge.getId(), member);

        // then
        assertThat(response.todayTodos()).hasSize(1);
        assertThat(response.todayTodos().getFirst().challengeTodoType()).isEqualTo(ChallengeTodoType.MINDSET);
    }

    @Test
    void 챌린지_2일차_이후에는_READ_COMMENT_투두만_조회된다() {
        // given
        // LocalDate.now()가 주말일 경우를 대비하여, 시작일을 충분히 과거로 설정하여
        // 현재 날짜가 챌린지 2일차 이후의 평일이 되도록 보장
        LocalDate realToday = LocalDate.now();
        // 시작일을 현재 날짜로부터 3일 전으로 설정.
        // 이렇게 하면 realToday가 월요일이든, 화요일이든, 수요일이든, 목요일이든, 금요일이든
        // 챌린지 시작일로부터 최소 2일 이상의 평일이 지났음을 보장할 수 있다.
        // (예: realToday가 수요일이면 시작일은 일요일. 월, 화 2일 경과)
        // (예: realToday가 월요일이면 시작일은 금요일. 금, 월 2일 경과)
        LocalDate safeStartDate = realToday.minusDays(3);

        NewsletterGroup day2Group = TestFixture.createNewsletterGroup("2일차 그룹");
        newsletterGroupRepository.save(day2Group);
        Challenge day2ChallengeFixed = challengeRepository.save(TestFixture.createChallenge(
                "Day 2 Challenge",
                safeStartDate,
                safeStartDate.plusDays(9),
                10,
                day2Group.getId()));

        challengeParticipantRepository.save(TestFixture.createChallengeParticipant(
                day2ChallengeFixed.getId(),
                member.getId(),
                1)); // completedDays를 1로 설정하여 2일차로 가정

        // Todos: MINDSET, READ, COMMENT 모두 생성
        challengeTodoRepository.save(
                TestFixture.createChallengeTodo(day2ChallengeFixed.getId(), ChallengeTodoType.MINDSET));
        challengeTodoRepository.save(
                TestFixture.createChallengeTodo(day2ChallengeFixed.getId(), ChallengeTodoType.READ));
        challengeTodoRepository.save(
                TestFixture.createChallengeTodo(day2ChallengeFixed.getId(), ChallengeTodoType.COMMENT));

        // when
        MemberChallengeProgressResponse response = challengeProgressService
                .getMemberProgress(day2ChallengeFixed.getId(), member);

        // then
        assertThat(response.todayTodos()).hasSize(2);
        assertThat(response.todayTodos()).extracting("challengeTodoType")
                .containsExactlyInAnyOrder(ChallengeTodoType.READ, ChallengeTodoType.COMMENT);
        assertThat(response.todayTodos()).extracting("challengeTodoType")
                .doesNotContain(ChallengeTodoType.MINDSET);
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

    private Challenge createStreakChallenge(String name) {
        NewsletterGroup group = newsletterGroupRepository.save(TestFixture.createNewsletterGroup(name + " 그룹"));
        return challengeRepository.save(TestFixture.createChallenge(
                name,
                LocalDate.now().minusDays(10),
                LocalDate.now().plusDays(10),
                10,
                group.getId()));
    }
}
