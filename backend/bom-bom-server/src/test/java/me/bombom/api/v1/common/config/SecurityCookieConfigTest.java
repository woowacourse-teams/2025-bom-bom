package me.bombom.api.v1.common.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.test.util.ReflectionTestUtils;

class SecurityCookieConfigTest {

    @Test
    void dev_프로필은_Lax_쿠키를_적용하고_기존_클라이언트의_CSRF_호환성을_유지한다() {
        new ApplicationContextRunner()
                .withPropertyValues("spring.profiles.active=dev")
                .withInitializer(new ConfigDataApplicationContextInitializer())
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    var properties = context.getEnvironment();
                    assertThat(properties.getActiveProfiles()).containsExactly("dev");
                    assertThat(properties.getProperty("app.security.csrf-enabled", Boolean.class)).isFalse();
                    assertThat(properties.getProperty("app.cors.allowed-origins"))
                            .contains("https://dev.bombom.news").doesNotContain("*");
                    assertThat(properties.getProperty("app.auth.apple-request-cookie-name"))
                            .isEqualTo("__Host-BOMBOM_APPLE_AUTH_DEV");
                    var config = new SecurityConfig();
                    ReflectionTestUtils.setField(config, "cookieSameSite", properties.getProperty("server.servlet.session.cookie.same-site"));
                    ReflectionTestUtils.setField(config, "cookieName", properties.getProperty("server.servlet.session.cookie.name"));
                    ReflectionTestUtils.setField(config, "cookieDomain", "bombom.news");
                    ReflectionTestUtils.setField(config, "cookieMaxAge", Duration.ofDays(1));
                    var response = new MockHttpServletResponse();
                    config.cookieSerializer().writeCookieValue(new CookieSerializer.CookieValue(
                            new MockHttpServletRequest(), response, "test-session"));
                    assertThat(response.getHeader("Set-Cookie"))
                            .contains("JSESSIONID_DEV=", "SameSite=Lax", "Secure", "HttpOnly");
                });
    }
}
