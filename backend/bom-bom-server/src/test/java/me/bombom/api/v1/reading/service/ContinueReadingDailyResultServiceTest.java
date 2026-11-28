package me.bombom.api.v1.reading.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.time.LocalDate;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.reading.domain.ContinueReadingDailyResult;
import me.bombom.api.v1.reading.domain.ContinueReadingDailyResultType;
import me.bombom.api.v1.reading.domain.TodayReading;
import me.bombom.api.v1.reading.repository.ContinueReadingDailyResultRepository;
import me.bombom.api.v1.reading.repository.TodayReadingRepository;
import me.bombom.support.integration.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
class ContinueReadingDailyResultServiceTest {

    // 2026-11-26(목)
    private static final LocalDate WEEKDAY = LocalDate.of(2026, 11, 26);

    @Autowired
    private ContinueReadingDailyResultService continueReadingDailyResultService;

    @Autowired
    private ContinueReadingDailyResultRepository continueReadingDailyResultRepository;

    @Autowired
    private TodayReadingRepository todayReadingRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Member reader;
    private Member noArticle;
    private Member missed;

    @BeforeEach
    void setUp() {
        reader = saveMemberWithTodayReading("reader", 2, 1);     // 받은 글 2, 읽음 1
        noArticle = saveMemberWithTodayReading("no-article", 0, 0); // 받은 글 없음
        missed = saveMemberWithTodayReading("missed", 3, 0);     // 받았지만 안 읽음
    }

    @Test
    void 평일이면_읽은_회원은_READ_받은_글이_없는_회원은_NO_ARTICLE로_기록하고_안_읽은_회원은_기록하지_않는다() {
        // when
        continueReadingDailyResultService.recordDailyResults(WEEKDAY, false);

        // then
        assertThat(continueReadingDailyResultRepository.findAll())
                .extracting(ContinueReadingDailyResult::getMemberId, ContinueReadingDailyResult::getResultDate,
                        ContinueReadingDailyResult::getResult)
                .containsExactlyInAnyOrder(
                        tuple(reader.getId(), WEEKDAY, ContinueReadingDailyResultType.READ),
                        tuple(noArticle.getId(), WEEKDAY, ContinueReadingDailyResultType.NO_ARTICLE)
                );
    }

    @Test
    void 주말이나_공휴일이면_READ만_기록한다() {
        // when
        continueReadingDailyResultService.recordDailyResults(LocalDate.of(2026, 11, 28), true);

        // then
        assertThat(continueReadingDailyResultRepository.findAll())
                .extracting(ContinueReadingDailyResult::getMemberId, ContinueReadingDailyResult::getResult)
                .containsExactly(tuple(reader.getId(), ContinueReadingDailyResultType.READ));
    }

    @Test
    void 같은_날짜로_다시_실행해도_중복_기록하지_않는다() {
        // when
        continueReadingDailyResultService.recordDailyResults(WEEKDAY, false);
        continueReadingDailyResultService.recordDailyResults(WEEKDAY, false);

        // then
        assertThat(continueReadingDailyResultRepository.findAll()).hasSize(2);
    }

    @Test
    void 보관_기간이_지난_기록은_지운다() {
        // given
        LocalDate expired = WEEKDAY.minusDays(ContinueReadingDailyResultService.RETENTION_DAYS + 1L);
        LocalDate kept = WEEKDAY.minusDays(ContinueReadingDailyResultService.RETENTION_DAYS);
        saveDailyResult(missed, expired);
        saveDailyResult(missed, kept);

        // when
        continueReadingDailyResultService.recordDailyResults(WEEKDAY, false);

        // then
        assertThat(continueReadingDailyResultRepository.findAll())
                .extracting(ContinueReadingDailyResult::getResultDate)
                .contains(kept)
                .doesNotContain(expired);
    }

    private Member saveMemberWithTodayReading(String name, int totalCount, int readCount) {
        Member member = memberRepository.save(TestFixture.createUniqueMember(name, name));
        todayReadingRepository.save(TodayReading.builder()
                .memberId(member.getId())
                .totalCount(totalCount)
                .currentCount(readCount)
                .readCount(readCount)
                .build());
        return member;
    }

    private void saveDailyResult(Member member, LocalDate date) {
        continueReadingDailyResultRepository.save(ContinueReadingDailyResult.builder()
                .memberId(member.getId())
                .resultDate(date)
                .result(ContinueReadingDailyResultType.READ)
                .build());
    }
}
