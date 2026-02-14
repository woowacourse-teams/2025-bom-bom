package me.bombom.api.v1.coupon.config;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties("coupon")
public class CouponQueueProperties {

    /**
     * 선착순 쿠폰 이벤트 설정 목록.
     *
     * application.yml 예시:
     *
     * coupon:
     *   events:
     *     - name: day1-coupon
     *       max-count: 35
     *       start-at: 2025-02-10T21:00:00
     *       end-at: 2025-02-10T21:10:00
     *       batch-size: 50
     *       active-limit: 50
     *       active-ttl-seconds: 30
     *       polling-interval-seconds: 3
     */
    private List<Event> events = List.of();

    @Getter
    @Setter
    public static class Event {

        /**
         * 쿠폰 이벤트를 식별할 이름 (대기열 키로 사용).
         */
        private String name;

        /**
         * 해당 이벤트의 최대 발급 수 (예: 하루 35장).
         */
        private long maxCount;

        /**
         * 이벤트 시작 일시 (이 시간 이전에는 발급 로직이 동작하지 않습니다).
         */
        private LocalDateTime startAt;

        /**
         * 이벤트 종료 일시 (이 시간 이후에는 발급 로직이 동작하지 않습니다).
         */
        private LocalDateTime endAt;

        /**
         * 한 번에 처리할 최대 인원 수 (동시에 "입장"시키는 사람 수 느낌, 기본값 50).
         */
        private long batchSize = 50L;

        /**
         * 쿠폰 이미지 URL.
         */
        private String imageUrl;

        /**
         * 동시에 입장 허용 가능한 최대 인원 수 (기본값 50).
         */
        private long activeLimit = 50L;

        /**
         * 입장 허용 후 유효 시간(초). 시간이 지나면 다음 사람에게 자리가 넘어갑니다.
         */
        private long activeTtlSeconds = 30L;

        /**
         * 프론트가 대기열 상태를 폴링할 권장 간격(초).
         */
        private int pollingIntervalSeconds = 3;

    }
}
