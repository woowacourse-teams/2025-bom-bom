package me.bombom.api.v1.subscribe.client;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

class WebRiskConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withInitializer(context -> context.getBeanFactory()
                    .setConversionService(ApplicationConversionService.getSharedInstance()))
            .withBean(RestClient.Builder.class, RestClient::builder)
            .withUserConfiguration(WebRiskClient.class);

    @Test
    void 설정이_없어도_기본_타임아웃으로_시작하고_검사를_생략한다() {
        runner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context.getBean(WebRiskClient.class).isThreat("https://example.com")).isFalse();
        });
    }

    @Test
    void YAML_프로퍼티로_키와_타임아웃을_읽는다() {
        runner.withPropertyValues("google.web-risk.api-key=yaml-test-key",
                        "google.web-risk.connect-timeout=1s", "google.web-risk.read-timeout=2s")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(ReflectionTestUtils.getField(context.getBean(WebRiskClient.class), "apiKey"))
                            .isEqualTo("yaml-test-key");
                });
    }

    @Test
    void 환경변수_이름으로_키를_설정할_수_있다() {
        runner.withPropertyValues("GOOGLE_WEB_RISK_API_KEY=environment-test-key")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(ReflectionTestUtils.getField(context.getBean(WebRiskClient.class), "apiKey"))
                            .isEqualTo("environment-test-key");
                });
    }
}
