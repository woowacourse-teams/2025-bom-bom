package news.bombom.captcha.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.List;
import news.bombom.captcha.config.RecaptchaProperties;
import news.bombom.captcha.dto.response.GoogleRecaptchaResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.client.MockServerRestClientCustomizer;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

@DisplayName("GoogleRecaptchaClient 테스트")
class GoogleRecaptchaClientTest {

    private MockRestServiceServer mockServer;
    private GoogleRecaptchaClient googleRecaptchaClient;
    private RecaptchaProperties recaptchaProperties;
    private ObjectMapper objectMapper;

    private static final String TEST_TOKEN = "test-recaptcha-token";
    private static final String TEST_IP = "192.168.1.1";
    private static final String TEST_SECRET_KEY = "test-secret-key";
    private static final String VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";

    @BeforeEach
    void setUp() {
        recaptchaProperties = new RecaptchaProperties();
        recaptchaProperties.setSecretKey(TEST_SECRET_KEY);
        recaptchaProperties.setVerifyUrl(VERIFY_URL);
        recaptchaProperties.setConnectTimeout(Duration.ofSeconds(3));
        recaptchaProperties.setReadTimeout(Duration.ofSeconds(5));

        // MockServerRestClientCustomizer 사용
        MockServerRestClientCustomizer customizer = new MockServerRestClientCustomizer();
        RestClient.Builder builder = RestClient.builder();
        customizer.customize(builder);
        
        mockServer = customizer.getServer();
        googleRecaptchaClient = new GoogleRecaptchaClient(recaptchaProperties, builder.build());
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Google API 호출 성공")
    void verify_success() throws Exception {
        // given
        GoogleRecaptchaResponse expectedResponse = new GoogleRecaptchaResponse();
        expectedResponse.setSuccess(true);
        expectedResponse.setChallengeTs("2025-07-25T12:00:00Z");
        expectedResponse.setHostname("example.com");

        mockServer.expect(requestTo(VERIFY_URL))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(objectMapper.writeValueAsString(expectedResponse), MediaType.APPLICATION_JSON));

        // when
        GoogleRecaptchaResponse result = googleRecaptchaClient.verify(TEST_TOKEN, TEST_IP);

        // then
        mockServer.verify();
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getChallengeTs()).isEqualTo("2025-07-25T12:00:00Z");
        assertThat(result.getHostname()).isEqualTo("example.com");
    }

    @Test
    @DisplayName("Google API가 실패 응답을 반환")
    void verify_google_returns_failure() throws Exception {
        // given
        GoogleRecaptchaResponse expectedResponse = new GoogleRecaptchaResponse();
        expectedResponse.setSuccess(false);
        expectedResponse.setErrorCodes(List.of("invalid-input-response"));

        mockServer.expect(requestTo(VERIFY_URL))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(objectMapper.writeValueAsString(expectedResponse), MediaType.APPLICATION_JSON));

        // when
        GoogleRecaptchaResponse result = googleRecaptchaClient.verify(TEST_TOKEN, TEST_IP);

        // then
        mockServer.verify();
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorCodes()).containsExactly("invalid-input-response");
    }

    @Test
    @DisplayName("서버 오류 시 RecaptchaApiException 발생")
    void verify_server_error() {
        // given
        mockServer.expect(requestTo(VERIFY_URL))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withServerError());

        // when & then
        assertThatThrownBy(() -> googleRecaptchaClient.verify(TEST_TOKEN, TEST_IP))
                .isInstanceOf(RecaptchaApiException.class)
                .hasMessageContaining("Google reCAPTCHA API 호출 중 오류가 발생했습니다.");

        mockServer.verify();
    }

    @Test
    @DisplayName("빈 토큰으로도 API 호출 가능")
    void verify_empty_token() throws Exception {
        // given
        GoogleRecaptchaResponse response = new GoogleRecaptchaResponse();
        response.setSuccess(false);
        response.setErrorCodes(List.of("missing-input-response"));

        mockServer.expect(requestTo(VERIFY_URL))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(objectMapper.writeValueAsString(response), MediaType.APPLICATION_JSON));

        // when
        GoogleRecaptchaResponse result = googleRecaptchaClient.verify("", TEST_IP);

        // then
        mockServer.verify();
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();
    }

    @Test
    @DisplayName("RecaptchaApiException 생성자 테스트")
    void recaptcha_api_exception() {
        // given
        String message = "테스트 메시지";
        Exception cause = new RuntimeException("원인");

        // when
        RecaptchaApiException exception = new RecaptchaApiException(message, cause);

        // then
        assertThat(exception).isInstanceOf(RuntimeException.class);
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
    }
}
