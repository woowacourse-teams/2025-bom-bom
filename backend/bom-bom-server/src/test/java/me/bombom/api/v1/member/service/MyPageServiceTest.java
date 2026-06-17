package me.bombom.api.v1.member.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import me.bombom.api.v1.article.repository.ArticleReadHistoryRepository;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.dto.response.RankSummaryResponse;
import me.bombom.api.v1.reading.domain.ContinueReadingRankHistory;
import me.bombom.api.v1.reading.domain.ContinueReadingRealtime;
import me.bombom.api.v1.reading.domain.MonthlyReadingRankHistory;
import me.bombom.api.v1.reading.repository.ContinueReadingRankHistoryRepository;
import me.bombom.api.v1.reading.repository.ContinueReadingRealtimeRepository;
import me.bombom.api.v1.reading.repository.MonthlyReadingRankHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class MyPageServiceTest {

    private static final Long MEMBER_ID = 1L;

    @Mock
    private ContinueReadingRealtimeRepository continueReadingRealtimeRepository;

    @Mock
    private MonthlyReadingRankHistoryRepository monthlyReadingRankHistoryRepository;

    @Mock
    private ContinueReadingRankHistoryRepository continueReadingRankHistoryRepository;

    @Mock
    private ArticleReadHistoryRepository articleReadHistoryRepository;

    private final Clock clock = Clock.fixed(
            Instant.parse("2026-06-17T00:00:00Z"),
            ZoneId.of("Asia/Seoul")
    );

    private MyPageService myPageService;

    @BeforeEach
    void setUp() {
        myPageService = new MyPageService(
                continueReadingRealtimeRepository,
                monthlyReadingRankHistoryRepository,
                continueReadingRankHistoryRepository,
                articleReadHistoryRepository,
                clock
        );
    }

    @Test
    void 읽기_순위_카드의_value는_누적_읽은_아티클_수이다() {
        // given
        Member member = mock(Member.class);
        given(member.getId()).willReturn(MEMBER_ID);
        given(monthlyReadingRankHistoryRepository.findRecentBeforePeriodByMemberId(
                MEMBER_ID,
                LocalDate.of(2026, 6, 1),
                PageRequest.of(0, 6)
        ))
                .willReturn(List.of(
                        MonthlyReadingRankHistory.builder()
                                .memberId(MEMBER_ID)
                                .period(LocalDate.of(2026, 5, 1))
                                .readCount(8)
                                .rankOrder(4)
                                .build()
                ));
        given(articleReadHistoryRepository.countByMemberId(MEMBER_ID)).willReturn(77);

        // when
        RankSummaryResponse response = myPageService.getRankSummary(member, "reading");

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.cards()).hasSize(1);
            softly.assertThat(response.cards().getFirst().type()).isEqualTo("reading");
            softly.assertThat(response.cards().getFirst().currentRank()).isEqualTo(4);
            softly.assertThat(response.cards().getFirst().value()).isEqualTo(77);
            softly.assertThat(response.cards().getFirst().rankHistory())
                    .extracting("month", "label", "rank")
                    .containsExactly(
                            org.assertj.core.groups.Tuple.tuple("2026-05", "5월", 4L)
                    );
        });
    }

    @Test
    void 스트릭_순위_카드를_조회한다() {
        // given
        Member member = mock(Member.class);
        given(member.getId()).willReturn(MEMBER_ID);
        given(continueReadingRealtimeRepository.findByMemberId(MEMBER_ID))
                .willReturn(Optional.of(ContinueReadingRealtime.builder()
                        .memberId(MEMBER_ID)
                        .dayCount(6)
                        .build()));
        given(continueReadingRankHistoryRepository.findRecentBeforePeriodByMemberId(
                MEMBER_ID,
                LocalDate.of(2026, 6, 1),
                PageRequest.of(0, 6)
        ))
                .willReturn(List.of(
                        ContinueReadingRankHistory.builder()
                                .memberId(MEMBER_ID)
                                .period(LocalDate.of(2026, 5, 1))
                                .dayCount(6)
                                .rankOrder(3)
                                .build(),
                        ContinueReadingRankHistory.builder()
                                .memberId(MEMBER_ID)
                                .period(LocalDate.of(2025, 12, 1))
                                .dayCount(2)
                                .rankOrder(20)
                                .build()
                ));

        // when
        RankSummaryResponse response = myPageService.getRankSummary(member, "streak");

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.cards()).hasSize(1);
            softly.assertThat(response.cards().getFirst().type()).isEqualTo("streak");
            softly.assertThat(response.cards().getFirst().currentRank()).isEqualTo(3);
            softly.assertThat(response.cards().getFirst().value()).isEqualTo(6);
            softly.assertThat(response.cards().getFirst().rankHistory())
                    .extracting("month", "label", "rank")
                    .containsExactly(
                            org.assertj.core.groups.Tuple.tuple("2025-12", "25.12", 20L),
                            org.assertj.core.groups.Tuple.tuple("2026-05", "5월", 3L)
                    );
        });
    }

    @Test
    void 이전달_랭킹_이력이_없으면_currentRank는_null이다() {
        // given
        Member member = mock(Member.class);
        given(member.getId()).willReturn(MEMBER_ID);
        given(monthlyReadingRankHistoryRepository.findRecentBeforePeriodByMemberId(
                MEMBER_ID,
                LocalDate.of(2026, 6, 1),
                PageRequest.of(0, 6)
        )).willReturn(List.of());
        given(articleReadHistoryRepository.countByMemberId(MEMBER_ID)).willReturn(0);

        // when
        RankSummaryResponse response = myPageService.getRankSummary(member, "reading");

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.cards()).hasSize(1);
            softly.assertThat(response.cards().getFirst().currentRank()).isNull();
            softly.assertThat(response.cards().getFirst().rankHistory()).isEmpty();
            softly.assertThat(response.cards().getFirst().value()).isZero();
        });
    }

    @Test
    void 지원하지_않는_순위_type이면_예외가_발생한다() {
        // given
        Member member = mock(Member.class);

        // when & then
        assertThatThrownBy(() -> myPageService.getRankSummary(member, "article"))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.INVALID_REQUEST_PARAMETER_VALIDATION);
    }
}
