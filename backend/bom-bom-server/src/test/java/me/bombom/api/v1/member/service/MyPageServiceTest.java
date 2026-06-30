package me.bombom.api.v1.member.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.LongStream;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.article.domain.ArticleReadHistory;
import me.bombom.api.v1.article.repository.ArticleReadHistoryRepository;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.dto.response.RankSummaryResponse;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.reading.domain.ContinueReadingRankHistory;
import me.bombom.api.v1.reading.domain.ContinueReadingRealtime;
import me.bombom.api.v1.reading.domain.MonthlyReadingRankHistory;
import me.bombom.api.v1.reading.repository.ContinueReadingRankHistoryRepository;
import me.bombom.api.v1.reading.repository.ContinueReadingRealtimeRepository;
import me.bombom.api.v1.reading.repository.MonthlyReadingRankHistoryRepository;
import me.bombom.support.integration.IntegrationTest;
import me.bombom.support.time.MutableClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
class MyPageServiceTest {

    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

    @Autowired
    private MyPageService myPageService;

    @Autowired
    private ContinueReadingRealtimeRepository continueReadingRealtimeRepository;

    @Autowired
    private MonthlyReadingRankHistoryRepository monthlyReadingRankHistoryRepository;

    @Autowired
    private ContinueReadingRankHistoryRepository continueReadingRankHistoryRepository;

    @Autowired
    private ArticleReadHistoryRepository articleReadHistoryRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MutableClock clock;

    private Member member;

    @BeforeEach
    void setUp() {
        clock.setInstant(Instant.parse("2026-06-17T00:00:00Z"), SEOUL_ZONE);

        member = memberRepository.save(TestFixture.createUniqueMember("member", "memberProvider"));
    }

    @Test
    void 읽기_순위_카드의_value는_누적_읽은_아티클_수이다() {
        // given
        monthlyReadingRankHistoryRepository.save(MonthlyReadingRankHistory.builder()
                .memberId(member.getId())
                .period(LocalDate.of(2026, 5, 1))
                .readCount(8)
                .rankOrder(4)
                .build());
        articleReadHistoryRepository.saveAll(createArticleReadHistories(77));

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
        continueReadingRealtimeRepository.save(ContinueReadingRealtime.builder()
                .memberId(member.getId())
                .dayCount(6)
                .build());
        continueReadingRankHistoryRepository.saveAll(List.of(
                ContinueReadingRankHistory.builder()
                        .memberId(member.getId())
                        .period(LocalDate.of(2026, 5, 1))
                        .dayCount(6)
                        .rankOrder(3)
                        .build(),
                ContinueReadingRankHistory.builder()
                        .memberId(member.getId())
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
        // when & then
        assertThatThrownBy(() -> myPageService.getRankSummary(member, "article"))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.INVALID_REQUEST_PARAMETER_VALIDATION);
    }

    private List<ArticleReadHistory> createArticleReadHistories(int count) {
        return LongStream.rangeClosed(1, count)
                .mapToObj(articleId -> ArticleReadHistory.builder()
                        .memberId(member.getId())
                        .articleId(articleId)
                        .newsletterId(1L)
                        .categoryId(1L)
                        .readAt(LocalDateTime.of(2026, 5, 1, 0, 0))
                        .build())
                .toList();
    }
}
