package me.bombom.api.v1.coupon.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(CouponQueueProperties.class)
public class CouponQueueConfig {
}
