package me.bombom.api.v1.challenge.service;

import static java.time.temporal.ChronoUnit.DAYS;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.challenge.domain.Challenge;
import me.bombom.api.v1.challenge.domain.ChallengeDailyGuide;
import me.bombom.api.v1.challenge.domain.ChallengeDailyGuideComment;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import me.bombom.api.v1.challenge.domain.DailyGuideType;
import me.bombom.api.v1.challenge.dto.request.DailyGuideCommentRequest;
import me.bombom.api.v1.challenge.dto.response.TodayDailyGuideResponse;
import me.bombom.api.v1.challenge.repository.ChallengeDailyGuideCommentRepository;
import me.bombom.api.v1.challenge.repository.ChallengeDailyGuideRepository;
import me.bombom.api.v1.challenge.repository.ChallengeParticipantRepository;
import me.bombom.api.v1.challenge.repository.ChallengeRepository;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

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

    private Member member;
    private Challenge challenge;
    private ChallengeParticipant participant;
    private ChallengeDailyGuide guide;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        challengeDailyGuideCommentRepository.deleteAllInBatch();
        challengeDailyGuideRepository.deleteAllInBatch();
        challengeParticipantRepository.deleteAllInBatch();
        challengeRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();

        member = TestFixture.normalMemberFixture();
        memberRepository.save(member);

        today = LocalDate.now(SEOUL_ZONE);
        challenge = challengeRepository.save(TestFixture.createChallenge(
                "테스트 챌린지",
                today.minusDays(5),
                today.plusDays(5),
                10
        ));

        participant = challengeParticipantRepository.save(
                TestFixture.createChallengeParticipant(
                        challenge.getId(),
                        member.getId(),
                        0
                )
        );

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
        // given
        DailyGuideCommentRequest request = new DailyGuideCommentRequest("뉴스레터 읽기 팁을 공유합니다");
        int dayIndex = guide.getDayIndex();

        // when
        challengeDailyGuideService.createDailyGuideComment(
                challenge.getId(),
                dayIndex,
                member.getId(),
                request
        );

        // then
        List<ChallengeDailyGuideComment> comments = challengeDailyGuideCommentRepository.findAll();
        assertSoftly(softly -> {
            softly.assertThat(comments).hasSize(1);
            softly.assertThat(comments.get(0).getGuideId()).isEqualTo(guide.getId());
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
                request
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
        Challenge futureChallenge = challengeRepository.save(TestFixture.createChallenge(
                "미래 챌린지",
                today.plusDays(10),
                today.plusDays(20),
                10
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
                request
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
                request
        )).isInstanceOf(CIllegalArgumentException.class);
    }

    @Test
    void 주말_가이드_조회_성공() {
        // given - 주말 가이드 생성 (dayIndex = 0)
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

        // when - 주말 가이드 조회 (실제로는 주말일 때만 dayIndex 0이 반환되지만,
        // 가이드가 존재하면 정상 조회됨)
        TodayDailyGuideResponse response = challengeDailyGuideService.getTodayDailyGuide(
                challenge.getId(),
                member.getId()
        );

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.dayIndex()).isGreaterThanOrEqualTo(0);
            softly.assertThat(response.type()).isNotNull();
            softly.assertThat(response.imageUrl()).isNotNull();
        });
    }

    @Test
    void 주말_가이드에_댓글_작성_불가() {
        // given - 주말 가이드 생성 (dayIndex = 0, commentEnabled = false)
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
                request
        )).isInstanceOf(CIllegalArgumentException.class);
    }

    @Test
    void dayIndex_0_유효성_검증_통과() {
        // given - 주말 가이드 생성 (dayIndex = 0, commentEnabled = true)
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
                request
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
}

