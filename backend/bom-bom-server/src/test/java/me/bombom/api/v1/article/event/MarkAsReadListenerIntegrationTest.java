package me.bombom.api.v1.article.event;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.time.LocalDateTime;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.article.repository.ArticleRepository;
import me.bombom.api.v1.article.repository.MarkAsReadEventLogRepository;
import me.bombom.api.v1.article.service.ArticleService;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.newsletter.domain.Category;
import me.bombom.api.v1.newsletter.domain.Newsletter;
import me.bombom.api.v1.newsletter.domain.NewsletterDetail;
import me.bombom.api.v1.newsletter.repository.CategoryRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterDetailRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterRepository;
import me.bombom.api.v1.pet.service.PetService;
import me.bombom.api.v1.reading.domain.MonthlyReadingRealtime;
import me.bombom.api.v1.reading.repository.ContinueReadingRealtimeRepository;
import me.bombom.api.v1.reading.repository.MonthlyReadingRealtimeRepository;
import me.bombom.api.v1.reading.repository.TodayReadingRepository;
import me.bombom.api.v1.reading.repository.WeeklyReadingRepository;
import me.bombom.api.v1.reading.service.ReadingService;
import me.bombom.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@IntegrationTest
class MarkAsReadListenerIntegrationTest {

    @Autowired
    private MarkAsReadListener markAsReadListener;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private MarkAsReadEventLogRepository markAsReadEventLogRepository;

    @Autowired
    private NewsletterRepository newsletterRepository;

    @Autowired
    private NewsletterDetailRepository newsletterDetailRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private MonthlyReadingRealtimeRepository monthlyReadingRealtimeRepository;

    @Autowired
    private TodayReadingRepository todayReadingRepository;

    @Autowired
    private ContinueReadingRealtimeRepository continueReadingRealtimeRepository;

    @Autowired
    private WeeklyReadingRepository weeklyReadingRepository;

    @MockitoSpyBean
    private ReadingService readingService;

    @MockitoSpyBean
    private ArticleService articleService;

    @MockitoSpyBean
    private PetService petService;

    private Member member;
    private Long articleId;

    @BeforeEach
    void setUp() {
        markAsReadEventLogRepository.deleteAllInBatch();
        articleRepository.deleteAllInBatch();
        newsletterRepository.deleteAllInBatch();
        newsletterDetailRepository.deleteAllInBatch();
        categoryRepository.deleteAllInBatch();
        monthlyReadingRealtimeRepository.deleteAllInBatch();
        todayReadingRepository.deleteAllInBatch();
        continueReadingRealtimeRepository.deleteAllInBatch();
        weeklyReadingRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();

        member = memberRepository.save(TestFixture.createMemberFixture("test@test.com", "testUser"));
        monthlyReadingRealtimeRepository.save(TestFixture.monthlyReadingRealtimeFixture(member, 0));
        todayReadingRepository.save(TestFixture.todayReadingFixtureZeroCurrentCount(member));
        continueReadingRealtimeRepository.save(TestFixture.continueReadingFixture(member));
        weeklyReadingRepository.save(TestFixture.weeklyReadingFixture(member));

        NewsletterDetail detail = newsletterDetailRepository.saveAll(TestFixture.createNewsletterDetails()).getFirst();
        Category category = categoryRepository.saveAll(TestFixture.createCategories()).getFirst();
        Newsletter newsletter = newsletterRepository.save(
                TestFixture.createNewsletter("테스트레터", "test@letter.com", category.getId(), detail.getId())
        );

        articleId = articleRepository.save(
                TestFixture.createArticle("테스트 아티클", member.getId(), newsletter.getId(), LocalDateTime.now().minusDays(1))
        ).getId();
    }

    @Test
    void 이벤트_정상_처리_시_읽기_카운트_증가() {
        // when
        markAsReadListener.on(MarkAsReadEvent.of(member.getId(), articleId, LocalDateTime.now(), true));

        // then
        verify(readingService, timeout(1_000)).updateReadingCount(member.getId(), false);
        awaitUntilAsserted(() -> {
            MonthlyReadingRealtime realtime = monthlyReadingRealtimeRepository.findByMemberId(member.getId()).orElseThrow();
            assertSoftly(softly -> softly.assertThat(realtime.getCurrentCount()).isEqualTo(1));
        });
    }

    @Test
    void 읽기_카운트_갱신_실패_시_펫_경험치도_갱신_안됨() {
        // given
        doThrow(new RuntimeException("읽기 카운트 갱신 실패"))
                .when(readingService).updateReadingCount(anyLong(), any(Boolean.class));

        // when
        markAsReadListener.on(MarkAsReadEvent.of(member.getId(), articleId, LocalDateTime.now(), true));

        // then
        verify(readingService, timeout(1_000)).updateReadingCount(member.getId(), false);
        verify(petService, after(300).never()).increaseCurrentScore(anyLong(), anyInt());
        awaitUntilAsserted(() -> {
            MonthlyReadingRealtime realtime = monthlyReadingRealtimeRepository.findByMemberId(member.getId()).orElseThrow();
            assertSoftly(softly -> {
                softly.assertThat(realtime.getCurrentCount()).isEqualTo(0);
                softly.assertThat(markAsReadEventLogRepository.count()).isZero();
            });
        });
    }

    @Test
    void 펫_경험치_갱신_실패해도_읽기_카운트는_유지된다() {
        // given
        doReturn(true).when(articleService).isArrivedToday(anyLong(), anyLong(), any(LocalDate.class));
        doReturn(true).when(articleService).canAddArticleScore(anyLong());
        doReturn(10).when(readingService).calculateArticleScore(anyLong());
        doThrow(new RuntimeException("펫 경험치 갱신 실패"))
                .when(petService).increaseCurrentScore(anyLong(), anyInt());

        // when
        markAsReadListener.on(MarkAsReadEvent.of(member.getId(), articleId, LocalDateTime.now(), true));

        // then
        verify(readingService, timeout(1_000)).updateReadingCount(member.getId(), true);
        verify(petService, timeout(1_000)).increaseCurrentScore(member.getId(), 10);
        awaitUntilAsserted(() -> {
            MonthlyReadingRealtime realtime = monthlyReadingRealtimeRepository.findByMemberId(member.getId()).orElseThrow();
            assertSoftly(softly -> softly.assertThat(realtime.getCurrentCount()).isEqualTo(1));
        });
    }

    @Test
    void 카운트_대상이_아닌_이벤트는_읽기_카운트_증가_안함() {
        // when
        markAsReadListener.on(MarkAsReadEvent.of(member.getId(), articleId, LocalDateTime.now(), false));

        // then
        verify(readingService, after(300).never()).updateReadingCount(anyLong(), any(Boolean.class));
        verify(petService, after(300).never()).increaseCurrentScore(anyLong(), anyInt());
        awaitUntilAsserted(() -> {
            MonthlyReadingRealtime realtime = monthlyReadingRealtimeRepository.findByMemberId(member.getId()).orElseThrow();
            assertSoftly(softly -> {
                softly.assertThat(realtime.getCurrentCount()).isEqualTo(0);
                softly.assertThat(markAsReadEventLogRepository.count()).isZero();
            });
        });
    }

    private void awaitUntilAsserted(CheckedAssertion assertion) {
        AssertionError lastAssertionError = null;
        for (int i = 0; i < 20; i++) {
            try {
                assertion.run();
                return;
            } catch (AssertionError e) {
                lastAssertionError = e;
                sleepBriefly();
            }
        }

        if (lastAssertionError != null) {
            throw lastAssertionError;
        }
    }

    private void sleepBriefly() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError("비동기 테스트 대기 중 인터럽트 발생", e);
        }
    }

    @FunctionalInterface
    private interface CheckedAssertion {

        void run();
    }
}
