package me.bombom.api.v1.subscribe.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.BDDMockito.given;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import me.bombom.api.v1.subscribe.domain.NewsletterSubscriptionCount;
import me.bombom.api.v1.subscribe.repository.NewsletterSubscriptionCountRepository;
import me.bombom.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@IntegrationTest
class NewsletterSubscriptionCountServiceTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2026-01-01T00:00:00Z");
    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

    // 기준 연도 2026 기준 고정 birth dates
    private static final LocalDate AGE_20S_BIRTH_DATE = LocalDate.of(2001, 1, 1); // age 25
    private static final LocalDate AGE_30S_BIRTH_DATE = LocalDate.of(1991, 1, 1); // age 35

    @Autowired
    private NewsletterSubscriptionCountService newsletterSubscriptionCountService;

    @Autowired
    private NewsletterSubscriptionCountRepository newsletterSubscriptionCountRepository;

    @MockitoBean
    private Clock clock;

    @BeforeEach
    void setup() {
        newsletterSubscriptionCountRepository.deleteAllInBatch();
        given(clock.instant()).willReturn(FIXED_INSTANT);
        given(clock.getZone()).willReturn(SEOUL_ZONE);
    }

    @Test
    void 생년월일이_없으면_구독자_수를_업데이트하지_않는다() {
        newsletterSubscriptionCountService.updateNewsletterSubscriptionCount(1L, null);

        assertThat(newsletterSubscriptionCountRepository.findAll()).isEmpty();
    }

    @Test
    void 최초_구독시_새_행을_생성하고_해당_연령대와_total을_1로_설정한다() {
        newsletterSubscriptionCountService.updateNewsletterSubscriptionCount(1L, AGE_20S_BIRTH_DATE);

        List<NewsletterSubscriptionCount> result = newsletterSubscriptionCountRepository.findAll();
        assertSoftly(softly -> {
            softly.assertThat(result).hasSize(1);
            softly.assertThat(result.getFirst().getTotal()).isEqualTo(1);
            softly.assertThat(result.getFirst().getAge20s()).isEqualTo(1);
        });
    }

    @Test
    void 동일_뉴스레터에_재호출_시_total과_연령대_수가_누적된다() {
        newsletterSubscriptionCountService.updateNewsletterSubscriptionCount(1L, AGE_20S_BIRTH_DATE);
        newsletterSubscriptionCountService.updateNewsletterSubscriptionCount(1L, AGE_20S_BIRTH_DATE);

        NewsletterSubscriptionCount result = newsletterSubscriptionCountRepository.findAll().getFirst();
        assertSoftly(softly -> {
            softly.assertThat(result.getTotal()).isEqualTo(2);
            softly.assertThat(result.getAge20s()).isEqualTo(2);
        });
    }

    @Test
    void 연령대가_다른_두_구독자가_구독하면_각_연령대_컬럼과_total이_증가한다() {
        newsletterSubscriptionCountService.updateNewsletterSubscriptionCount(1L, AGE_20S_BIRTH_DATE);
        newsletterSubscriptionCountService.updateNewsletterSubscriptionCount(1L, AGE_30S_BIRTH_DATE);

        NewsletterSubscriptionCount result = newsletterSubscriptionCountRepository.findAll().getFirst();
        assertSoftly(softly -> {
            softly.assertThat(result.getTotal()).isEqualTo(2);
            softly.assertThat(result.getAge20s()).isEqualTo(1);
            softly.assertThat(result.getAge30s()).isEqualTo(1);
        });
    }

    @Test
    void 구독_해지시_해당_연령대와_total이_감소한다() {
        newsletterSubscriptionCountService.updateNewsletterSubscriptionCount(1L, AGE_20S_BIRTH_DATE);
        newsletterSubscriptionCountService.updateNewsletterSubscriptionCount(1L, AGE_30S_BIRTH_DATE);

        newsletterSubscriptionCountService.decreaseNewsletterSubscriptionCount(1L, AGE_20S_BIRTH_DATE);

        NewsletterSubscriptionCount result = newsletterSubscriptionCountRepository.findAll().getFirst();
        assertSoftly(softly -> {
            softly.assertThat(result.getTotal()).isEqualTo(1);
            softly.assertThat(result.getAge20s()).isZero();
            softly.assertThat(result.getAge30s()).isEqualTo(1);
        });
    }

    @Test
    void 생년월일이_없으면_구독자_수를_감소하지_않는다() {
        newsletterSubscriptionCountService.updateNewsletterSubscriptionCount(1L, AGE_20S_BIRTH_DATE);

        newsletterSubscriptionCountService.decreaseNewsletterSubscriptionCount(1L, null);

        NewsletterSubscriptionCount result = newsletterSubscriptionCountRepository.findAll().getFirst();
        assertSoftly(softly -> {
            softly.assertThat(result.getTotal()).isEqualTo(1);
            softly.assertThat(result.getAge20s()).isEqualTo(1);
        });
    }
}
