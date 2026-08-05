package me.bombom.api.v1.article.event;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDateTime;
import java.util.concurrent.Executor;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.article.repository.ArticleRepository;
import me.bombom.api.v1.article.repository.MarkAsReadEventLogRepository;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.newsletter.domain.Category;
import me.bombom.api.v1.newsletter.domain.Newsletter;
import me.bombom.api.v1.newsletter.domain.NewsletterDetail;
import me.bombom.api.v1.newsletter.repository.CategoryRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterDetailRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterRepository;
import me.bombom.api.v1.pet.ScorePolicyConstants;
import me.bombom.api.v1.pet.domain.Pet;
import me.bombom.api.v1.pet.repository.PetRepository;
import me.bombom.api.v1.pet.repository.StageRepository;
import me.bombom.api.v1.reading.domain.MonthlyReadingRealtime;
import me.bombom.api.v1.reading.repository.ContinueReadingRealtimeRepository;
import me.bombom.api.v1.reading.repository.MonthlyReadingRealtimeRepository;
import me.bombom.api.v1.reading.repository.TodayReadingRepository;
import me.bombom.api.v1.reading.repository.WeeklyReadingRepository;
import me.bombom.support.integration.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@IntegrationTest
class MarkAsReadListenerIntegrationTest {

    private static final LocalDateTime READ_AT = LocalDateTime.of(2026, 1, 1, 10, 0);
    private static final LocalDateTime YESTERDAY_ARTICLE_ARRIVED_AT = READ_AT.minusDays(1);
    private static final LocalDateTime TODAY_ARTICLE_ARRIVED_AT = READ_AT;

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

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private StageRepository stageRepository;

    @Autowired
    @Qualifier("markAsReadExecutor")
    private Executor markAsReadExecutor;

    private Member member;
    private Long articleId;
    private Newsletter newsletter;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(TestFixture.createMemberFixture("test@test.com", "testUser"));
        monthlyReadingRealtimeRepository.save(TestFixture.monthlyReadingRealtimeFixture(member, 0));
        todayReadingRepository.save(TestFixture.todayReadingFixtureZeroCurrentCount(member));
        continueReadingRealtimeRepository.save(TestFixture.continueReadingFixture(member));
        weeklyReadingRepository.save(TestFixture.weeklyReadingFixture(member));

        NewsletterDetail detail = newsletterDetailRepository.saveAll(TestFixture.createNewsletterDetails()).getFirst();
        Category category = categoryRepository.saveAll(TestFixture.createCategories()).getFirst();
        newsletter = newsletterRepository.save(
                TestFixture.createNewsletter("테스트레터", "test@letter.com", category.getId(), detail.getId())
        );

        articleId = saveArticleArrivedAt(member, YESTERDAY_ARTICLE_ARRIVED_AT);
    }

    @Test
    void 이벤트_정상_처리_시_읽기_카운트_증가() {
        // when
        markAsReadListener.on(MarkAsReadEvent.of(member.getId(), articleId, READ_AT, true));

        // then
        awaitUntilAsserted(() -> {
            MonthlyReadingRealtime realtime = monthlyReadingRealtimeRepository.findByMemberId(member.getId()).orElseThrow();
            assertSoftly(softly -> {
                softly.assertThat(realtime.getCurrentCount()).isEqualTo(1);
                softly.assertThat(markAsReadEventLogRepository.count()).isEqualTo(1);
            });
        });
    }

    @Test
    void 펫_정보가_없어_경험치_갱신에_실패해도_읽기_카운트는_유지된다() {
        // given
        Long todayArticleId = saveArticleArrivedAt(member, TODAY_ARTICLE_ARRIVED_AT);

        // when
        markAsReadListener.on(MarkAsReadEvent.of(member.getId(), todayArticleId, READ_AT, true));

        // then
        awaitUntilAsserted(() -> {
            MonthlyReadingRealtime realtime = monthlyReadingRealtimeRepository.findByMemberId(member.getId()).orElseThrow();
            assertSoftly(softly -> {
                softly.assertThat(realtime.getCurrentCount()).isEqualTo(1);
                softly.assertThat(markAsReadEventLogRepository.count()).isEqualTo(1);
                softly.assertThat(petRepository.findByMemberId(member.getId())).isEmpty();
            });
        });
    }

    @Test
    void 읽기_카운트_갱신에_실패하면_이벤트_로그도_롤백된다() {
        // given
        Member memberWithoutReading = memberRepository.save(TestFixture.uniqueMemberFixture());
        Long articleWithoutReadingId = saveArticleArrivedAt(memberWithoutReading, YESTERDAY_ARTICLE_ARRIVED_AT);

        // when
        markAsReadListener.on(MarkAsReadEvent.of(memberWithoutReading.getId(), articleWithoutReadingId, READ_AT, true));

        // then
        waitForMarkAsReadExecutorIdle();
        assertSoftly(softly -> {
            softly.assertThat(markAsReadEventLogRepository.count()).isZero();
            softly.assertThat(monthlyReadingRealtimeRepository.findByMemberId(memberWithoutReading.getId())).isEmpty();
        });
    }

    @Test
    void 오늘_도착한_아티클은_오늘과_주간_읽기_카운트도_증가한다() {
        // given
        Long todayArticleId = saveArticleArrivedAt(member, TODAY_ARTICLE_ARRIVED_AT);
        savePet(member);
        int weeklyReadingCountBefore = weeklyReadingRepository.findByMemberId(member.getId())
                .orElseThrow()
                .getCurrentCount();

        // when
        markAsReadListener.on(MarkAsReadEvent.of(member.getId(), todayArticleId, READ_AT, true));

        // then
        awaitUntilAsserted(() -> {
            MonthlyReadingRealtime realtime = monthlyReadingRealtimeRepository.findByMemberId(member.getId()).orElseThrow();
            assertSoftly(softly -> {
                softly.assertThat(realtime.getCurrentCount()).isEqualTo(1);
                softly.assertThat(todayReadingRepository.findByMemberId(member.getId()).orElseThrow().getCurrentCount())
                        .isEqualTo(1);
                softly.assertThat(weeklyReadingRepository.findByMemberId(member.getId()).orElseThrow().getCurrentCount())
                        .isEqualTo(weeklyReadingCountBefore + 1);
                softly.assertThat(petRepository.findByMemberId(member.getId()).orElseThrow().getCurrentScore())
                        .isEqualTo(ScorePolicyConstants.ARTICLE_READING_SCORE
                                + ScorePolicyConstants.CONTINUE_READING_BONUS_SCORE);
            });
        });
    }

    @Test
    void 카운트_대상이_아닌_이벤트는_읽기_카운트_증가_안함() {
        // when
        markAsReadListener.on(MarkAsReadEvent.of(member.getId(), articleId, READ_AT, false));

        // then
        waitForMarkAsReadExecutorIdle();
        MonthlyReadingRealtime realtime = monthlyReadingRealtimeRepository.findByMemberId(member.getId()).orElseThrow();
        assertSoftly(softly -> {
            softly.assertThat(realtime.getCurrentCount()).isEqualTo(0);
            softly.assertThat(markAsReadEventLogRepository.count()).isZero();
        });
    }

    private Long saveArticleArrivedAt(Member targetMember, LocalDateTime arrivedAt) {
        return articleRepository.save(
                TestFixture.createArticle("테스트 아티클", targetMember.getId(), newsletter.getId(), arrivedAt)
        ).getId();
    }

    private Pet savePet(Member targetMember) {
        Long firstStageId = stageRepository.save(TestFixture.createStage(1, 0)).getId();
        stageRepository.save(TestFixture.createStage(2, 50));
        return petRepository.save(TestFixture.createPet(targetMember, firstStageId));
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

    private void waitForMarkAsReadExecutorIdle() {
        ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) markAsReadExecutor;
        for (int i = 0; i < 20; i++) {
            if (executor.getActiveCount() == 0 && executor.getThreadPoolExecutor().getQueue().isEmpty()) {
                return;
            }
            sleepBriefly();
        }
        throw new AssertionError("MarkAsRead 비동기 작업이 제한 시간 안에 완료되지 않음");
    }

    @FunctionalInterface
    private interface CheckedAssertion {

        void run();
    }
}
