package me.bombom.support.integration;

import jakarta.persistence.EntityManager;
import me.bombom.support.auth.FakeAppleOAuth2Service;
import me.bombom.support.auth.FakeOAuth2LoginSuccessHandler;
import me.bombom.support.notification.FakeDiscordWebhookNotifier;
import me.bombom.support.persistence.CleanUp;
import me.bombom.support.subscribe.FakeUnsubscribeAgent;
import me.bombom.support.time.MutableClock;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 통합 테스트에서 외부 연동 Bean을 테스트 대역으로 교체하고 DB 정리 도구를 등록한다.
 */
@TestConfiguration(proxyBeanMethods = false)
public class IntegrationTestConfig {

    @Bean
    @Primary
    MutableClock mutableClock() {
        return new MutableClock();
    }

    @Bean
    @Primary
    FakeOAuth2LoginSuccessHandler fakeOAuth2LoginSuccessHandler() {
        return new FakeOAuth2LoginSuccessHandler();
    }

    @Bean
    @Primary
    FakeDiscordWebhookNotifier fakeDiscordWebhookNotifier() {
        return new FakeDiscordWebhookNotifier();
    }

    @Bean
    @Primary
    FakeUnsubscribeAgent fakeUnsubscribeAgent() {
        return new FakeUnsubscribeAgent();
    }

    @Bean
    @Primary
    FakeAppleOAuth2Service fakeAppleOAuth2Service() {
        return new FakeAppleOAuth2Service();
    }

    @Bean
    CleanUp cleanUp(EntityManager entityManager, JdbcTemplate jdbcTemplate) {
        return new CleanUp(entityManager, jdbcTemplate);
    }
}
