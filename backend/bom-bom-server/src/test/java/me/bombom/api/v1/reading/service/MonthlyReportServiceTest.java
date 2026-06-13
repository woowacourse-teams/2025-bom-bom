package me.bombom.api.v1.reading.service;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.YearMonth;
import java.util.List;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.article.repository.ArticleReadHistoryRepository;
import me.bombom.api.v1.bookmark.repository.BookmarkRepository;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.newsletter.domain.Category;
import me.bombom.api.v1.newsletter.domain.Newsletter;
import me.bombom.api.v1.newsletter.domain.NewsletterDetail;
import me.bombom.api.v1.newsletter.repository.CategoryRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterDetailRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterRepository;
import me.bombom.openapi.model.MonthlyReportDashboardRequest;
import me.bombom.openapi.model.MonthlyReportRequest;
import me.bombom.openapi.model.ReadingCalendarDayResponse;
import me.bombom.openapi.model.ReadingDashboardResponse;
import me.bombom.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
class MonthlyReportServiceTest {

    @Autowired
    private MonthlyReportService monthlyReportService;

    @Autowired
    private ArticleReadHistoryRepository articleReadHistoryRepository;

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private NewsletterRepository newsletterRepository;

    @Autowired
    private NewsletterDetailRepository newsletterDetailRepository;

    private Member member;
    private Member otherMember;
    private List<Newsletter> newsletters;

    @BeforeEach
    void setUp() {
        articleReadHistoryRepository.deleteAllInBatch();
        bookmarkRepository.deleteAllInBatch();
        newsletterRepository.deleteAllInBatch();
        newsletterDetailRepository.deleteAllInBatch();
        categoryRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();

        member = memberRepository.save(TestFixture.createUniqueMember("member", "memberProvider"));
        otherMember = memberRepository.save(TestFixture.createUniqueMember("other", "otherProvider"));

        List<Category> categories = TestFixture.createCategories();
        categoryRepository.saveAll(categories);
        List<NewsletterDetail> newsletterDetails = TestFixture.createNewsletterDetails();
        newsletterDetailRepository.saveAll(newsletterDetails);
        newsletters = newsletterRepository.saveAll(TestFixture.createNewslettersWithDetails(categories, newsletterDetails));
    }

    @Test
    void 월간_읽기_캘린더를_읽기_히스토리_기준으로_조회한다() {
        YearMonth targetMonth = YearMonth.now();

        articleReadHistoryRepository.saveAll(List.of(
                TestFixture.createArticleReadHistory(member, 1L, newsletters.get(0), targetMonth.atDay(1).atTime(10, 0)),
                TestFixture.createArticleReadHistory(member, 2L, newsletters.get(1), targetMonth.atDay(1).atTime(11, 0)),
                TestFixture.createArticleReadHistory(member, 3L, newsletters.get(2), targetMonth.atDay(2).atTime(10, 0)),
                TestFixture.createArticleReadHistory(member, 4L, newsletters.get(0), targetMonth.minusMonths(1).atEndOfMonth().atTime(10, 0)),
                TestFixture.createArticleReadHistory(otherMember, 5L, newsletters.get(0), targetMonth.atDay(1).atTime(10, 0))
        ));

        List<ReadingCalendarDayResponse> result = monthlyReportService.getReadingCalendar(
                member,
                new MonthlyReportRequest(targetMonth.getYear(), targetMonth.getMonthValue())
        );

        assertSoftly(softly -> {
            softly.assertThat(result).hasSize(targetMonth.lengthOfMonth());
            softly.assertThat(result.get(0).date()).isEqualTo(targetMonth.atDay(1));
            softly.assertThat(result.get(0).readCount()).isEqualTo(2);
            softly.assertThat(result.get(1).readCount()).isEqualTo(1);
            softly.assertThat(result.get(2).read()).isFalse();
        });
    }

    @Test
    void 월간_읽기_대시보드를_읽기_히스토리와_월간_북마크_기준으로_조회한다() {
        YearMonth targetMonth = YearMonth.now();

        articleReadHistoryRepository.saveAll(List.of(
                TestFixture.createArticleReadHistory(member, 1L, newsletters.get(2), targetMonth.atDay(1).atTime(10, 0)),
                TestFixture.createArticleReadHistory(member, 2L, newsletters.get(2), targetMonth.atDay(2).atTime(10, 0)),
                TestFixture.createArticleReadHistory(member, 3L, newsletters.get(0), targetMonth.atDay(3).atTime(10, 0)),
                TestFixture.createArticleReadHistory(member, 4L, newsletters.get(0), targetMonth.minusMonths(1).atDay(1).atTime(10, 0)),
                TestFixture.createArticleReadHistory(member, 5L, newsletters.get(1), targetMonth.minusMonths(1).atDay(2).atTime(10, 0)),
                TestFixture.createArticleReadHistory(otherMember, 6L, newsletters.get(2), targetMonth.atDay(1).atTime(10, 0))
        ));

        bookmarkRepository.save(TestFixture.createBookmark(member, 1L));
        bookmarkRepository.save(TestFixture.createBookmark(member, 2L));
        bookmarkRepository.save(TestFixture.createBookmark(otherMember, 4L));

        ReadingDashboardResponse result = monthlyReportService.getReadingDashboard(
                member,
                new MonthlyReportDashboardRequest(targetMonth.getYear(), targetMonth.getMonthValue(), 1)
        );

        assertSoftly(softly -> {
            softly.assertThat(result.readArticleCount()).isEqualTo(3);
            softly.assertThat(result.readArticleChangeRate()).isEqualTo(50.0);
            softly.assertThat(result.bookmarkCount()).isEqualTo(2);
            softly.assertThat(result.frequentReadNewsletters()).hasSize(1);
            softly.assertThat(result.frequentReadNewsletters().getFirst().newsletterId()).isEqualTo(newsletters.get(2).getId());
        });
    }
}
