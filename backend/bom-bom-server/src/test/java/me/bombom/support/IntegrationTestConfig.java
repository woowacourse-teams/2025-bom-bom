package me.bombom.support;

import jakarta.persistence.EntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

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
