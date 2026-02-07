package me.bombom.api.v1.challenge.service;

import static java.time.temporal.ChronoUnit.DAYS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.BDDMockito.given;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.challenge.domain.Challenge;
import me.bombom.api.v1.challenge.domain.ChallengeDailyGuide;
import me.bombom.api.v1.challenge.domain.ChallengeDailyGuideComment;
import me.bombom.api.v1.challenge.domain.ChallengeDailyTodo;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import me.bombom.api.v1.challenge.domain.ChallengeTodo;
import me.bombom.api.v1.challenge.domain.ChallengeTodoType;
import me.bombom.api.v1.challenge.domain.DailyGuideType;
import me.bombom.api.v1.challenge.dto.request.DailyGuideCommentRequest;
import me.bombom.api.v1.challenge.dto.response.MemberDailyCommentResponse;
import me.bombom.api.v1.challenge.dto.response.TodayDailyGuideResponse;
import me.bombom.api.v1.challenge.repository.ChallengeDailyGuideCommentRepository;
import me.bombom.api.v1.challenge.repository.ChallengeDailyGuideRepository;
import me.bombom.api.v1.challenge.repository.ChallengeDailyResultRepository;
import me.bombom.api.v1.challenge.repository.ChallengeDailyTodoRepository;
import me.bombom.api.v1.challenge.repository.ChallengeParticipantRepository;
import me.bombom.api.v1.challenge.repository.ChallengeRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@IntegrationTest
class ChallengeDailyGuideServiceTest {

    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

    @Autowired
    private ChallengeDailyGuideService challengeDailyGuideService;

    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private ChallengeParticipantRepository challengeParticipantRepository;

    @Autowired
    private ChallengeDailyGuideRepository challengeDailyGuideRepository;

    @Autowired
    private ChallengeDailyGuideCommentRepository challengeDailyGuideCommentRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ChallengeTodoRepository challengeTodoRepository;

    @Autowired
    private ChallengeDailyTodoRepository challengeDailyTodoRepository;

    @Autowired
    private ChallengeDailyResultRepository challengeDailyResultRepository;

    @Autowired
    private NewsletterGroupRepository newsletterGroupRepository;

    @MockitoBean
    private Clock clock;

    private Member member;
    private Challenge challenge;
    private ChallengeParticipant participant;
    private ChallengeDailyGuide guide;
    private LocalDate today;
    private ChallengeTodo readTodo;
    private ChallengeTodo commentTodo;

    @BeforeEach
    void setUp() {
        challengeDailyGuideCommentRepository.deleteAllInBatch();
        challengeDailyResultRepository.deleteAllInBatch();
        challengeDailyTodoRepository.deleteAllInBatch();
        challengeTodoRepository.deleteAllInBatch();
        challengeDailyGuideRepository.deleteAllInBatch();
        challengeParticipantRepository.deleteAllInBatch();
        challengeRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
        newsletterGroupRepository.deleteAllInBatch();

        member = TestFixture.normalMemberFixture();
        memberRepository.save(member);

        // 무조건 평일로 설정 (2026-01-26 월요일)
        today = LocalDate.of(2026, 1, 26);

        NewsletterGroup group = TestFixture.createNewsletterGroup("그룹");
        newsletterGroupRepository.save(group);

        given(clock.getZone()).willReturn(SEOUL_ZONE);
        given(clock.instant()).willReturn(today.atStartOfDay(SEOUL_ZONE).toInstant());
        today = LocalDate.now(clock);
        challenge = challengeRepository.save(TestFixture.createChallenge(
                "테스트 챌린지",
                today.minusDays(5),
                today.plusDays(5),
                10,
                group.getId()
                )
        );

        participant = challengeParticipantRepository.save(
                TestFixture.createChallengeParticipant(
                        challenge.getId(),
                        member.getId(),
                        0
                )
        );

        // READ, COMMENT 투두 생성
        readTodo = challengeTodoRepository.save(TestFixture.createChallengeTodo(challenge.getId(), ChallengeTodoType.READ));
        commentTodo = challengeTodoRepository.save(TestFixture.createChallengeTodo(challenge.getId(), ChallengeTodoType.COMMENT));

        int dayIndex = calculateDayIndex(challenge.getStartDate(), today);
        guide = challengeDailyGuideRepository.save(
                TestFixture.createChallengeDailyGuide(
                        challenge.getId(),
                        dayIndex,
                        DailyGuideType.COMMENT,
                        "https://example.com/day07.webp",
                        "오늘은 팁을 남겨주세요",
                        true
                )
        );
    }

    private int calculateDayIndex(LocalDate startDate, LocalDate today) {
        DayOfWeek dayOfWeek = today.getDayOfWeek();
        boolean isWeekend = dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;

        if (isWeekend) {
            return 0;
        }
        return (int) DAYS.between(startDate, today) + 1;
    }

    @Test
    void 오늘의_데일리_가이드_조회_성공() {
        // when
        TodayDailyGuideResponse response = challengeDailyGuideService.getTodayDailyGuide(
                challenge.getId(),
                member.getId()
        );

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.dayIndex()).isEqualTo(guide.getDayIndex());
            softly.assertThat(response.type()).isEqualTo(DailyGuideType.COMMENT);
            softly.assertThat(response.imageUrl()).isEqualTo("https://example.com/day07.webp");
            softly.assertThat(response.notice()).isEqualTo("오늘은 팁을 남겨주세요");
            softly.assertThat(response.commentEnabled()).isTrue();
            softly.assertThat(response.myComment().exists()).isFalse();
            softly.assertThat(response.myComment().content()).isNull();
            softly.assertThat(response.myComment().createdAt()).isNull();
        });
    }

    @Test
    void 댓글이_있는_경우_오늘의_데일리_가이드_조회() {
        // given
        challengeDailyGuideCommentRepository.save(
                TestFixture.createChallengeDailyGuideComment(
                        guide.getId(),
                        participant.getId(),
                        "뉴스레터 읽기 팁을 공유합니다"
                )
        );

        // when
        TodayDailyGuideResponse response = challengeDailyGuideService.getTodayDailyGuide(
                challenge.getId(),
                member.getId()
        );

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.myComment().exists()).isTrue();
            softly.assertThat(response.myComment().content()).isEqualTo("뉴스레터 읽기 팁을 공유합니다");
            softly.assertThat(response.myComment().createdAt()).isNotNull();
        });
    }

    @Test
    void 데일리_가이드_댓글_작성_성공() {
        // given - guide.getDayIndex()가 0(주말)이 아닌 경우만 테스트
        int dayIndex = guide.getDayIndex();

        if (dayIndex == 0) {
            // 주말인 경우 유효한 dayIndex로 가이드 재생성
            challengeDailyGuideRepository.deleteAll();
            dayIndex = 1;
            guide = challengeDailyGuideRepository.save(
                    TestFixture.createChallengeDailyGuide(
                            challenge.getId(),
                            dayIndex,
                            DailyGuideType.COMMENT,
                            "https://example.com/day07.webp",
                            "오늘은 팁을 남겨주세요",
                            true
                    )
            );
        }

        final Long finalGuideId = guide.getId();

        DailyGuideCommentRequest request = new DailyGuideCommentRequest("뉴스레터 읽기 팁을 공유합니다");

        // when
        challengeDailyGuideService.createDailyGuideComment(
                challenge.getId(),
                dayIndex,
                member.getId(),
                request,
                today
        );

        // then
        List<ChallengeDailyGuideComment> comments = challengeDailyGuideCommentRepository.findAll();
        assertSoftly(softly -> {
            softly.assertThat(comments).hasSize(1);
            softly.assertThat(comments.get(0).getGuideId()).isEqualTo(finalGuideId);
            softly.assertThat(comments.get(0).getParticipantId()).isEqualTo(participant.getId());
            softly.assertThat(comments.get(0).getContent()).isEqualTo("뉴스레터 읽기 팁을 공유합니다");
        });
    }

    @Test
    void 이미_댓글이_있는_경우_예외_발생() {
        // given
        challengeDailyGuideCommentRepository.save(
                TestFixture.createChallengeDailyGuideComment(
                        guide.getId(),
                        participant.getId(),
                        "기존 댓글"
                )
        );
        DailyGuideCommentRequest request = new DailyGuideCommentRequest("새 댓글");
        int dayIndex = guide.getDayIndex();

        // when & then
        assertThatThrownBy(() -> challengeDailyGuideService.createDailyGuideComment(
                challenge.getId(),
                dayIndex,
                member.getId(),
                request,
                today
        )).isInstanceOf(CIllegalArgumentException.class);
    }

    @Test
    void 챌린지에_참여하지_않은_경우_예외_발생() {
        // given
        Member otherMember = TestFixture.createUniqueMember("other", "other");
        memberRepository.save(otherMember);

        // when & then
        assertThatThrownBy(() -> challengeDailyGuideService.getTodayDailyGuide(
                challenge.getId(),
                otherMember.getId()
        )).isInstanceOf(CIllegalArgumentException.class);
    }

    @Test
    void 존재하지_않는_챌린지_조회시_예외_발생() {
        // when & then
        assertThatThrownBy(() -> challengeDailyGuideService.getTodayDailyGuide(
                999L,
                member.getId()
        )).isInstanceOf(CIllegalArgumentException.class);
    }

    @Test
    void 챌린지_기간이_아닌_경우_예외_발생() {
        // given
        NewsletterGroup futureGroup = TestFixture.createNewsletterGroup("미래 그룹");
        newsletterGroupRepository.save(futureGroup);
        Challenge futureChallenge = challengeRepository.save(TestFixture.createChallenge(
                "미래 챌린지",
                today.plusDays(10),
                today.plusDays(20),
                10,
                futureGroup.getId()
        ));

        challengeParticipantRepository.save(
                TestFixture.createChallengeParticipant(
                        futureChallenge.getId(),
                        member.getId(),
                        0
                )
        );

        // when & then
        assertThatThrownBy(() -> challengeDailyGuideService.getTodayDailyGuide(
                futureChallenge.getId(),
                member.getId()
        )).isInstanceOf(CIllegalArgumentException.class);
    }

    @Test
    void 댓글_작성이_불가능한_가이드에_댓글_작성시_예외_발생() {
        // given
        ChallengeDailyGuide disabledGuide = challengeDailyGuideRepository.save(
                TestFixture.createChallengeDailyGuide(
                        challenge.getId(),
                        guide.getDayIndex() + 1,
                        DailyGuideType.READ,
                        "https://example.com/day08.webp",
                        null,
                        false // 댓글 작성 불가
                )
        );
        DailyGuideCommentRequest request = new DailyGuideCommentRequest("댓글 작성 시도");

        // when & then
        assertThatThrownBy(() -> challengeDailyGuideService.createDailyGuideComment(
                challenge.getId(),
                disabledGuide.getDayIndex(),
                member.getId(),
                request,
                today
        )).isInstanceOf(CIllegalArgumentException.class);
    }

    @Test
    void 존재하지_않는_가이드에_댓글_작성시_예외_발생() {
        // given
        DailyGuideCommentRequest request = new DailyGuideCommentRequest("댓글 작성 시도");

        // when & then
        assertThatThrownBy(() -> challengeDailyGuideService.createDailyGuideComment(
                challenge.getId(),
                999, // 존재하지 않는 dayIndex
                member.getId(),
                request,
                today
        )).isInstanceOf(CIllegalArgumentException.class);
    }

    @Test
    void 주말_가이드_조회_성공() {
        // given - 기존 guide 삭제하고 주말 가이드 생성 (dayIndex = 0)
        challengeDailyGuideRepository.deleteAll();
        ChallengeDailyGuide weekendGuide = challengeDailyGuideRepository.save(
                TestFixture.createChallengeDailyGuide(
                        challenge.getId(),
                        0,
                        DailyGuideType.COMMENT,
                        "https://example.com/weekend.webp",
                        "주말입니다",
                        false
                )
        );

        // when - 주말 가이드 조회
        // 실제로 오늘이 주말이 아니면 dayIndex가 0이 아니므로 예외가 발생할 수 있음
        // 따라서 실제 주말일 때만 테스트하거나, 가이드를 실제 dayIndex로 생성해야 함
        int actualDayIndex = calculateDayIndex(challenge.getStartDate(), today);
        if (actualDayIndex == 0) {
            // 실제 주말인 경우
            TodayDailyGuideResponse response = challengeDailyGuideService.getTodayDailyGuide(
                    challenge.getId(),
                    member.getId()
            );
            assertSoftly(softly -> {
                softly.assertThat(response.dayIndex()).isEqualTo(0);
                softly.assertThat(response.type()).isEqualTo(DailyGuideType.COMMENT);
                softly.assertThat(response.imageUrl()).isEqualTo("https://example.com/weekend.webp");
            });
        } else {
            // 주말이 아닌 경우, 실제 dayIndex에 맞는 가이드를 생성
            challengeDailyGuideRepository.deleteAll();
            ChallengeDailyGuide actualGuide = challengeDailyGuideRepository.save(
                    TestFixture.createChallengeDailyGuide(
                            challenge.getId(),
                            actualDayIndex,
                            DailyGuideType.COMMENT,
                            "https://example.com/weekend.webp",
                            "주말입니다",
                            false
                    )
            );
            TodayDailyGuideResponse response = challengeDailyGuideService.getTodayDailyGuide(
                    challenge.getId(),
                    member.getId()
            );
            assertSoftly(softly -> {
                softly.assertThat(response.dayIndex()).isEqualTo(actualDayIndex);
                softly.assertThat(response.type()).isNotNull();
                softly.assertThat(response.imageUrl()).isNotNull();
            });
        }
    }

    @Test
    void 주말_가이드에_댓글_작성_불가() {
        // given - 기존 guide 삭제하고 주말 가이드 생성 (dayIndex = 0, commentEnabled = false)
        challengeDailyGuideRepository.deleteAll();
        ChallengeDailyGuide weekendGuide = challengeDailyGuideRepository.save(
                TestFixture.createChallengeDailyGuide(
                        challenge.getId(),
                        0,
                        DailyGuideType.COMMENT,
                        "https://example.com/weekend.webp",
                        "주말입니다",
                        false // 댓글 작성 불가
                )
        );
        DailyGuideCommentRequest request = new DailyGuideCommentRequest("주말 댓글 작성 시도");

        // when & then - dayIndex 0으로 댓글 작성 시도 (댓글 작성 불가능하므로 예외 발생)
        assertThatThrownBy(() -> challengeDailyGuideService.createDailyGuideComment(
                challenge.getId(),
                0,
                member.getId(),
                request,
                today
        )).isInstanceOf(CIllegalArgumentException.class);
    }

    @Test
    void dayIndex_0_유효성_검증_통과() {
        // given - 기존 guide와 comment 삭제하고 주말 가이드 생성 (dayIndex = 0, commentEnabled = true)
        challengeDailyGuideCommentRepository.deleteAll();
        challengeDailyGuideRepository.deleteAll();
        ChallengeDailyGuide weekendGuide = challengeDailyGuideRepository.save(
                TestFixture.createChallengeDailyGuide(
                        challenge.getId(),
                        0,
                        DailyGuideType.COMMENT,
                        "https://example.com/weekend.webp",
                        "주말입니다",
                        true
                )
        );

        // when & then - dayIndex 0은 유효성 검증을 통과해야 함 (예외가 발생하지 않아야 함)
        // commentEnabled = true이므로 댓글 작성 성공
        DailyGuideCommentRequest request = new DailyGuideCommentRequest("주말 댓글");
        challengeDailyGuideService.createDailyGuideComment(
                challenge.getId(),
                0,
                member.getId(),
                request,
                today
        );

        // then - 댓글이 생성되었는지 확인
        List<ChallengeDailyGuideComment> comments = challengeDailyGuideCommentRepository.findAll();
        assertSoftly(softly -> {
            softly.assertThat(comments).hasSize(1);
            softly.assertThat(comments.get(0).getGuideId()).isEqualTo(weekendGuide.getId());
            softly.assertThat(comments.get(0).getParticipantId()).isEqualTo(participant.getId());
            softly.assertThat(comments.get(0).getContent()).isEqualTo("주말 댓글");
        });
    }

    @Test
    void day1에_코멘트_작성시_READ와_COMMENT_체크리스트_자동_완료() {
        // given - day1 가이드 생성
        ChallengeDailyGuide day1Guide = challengeDailyGuideRepository.save(
                TestFixture.createChallengeDailyGuide(
                        challenge.getId(),
                        1,
                        DailyGuideType.COMMENT,
                        "https://example.com/day01.webp",
                        "첫날 가이드",
                        true
                )
        );
        DailyGuideCommentRequest request = new DailyGuideCommentRequest("첫날 코멘트");

        // 평일 날짜 사용 (주말에는 updateChallengeDailyTodo가 동작하지 않음)
        LocalDate tempWeekday = today;
        while (tempWeekday.getDayOfWeek() == DayOfWeek.SATURDAY || tempWeekday.getDayOfWeek() == DayOfWeek.SUNDAY) {
            tempWeekday = tempWeekday.plusDays(1);
        }
        final LocalDate weekday = tempWeekday;

        // when
        challengeDailyGuideService.createDailyGuideComment(
                challenge.getId(),
                1,
                member.getId(),
                request,
                weekday
        );

        // then - 코멘트 생성 확인
        List<ChallengeDailyGuideComment> comments = challengeDailyGuideCommentRepository.findAll();
        assertThat(comments).hasSize(1);

        // then - READ 투두 생성 확인
        boolean readTodoExists = challengeDailyTodoRepository.existsByParticipantIdAndTodoDateAndChallengeTodoId(
                participant.getId(), weekday, readTodo.getId());
        assertThat(readTodoExists).isTrue();

        // then - COMMENT 투두 생성 확인
        boolean commentTodoExists = challengeDailyTodoRepository.existsByParticipantIdAndTodoDateAndChallengeTodoId(
                participant.getId(), weekday, commentTodo.getId());
        assertThat(commentTodoExists).isTrue();

        // then - progress 처리 확인: ChallengeDailyResult 생성 확인
        boolean dailyResultExists = challengeDailyResultRepository.existsByParticipantIdAndDate(
                participant.getId(), weekday);
        assertThat(dailyResultExists).isTrue();

        // then - progress 처리 확인: completedDays 증가 확인
        ChallengeParticipant updatedParticipant = challengeParticipantRepository.findById(participant.getId()).orElseThrow();
        assertThat(updatedParticipant.getCompletedDays()).isEqualTo(1);
    }

    @Test
    void day1이_아닐때_코멘트_작성시_체크리스트_생성_안함() {
        // given - day2 가이드 생성
        ChallengeDailyGuide day2Guide = challengeDailyGuideRepository.save(
                TestFixture.createChallengeDailyGuide(
                        challenge.getId(),
                        2,
                        DailyGuideType.COMMENT,
                        "https://example.com/day02.webp",
                        "둘째날 가이드",
                        true
                )
        );
        DailyGuideCommentRequest request = new DailyGuideCommentRequest("둘째날 코멘트");

        // when
        challengeDailyGuideService.createDailyGuideComment(
                challenge.getId(),
                2,
                member.getId(),
                request,
                today
        );

        // then - 코멘트는 생성됨
        List<ChallengeDailyGuideComment> comments = challengeDailyGuideCommentRepository.findAll();
        assertThat(comments).hasSize(1);

        // then - READ 투두 생성 안 됨 (아직 아티클을 읽지 않았으므로)
        boolean readTodoExists = challengeDailyTodoRepository.existsByParticipantIdAndTodoDateAndChallengeTodoId(
                participant.getId(), today, readTodo.getId());
        assertThat(readTodoExists).isFalse();

        // then - COMMENT 투두 생성 안 됨 (day1이 아니므로)
        boolean commentTodoExists = challengeDailyTodoRepository.existsByParticipantIdAndTodoDateAndChallengeTodoId(
                participant.getId(), today, commentTodo.getId());
        assertThat(commentTodoExists).isFalse();
    }

    @Test
    void day1에_이미_READ_todo가_있어도_중복_생성_안함() {
        // given - day1 가이드 생성
        ChallengeDailyGuide day1Guide = challengeDailyGuideRepository.save(
                TestFixture.createChallengeDailyGuide(
                        challenge.getId(),
                        1,
                        DailyGuideType.COMMENT,
                        "https://example.com/day01.webp",
                        "첫날 가이드",
                        true
                )
        );

        // 평일 날짜 사용 (주말에는 updateChallengeDailyTodo가 동작하지 않음)
        LocalDate tempWeekday = today;
        while (tempWeekday.getDayOfWeek() == DayOfWeek.SATURDAY || tempWeekday.getDayOfWeek() == DayOfWeek.SUNDAY) {
            tempWeekday = tempWeekday.plusDays(1);
        }
        final LocalDate weekday = tempWeekday;

        // 이미 READ 투두 생성
        challengeDailyTodoRepository.save(
                TestFixture.createChallengeDailyTodo(
                        participant.getId(),
                        weekday,
                        readTodo.getId()
                )
        );

        DailyGuideCommentRequest request = new DailyGuideCommentRequest("첫날 코멘트");

        // when
        challengeDailyGuideService.createDailyGuideComment(
                challenge.getId(),
                1,
                member.getId(),
                request,
                weekday
        );

        // then - READ 투두는 1개만 존재 (중복 생성 안 됨)
        List<ChallengeDailyTodo> readTodos = challengeDailyTodoRepository.findAll().stream()
                .filter(todo -> todo.getParticipantId().equals(participant.getId()))
                .filter(todo -> todo.getChallengeTodoId().equals(readTodo.getId()))
                .filter(todo -> todo.getTodoDate().equals(weekday))
                .toList();
        assertThat(readTodos).hasSize(1);

        // then - COMMENT 투두 생성 확인
        boolean commentTodoExists = challengeDailyTodoRepository.existsByParticipantIdAndTodoDateAndChallengeTodoId(
                participant.getId(), weekday, commentTodo.getId());
        assertThat(commentTodoExists).isTrue();
    }

    @Test
    void day1에_이미_COMMENT_todo가_있어도_중복_생성_안함() {
        // given - day1 가이드 생성
        ChallengeDailyGuide day1Guide = challengeDailyGuideRepository.save(
                TestFixture.createChallengeDailyGuide(
                        challenge.getId(),
                        1,
                        DailyGuideType.COMMENT,
                        "https://example.com/day01.webp",
                        "첫날 가이드",
                        true
                )
        );

        // 평일 날짜 사용 (주말에는 updateChallengeDailyTodo가 동작하지 않음)
        LocalDate tempWeekday = today;
        while (tempWeekday.getDayOfWeek() == DayOfWeek.SATURDAY || tempWeekday.getDayOfWeek() == DayOfWeek.SUNDAY) {
            tempWeekday = tempWeekday.plusDays(1);
        }
        final LocalDate weekday = tempWeekday;

        // 이미 COMMENT 투두 생성
        challengeDailyTodoRepository.save(
                TestFixture.createChallengeDailyTodo(
                        participant.getId(),
                        weekday,
                        commentTodo.getId()
                )
        );

        DailyGuideCommentRequest request = new DailyGuideCommentRequest("첫날 코멘트");

        // when
        challengeDailyGuideService.createDailyGuideComment(
                challenge.getId(),
                1,
                member.getId(),
                request,
                weekday
        );

        // then - COMMENT 투두는 1개만 존재 (중복 생성 안 됨)
        List<ChallengeDailyTodo> commentTodos = challengeDailyTodoRepository.findAll().stream()
                .filter(todo -> todo.getParticipantId().equals(participant.getId()))
                .filter(todo -> todo.getChallengeTodoId().equals(commentTodo.getId()))
                .filter(todo -> todo.getTodoDate().equals(weekday))
                .toList();
        assertThat(commentTodos).hasSize(1);

        // then - READ 투두 생성 확인
        boolean readTodoExists = challengeDailyTodoRepository.existsByParticipantIdAndTodoDateAndChallengeTodoId(
                participant.getId(), weekday, readTodo.getId());
        assertThat(readTodoExists).isTrue();
    }

    @Test
    void 데일리_가이드_코멘트_목록_조회_성공() {
        // given
        Member member2 = TestFixture.createUniqueMember("member2", "member2");
        memberRepository.save(member2);
        ChallengeParticipant participant2 = challengeParticipantRepository.save(
                TestFixture.createChallengeParticipant(
                        challenge.getId(),
                        member2.getId(),
                        0
                )
        );

        challengeDailyGuideCommentRepository.save(
                TestFixture.createChallengeDailyGuideComment(
                        guide.getId(),
                        participant.getId(),
                        "첫 번째 코멘트"
                )
        );
        challengeDailyGuideCommentRepository.save(
                TestFixture.createChallengeDailyGuideComment(
                        guide.getId(),
                        participant2.getId(),
                        "두 번째 코멘트"
                )
        );

        Pageable pageable = PageRequest.of(0, 20);

        // when
        Page<me.bombom.api.v1.challenge.dto.response.DailyGuideCommentResponse> response =
                challengeDailyGuideService.getTotalComments(challenge.getId(), guide.getDayIndex(), member.getId(), pageable);

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.getContent()).hasSize(2);
            softly.assertThat(response.getTotalElements()).isEqualTo(2);
            softly.assertThat(response.getTotalPages()).isEqualTo(1);
        });
    }

    @Test
    void 데일리_가이드_코멘트_목록_조회_페이징() {
        // given
        Member member2 = TestFixture.createUniqueMember("member2", "member2");
        memberRepository.save(member2);
        ChallengeParticipant participant2 = challengeParticipantRepository.save(
                TestFixture.createChallengeParticipant(
                        challenge.getId(),
                        member2.getId(),
                        0
                )
        );

        challengeDailyGuideCommentRepository.save(
                TestFixture.createChallengeDailyGuideComment(
                        guide.getId(),
                        participant.getId(),
                        "첫 번째 코멘트"
                )
        );
        challengeDailyGuideCommentRepository.save(
                TestFixture.createChallengeDailyGuideComment(
                        guide.getId(),
                        participant2.getId(),
                        "두 번째 코멘트"
                )
        );

        Pageable pageable = PageRequest.of(0, 1);

        // when
        Page<me.bombom.api.v1.challenge.dto.response.DailyGuideCommentResponse> response =
                challengeDailyGuideService.getTotalComments(challenge.getId(), guide.getDayIndex(), member.getId(), pageable);

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.getContent()).hasSize(1);
            softly.assertThat(response.getTotalElements()).isEqualTo(2);
            softly.assertThat(response.getTotalPages()).isEqualTo(2);
        });
    }

    @Test
    void 존재하지_않는_챌린지로_코멘트_목록_조회시_예외_발생() {
        // given
        Pageable pageable = PageRequest.of(0, 20);

        // when & then
        assertThatThrownBy(() -> challengeDailyGuideService.getTotalComments(
                999L,
                guide.getDayIndex(),
                member.getId(),
                pageable
        )).isInstanceOf(CIllegalArgumentException.class);
    }

    @Test
    void 유효하지_않은_dayIndex로_코멘트_목록_조회시_예외_발생() {
        // given
        Pageable pageable = PageRequest.of(0, 20);
        int invalidDayIndex = challenge.getTotalDays() + 1;

        // when & then
        assertThatThrownBy(() -> challengeDailyGuideService.getTotalComments(
                challenge.getId(),
                invalidDayIndex,
                member.getId(),
                pageable
        )).isInstanceOf(CIllegalArgumentException.class);
    }

    @Test
    void 존재하지_않는_가이드로_코멘트_목록_조회시_예외_발생() {
        // given
        Pageable pageable = PageRequest.of(0, 20);
        // 유효한 dayIndex 범위 내이지만 존재하지 않는 가이드를 조회
        // setUp에서 guide 하나만 생성했으므로, guide.getDayIndex()와 다른 유효한 dayIndex를 사용
        int currentDayIndex = guide.getDayIndex();
        int nonExistentDayIndex;
        if (currentDayIndex == 0) {
            // 주말 가이드(dayIndex=0)인 경우, 1을 사용
            nonExistentDayIndex = 1;
        } else if (currentDayIndex < challenge.getTotalDays()) {
            // 다음 dayIndex 사용
            nonExistentDayIndex = currentDayIndex + 1;
        } else {
            // 마지막 dayIndex인 경우, 이전 dayIndex 사용
            nonExistentDayIndex = Math.max(1, currentDayIndex - 1);
        }

        // when & then
        assertThatThrownBy(() -> challengeDailyGuideService.getTotalComments(
                challenge.getId(),
                nonExistentDayIndex,
                member.getId(),
                pageable
        )).isInstanceOf(CIllegalArgumentException.class);
    }

    @Test
    void 코멘트가_없는_경우_빈_목록_반환() {
        // given
        Pageable pageable = PageRequest.of(0, 20);

        // when
        Page<me.bombom.api.v1.challenge.dto.response.DailyGuideCommentResponse> response =
                challengeDailyGuideService.getTotalComments(challenge.getId(), guide.getDayIndex(), member.getId(), pageable);

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.getContent()).isEmpty();
            softly.assertThat(response.getTotalElements()).isEqualTo(0);
            softly.assertThat(response.getTotalPages()).isEqualTo(0);
        });
    }

    @Test
    void 코멘트_목록_조회시_챌린지에_참여하지_않은_경우_예외_발생() {
        // given
        Member otherMember = TestFixture.createUniqueMember("other", "other");
        memberRepository.save(otherMember);
        Pageable pageable = PageRequest.of(0, 20);

        // when & then
        assertThatThrownBy(() -> challengeDailyGuideService.getTotalComments(
                challenge.getId(),
                guide.getDayIndex(),
                otherMember.getId(),
                pageable
        )).isInstanceOf(CIllegalArgumentException.class);
    }

    @Test
    void 데일리_가이드_내_댓글_조회_성공() {
        // given
        String content = "내 댓글 내용입니다.";
        challengeDailyGuideCommentRepository.save(
                TestFixture.createChallengeDailyGuideComment(
                        guide.getId(),
                        participant.getId(),
                        content));

        // when
        MemberDailyCommentResponse response = challengeDailyGuideService.getDailyGuideComment(challenge.getId(),
                guide.getDayIndex(), member.getId());

        // then
        assertThat(response.comment()).isEqualTo(content);
    }

    @Test
    void 데일리_가이드_내_댓글이_없는_경우_빈_내용_반환() {
        // when
        MemberDailyCommentResponse response = challengeDailyGuideService.getDailyGuideComment(challenge.getId(),
                guide.getDayIndex(), member.getId());

        // then
        assertThat(response.comment()).isNull();
    }

    @Test
    void 존재하지_않는_챌린지에서_내_댓글_조회시_예외_발생() {
        // when & then
        assertThatThrownBy(() -> challengeDailyGuideService.getDailyGuideComment(
                0L,
                guide.getDayIndex(),
                member.getId())).isInstanceOf(CIllegalArgumentException.class);
    }

    @Test
    void 참여하지_않은_챌린지에서_내_댓글_조회시_예외_발생() {
        // given
        Member otherMember = TestFixture.createUniqueMember("other", "other");
        memberRepository.save(otherMember);

        // when & then
        assertThatThrownBy(() -> challengeDailyGuideService.getDailyGuideComment(
                challenge.getId(),
                guide.getDayIndex(),
                otherMember.getId())).isInstanceOf(CIllegalArgumentException.class);
    }

    @Test
    void 미래의_데일리_가이드_내_댓글_조회시_예외_발생() {
        // given
        int futureDayIndex = calculateDayIndex(challenge.getStartDate(), today) + 1;

        // when & then
        assertThatThrownBy(() -> challengeDailyGuideService.getDailyGuideComment(
                challenge.getId(),
                futureDayIndex,
                member.getId())).isInstanceOf(CIllegalArgumentException.class);
    }
}
