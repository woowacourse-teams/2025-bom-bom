package me.bombom.api.v1.subscribe.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import java.util.regex.Pattern;
import me.bombom.api.v1.common.exception.RetryableException;
import me.bombom.api.v1.subscribe.client.PlaywrightClient;
import me.bombom.api.v1.subscribe.config.SubscribePatternProperties;
import me.bombom.api.v1.subscribe.dto.UnsubscribePatterns;
import me.bombom.api.v1.subscribe.dto.response.PlaywrightResponse;
import me.bombom.api.v1.subscribe.exception.AutoUnsubscribeFailedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UnsubscribeAgentTest {

    private static final long MOCK_NEWSLETTER_ID = 1L;
    private static final String MOCK_URL = "https://example.com/unsubscribe";

    @Mock
    private PlaywrightClient playwrightClient;

    @Mock
    private SubscribePatternProperties properties;

    @InjectMocks
    private UnsubscribeAgent agent;

    @BeforeEach
    void setUp() {
        given(properties.getUnsubscribePattern()).willReturn(Pattern.compile("unsub"));
        given(properties.getSuccessPattern()).willReturn(Pattern.compile("success"));
        given(properties.getAlreadyUnsubscribedPattern()).willReturn(Pattern.compile("already"));
        given(properties.getErrorPattern()).willReturn(Pattern.compile("error"));
    }

    @Test
    @DisplayName("구독 취소 성공 시 예외가 발생하지 않는다")
    void 구독_취소_성공() {
        // given
        given(playwrightClient.executeUnsubscribe(eq(MOCK_URL), any(UnsubscribePatterns.class)))
                .willReturn(new PlaywrightResponse(200, true, "Success", "text_match"));

        // when & then
        assertThatCode(() -> agent.unsubscribe(MOCK_URL, MOCK_NEWSLETTER_ID))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("5xx 에러 발생 시 RetryableException을 발생시킨다")
    void 구독_취소_재시도_가능한_에러() {
        // given
        given(playwrightClient.executeUnsubscribe(eq(MOCK_URL), any(UnsubscribePatterns.class)))
                .willReturn(new PlaywrightResponse(500, false, "Server Error", null));

        // when & then
        assertThatThrownBy(() -> agent.unsubscribe(MOCK_URL, MOCK_NEWSLETTER_ID))
                .isInstanceOf(RetryableException.class);
    }

    @Test
    @DisplayName("4xx 에러 발생 시 AutoUnsubscribeFailedException을 발생시킨다")
    void 구독_취소_실패_에러() {
        // given
        given(playwrightClient.executeUnsubscribe(eq(MOCK_URL), any(UnsubscribePatterns.class)))
                .willReturn(new PlaywrightResponse(404, false, "Not Found", null));

        // when & then
        assertThatThrownBy(() -> agent.unsubscribe(MOCK_URL, MOCK_NEWSLETTER_ID))
                .isInstanceOf(AutoUnsubscribeFailedException.class);
    }

    @Test
    @Disabled("실제 Lambda를 호출하여 구독 취소를 수행한다 (실제 AWS 환경 변수 필요)")
    void 실제_람다_연동_테스트() {
        // given
        String realUnsubscribeUrl = "...";

        // when
        agent.unsubscribe(realUnsubscribeUrl, MOCK_NEWSLETTER_ID);
    }
}
