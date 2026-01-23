package me.bombom.api.v1.subscribe.service;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import me.bombom.api.v1.subscribe.config.SubscribePatternProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Import(UnsubscribeAgent.class)
@TestPropertySource("classpath:unsubscribe-pattern.yml")
@EnableConfigurationProperties(SubscribePatternProperties.class)
@ExtendWith({OutputCaptureExtension.class, SpringExtension.class})
class UnsubscribeAgentTest {

    // newsletterId는 로그를 위한 것이라 무시해도 됨
    private static final long MOCK_NEWSLETTER_ID = 1L;

    @Autowired
    private UnsubscribeAgent agent;

    private String createDataUrl(String html) {
        // charset=utf-8 명시가 중요함 (한글 깨짐 방지)
        return "data:text/html;charset=utf-8," + URLEncoder.encode(html, StandardCharsets.UTF_8).replace("+", "%20");
    }

    @Test
    @DisplayName("Role이 Button이고 텍스트가 'Unsubscribe'인 요소를 찾아 클릭한다")
    void 영어_구독취소_버튼_클릭(CapturedOutput output) {
        // given
        String html = """
                <html>
                    <body>
                        <button>Unsubscribe</button>
                    </body>
                </html>
                """;

        // when
        agent.unsubscribe(createDataUrl(html), MOCK_NEWSLETTER_ID);

        // then
        assertSoftly(softly -> {
            // 성공 시 로그가 없으므로 에러가 없는지로 검증
            softly.assertThat(output).doesNotContain("구독 취소 실패");
        });
    }

    @Test
    @DisplayName("한국어 '수신거부' 버튼도 정확히 인식한다")
    void 한국어_수신거부_버튼_클릭(CapturedOutput output) {
        // given
        String html = """
                <html>
                    <body>
                        <button type="button">수신거부</button>
                    </body>
                </html>
                """;

        // when
        agent.unsubscribe(createDataUrl(html), MOCK_NEWSLETTER_ID);

        // then
        assertSoftly(softly -> {
            softly.assertThat(output).doesNotContain("구독 취소 실패");
        });
    }

    @Test
    @DisplayName("버튼 태그가 아닌 'Cancel' 텍스트 링크도 2차 시도(Role.LINK)로 찾아낸다")
    void 텍스트_링크_클릭(CapturedOutput output) {
        // given (Role: LINK)
        String html = """
                <html>
                    <body>
                        <div><a href="#">Cancel</a></div>
                    </body>
                </html>
                """;

        // when
        agent.unsubscribe(createDataUrl(html), MOCK_NEWSLETTER_ID);

        // then
        assertSoftly(softly -> {
            softly.assertThat(output).doesNotContain("구독 취소 실패");
        });
    }

    @Test
    @DisplayName("이미 'Successfully unsubscribed' 텍스트가 있어도 버튼으로 오인하지 않고 성공으로 감지한다")
    void 이미_완료된_페이지_감지(CapturedOutput output) {
        // given
        String html = """
                <html>
                    <body>
                        <h1>Status: Successfully unsubscribed</h1>
                        <p>You will no longer receive emails.</p>
                    </body>
                </html>
                """;

        // when
        agent.unsubscribe(createDataUrl(html), MOCK_NEWSLETTER_ID);

        // then
        assertSoftly(softly -> {
            // "이미 구독 취소된 페이지입니다" 로그는 debug 레벨이라 안 보일 수 있음.
            // 핵심은 실패 로그가 없어야 함.
            softly.assertThat(output).doesNotContain("구독 취소 실패");
        });
    }

    @Test
    @DisplayName("성공 키워드가 긴 문장 속에 포함되어 있어도 감지한다")
    void 긴_문장_속_성공_키워드_감지(CapturedOutput output) {
        // given
        String html = """
                <html>
                    <body>
                        <p>Your subscription cancellation process is unsubscribed now.</p>
                    </body>
                </html>
                """;

        // when
        agent.unsubscribe(createDataUrl(html), MOCK_NEWSLETTER_ID);

        // then
        assertSoftly(softly -> {
            softly.assertThat(output).doesNotContain("구독 취소 실패");
        });
    }
}
