package me.bombom.api.v1.auth.handler;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.test.util.ReflectionTestUtils;

class OAuth2LoginFailureHandlerTest {

    @Test
    void 실패_로그는_민감정보_없이_구조화하고_기존_리다이렉트를_유지한다() throws Exception {
        OAuth2LoginFailureHandler handler = new OAuth2LoginFailureHandler(new me.bombom.api.v1.auth.diagnostic.OAuth2Diagnostics(
                java.time.Clock.systemUTC(), "JSESSIONID_PROD", "instance-1"));
        ReflectionTestUtils.setField(handler, "frontendBaseUrl", "https://www.bombom.news");
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/login/oauth2/code/google");
        request.setParameter("state", "secret-state");
        MockHttpServletResponse response = new MockHttpServletResponse();
        Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            handler.onAuthenticationFailure(request, response, new OAuth2AuthenticationException(
                    new OAuth2Error("invalid_user_info_response"), "Bearer secret-access-token user@example.com"));

            assertThat(response.getRedirectedUrl()).isEqualTo("https://www.bombom.news/login?error");
            assertThat(request.getSession(false)).isNull();
            String message = appender.list.getLast().getFormattedMessage();
            assertThat(message).startsWith("{")
                    .contains("oauth_login_failed", "invalid_user_info_response", "OAuth2 로그인 실패")
                    .doesNotContain("secret-access-token", "secret-state", "user@example.com");
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
    }
}
