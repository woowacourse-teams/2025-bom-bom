package me.bombom.api.v1.image.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDateTime;
import java.time.ZoneId;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.image.config.ImageUploadRateLimitProperties;
import me.bombom.api.v1.image.domain.ImageUploadBucketKey;
import me.bombom.api.v1.image.repository.ImageUploadTokenBucketRepository;
import me.bombom.support.integration.IntegrationTest;
import me.bombom.support.time.MutableClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
class ImageUploadRateLimitServiceTest {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 1, 1, 10, 0);
    private static final long MEMBER_ID = 1L;
    private static final String GUEST_ID = "e4560796-3db9-4f26-828a-c356c91d5076";

    @Autowired
    private ImageUploadRateLimitService imageUploadRateLimitService;

    @Autowired
    private ImageUploadTokenBucketRepository imageUploadTokenBucketRepository;

    @Autowired
    private ImageUploadRateLimitProperties properties;

    @Autowired
    private MutableClock clock;

    private ImageUploadBucketKey memberKey;
    private ImageUploadBucketKey guestKey;

    @BeforeEach
    void setUp() {
        clock.setInstant(NOW.atZone(SEOUL).toInstant());
        memberKey = ImageUploadBucketKey.of(MEMBER_ID, null);
        guestKey = ImageUploadBucketKey.of(null, GUEST_ID);
    }

    @Test
    void 버킷이_없으면_생성_후_토큰을_차감한다() {
        assertThat(imageUploadTokenBucketRepository.findById(memberKey.value())).isEmpty();
        int capacity = properties.member().bucketCapacity();

        boolean result = imageUploadRateLimitService.tryConsume(memberKey, 3);

        assertSoftly(softly -> {
            softly.assertThat(result).isTrue();
            softly.assertThat(imageUploadTokenBucketRepository.findById(memberKey.value()))
                    .hasValueSatisfying(bucket -> softly.assertThat(bucket.getTokens()).isEqualTo(capacity - 3.0));
        });
    }

    @Test
    void 요청한_장수만큼_토큰을_차감한다() {
        saveBucket(memberKey, 10.0);

        boolean result = imageUploadRateLimitService.tryConsume(memberKey, 4);

        assertSoftly(softly -> {
            softly.assertThat(result).isTrue();
            softly.assertThat(tokensOf(memberKey)).isEqualTo(6.0);
        });
    }

    @Test
    void 토큰이_요청_장수보다_적으면_한_장도_차감하지_않는다() {
        saveBucket(memberKey, 3.0);

        boolean result = imageUploadRateLimitService.tryConsume(memberKey, 4);

        assertSoftly(softly -> {
            softly.assertThat(result).isFalse();
            softly.assertThat(tokensOf(memberKey)).isEqualTo(3.0);
        });
    }

    @Test
    void 토큰이_정확히_요청_장수만큼_남으면_차감을_허용한다() {
        saveBucket(memberKey, 4.0);

        boolean result = imageUploadRateLimitService.tryConsume(memberKey, 4);

        assertSoftly(softly -> {
            softly.assertThat(result).isTrue();
            softly.assertThat(tokensOf(memberKey)).isEqualTo(0.0);
        });
    }

    @Test
    void 회복_시간이_지나면_토큰이_충전된다() {
        saveBucket(memberKey, 0.0);
        int refillSeconds = properties.member().refillSeconds();
        clock.setInstant(NOW.plusSeconds(refillSeconds * 2L).atZone(SEOUL).toInstant());

        boolean result = imageUploadRateLimitService.tryConsume(memberKey, 2);

        assertThat(result).isTrue();
    }

    @Test
    void 회복량은_버킷_용량을_넘지_않는다() {
        saveBucket(memberKey, 0.0);
        int capacity = properties.member().bucketCapacity();
        int refillSeconds = properties.member().refillSeconds();
        clock.setInstant(NOW.plusSeconds(refillSeconds * (capacity + 100L)).atZone(SEOUL).toInstant());

        imageUploadRateLimitService.tryConsume(memberKey, 1);

        assertThat(tokensOf(memberKey)).isEqualTo(capacity - 1.0);
    }

    @Test
    void 회원과_비회원은_서로_다른_버킷을_사용한다() {
        imageUploadRateLimitService.tryConsume(memberKey, 1);
        imageUploadRateLimitService.tryConsume(guestKey, 1);

        assertSoftly(softly -> {
            softly.assertThat(tokensOf(memberKey))
                    .isEqualTo(properties.member().bucketCapacity() - 1.0);
            softly.assertThat(tokensOf(guestKey))
                    .isEqualTo(properties.guest().bucketCapacity() - 1.0);
        });
    }

    @Test
    void 보상하면_차감했던_토큰이_복구된다() {
        saveBucket(memberKey, 10.0);
        imageUploadRateLimitService.tryConsume(memberKey, 4);

        imageUploadRateLimitService.refund(memberKey, 4);

        assertThat(tokensOf(memberKey)).isEqualTo(10.0);
    }

    @Test
    void 보상해도_버킷_용량을_넘지_않는다() {
        int capacity = properties.member().bucketCapacity();
        saveBucket(memberKey, capacity);

        imageUploadRateLimitService.refund(memberKey, 4);

        assertThat(tokensOf(memberKey)).isEqualTo((double) capacity);
    }

    @Test
    void 보상은_회복_기준_시각을_미루지_않는다() {
        saveBucket(memberKey, 10.0);
        imageUploadRateLimitService.tryConsume(memberKey, 4);
        LocalDateTime consumedAt = updatedAtOf(memberKey);

        clock.setInstant(NOW.plusHours(1).atZone(SEOUL).toInstant());
        imageUploadRateLimitService.refund(memberKey, 4);

        assertThat(updatedAtOf(memberKey)).isEqualTo(consumedAt);
    }

    private void saveBucket(ImageUploadBucketKey key, double tokens) {
        imageUploadTokenBucketRepository.save(
                TestFixture.createImageUploadTokenBucket(key.value(), tokens, NOW));
    }

    private double tokensOf(ImageUploadBucketKey key) {
        return imageUploadTokenBucketRepository.findById(key.value()).orElseThrow().getTokens();
    }

    private LocalDateTime updatedAtOf(ImageUploadBucketKey key) {
        return imageUploadTokenBucketRepository.findById(key.value()).orElseThrow().getUpdatedAt();
    }
}
