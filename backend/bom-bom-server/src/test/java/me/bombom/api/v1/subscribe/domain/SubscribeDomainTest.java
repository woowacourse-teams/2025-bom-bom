package me.bombom.api.v1.subscribe.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class SubscribeDomainTest {

    @Test
    void AgeGroup은_생년으로_연령대를_계산하고_dbKey를_보관한다() {
        assertSoftly(softly -> {
            softly.assertThat(AgeGroup.fromBirthYear(2026, 2030)).isEqualTo(AgeGroup.AGE0S);
            softly.assertThat(AgeGroup.fromBirthYear(2026, 2011)).isEqualTo(AgeGroup.AGE10S);
            softly.assertThat(AgeGroup.fromBirthYear(2026, 2001)).isEqualTo(AgeGroup.AGE20S);
            softly.assertThat(AgeGroup.fromBirthYear(2026, 1991)).isEqualTo(AgeGroup.AGE30S);
            softly.assertThat(AgeGroup.fromBirthYear(2026, 1981)).isEqualTo(AgeGroup.AGE40S);
            softly.assertThat(AgeGroup.fromBirthYear(2026, 1971)).isEqualTo(AgeGroup.AGE50S);
            softly.assertThat(AgeGroup.fromBirthYear(2026, 1961)).isEqualTo(AgeGroup.AGE60PLUS);
            softly.assertThat(AgeGroup.AGE60PLUS.getDbKey()).isEqualTo("age60plus");
        });
    }

    @Test
    void NewsletterSubscriptionCount는_초기값과_연령대_버킷을_계산한다() {
        // when
        NewsletterSubscriptionCount count = NewsletterSubscriptionCount.from(1L);

        // then
        assertSoftly(softly -> {
            softly.assertThat(count.getNewsletterId()).isEqualTo(1L);
            softly.assertThat(count.getTotal()).isZero();
            softly.assertThat(NewsletterSubscriptionCount.toDecadeBucket(2026, 2026)).isZero();
            softly.assertThat(NewsletterSubscriptionCount.toDecadeBucket(2026, 1996)).isEqualTo(3);
            softly.assertThat(NewsletterSubscriptionCount.toDecadeBucket(2026, 1950)).isEqualTo(6);
        });
    }

    @Test
    void NewsletterSubscriptionCount는_미래_출생연도를_거부한다() {
        assertThatThrownBy(() -> NewsletterSubscriptionCount.toDecadeBucket(2026, 2027))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("출생연도가 미래입니다");
    }

    @Test
    void Subscribe는_상태와_소유자를_판별한다() {
        // given
        Subscribe subscribe = Subscribe.builder()
                .memberId(1L)
                .newsletterId(10L)
                .unsubscribeUrl("https://newsletter.example/unsubscribe")
                .build();

        // when
        subscribe.changeStatus(SubscribeStatus.UNSUBSCRIBING);

        // then
        assertSoftly(softly -> {
            softly.assertThat(subscribe.isNotOwner(2L)).isTrue();
            softly.assertThat(subscribe.isNotOwner(1L)).isFalse();
            softly.assertThat(subscribe.isUnsubscribing()).isTrue();
            softly.assertThat(subscribe.isFailedToUnsubscribe()).isFalse();
        });

        // when
        subscribe.changeStatus(SubscribeStatus.UNSUBSCRIBE_FAILED);

        // then
        assertThat(subscribe.isFailedToUnsubscribe()).isTrue();
    }

    @Test
    void UnsubscribeRetry는_재시도마다_backoff와_마지막_오류를_갱신한다() {
        // given
        LocalDateTime now = LocalDateTime.of(2026, 1, 1, 0, 0);
        UnsubscribeRetry retry = UnsubscribeRetry.builder()
                .subscribeId(1L)
                .nextRetryAt(now)
                .lastError("initial")
                .build();

        // when
        retry.increaseRetryCount(now, "network");

        // then
        assertSoftly(softly -> {
            softly.assertThat(retry.getRetryCount()).isEqualTo(1);
            softly.assertThat(retry.getNextRetryAt()).isEqualTo(now.plusMinutes(10));
            softly.assertThat(retry.getLastError()).isEqualTo("network");
            softly.assertThat(retry.isMaxRetryReached()).isFalse();
        });

        // when
        retry.increaseRetryCount(now, "maintenance");
        retry.increaseRetryCount(now, "timeout");

        // then
        assertSoftly(softly -> {
            softly.assertThat(retry.getRetryCount()).isEqualTo(3);
            softly.assertThat(retry.getNextRetryAt()).isEqualTo(now.plusHours(2));
            softly.assertThat(retry.isMaxRetryReached()).isTrue();
        });
    }
}
