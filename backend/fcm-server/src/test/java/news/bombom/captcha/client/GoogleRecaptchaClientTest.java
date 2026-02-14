package news.bombom.captcha.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import news.bombom.captcha.config.RecaptchaProperties;
import news.bombom.captcha.dto.response.GoogleRecaptchaResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class GoogleRecaptchaClientTest {

    @Mock
    private RecaptchaProperties recaptchaProperties;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private GoogleRecaptchaClient googleRecaptchaClient;

    @Captor
    private ArgumentCaptor<HttpEntity<MultiValueMap<String, String>>> requestCaptor;

    private static final String TEST_TOKEN = "test-recaptcha-token";
    private static final String TEST_IP = "192.168.1.1";
    private static final String TEST_SECRET_KEY = "test-secret-key";
    private static final String VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";

    @BeforeEach
    void setUp() {
        when(recaptchaProperties.getSecretKey()).thenReturn(TEST_SECRET_KEY);
        when(recaptchaProperties.getVerifyUrl()).thenReturn(VERIFY_URL);
    }

    @Test
    @DisplayName("Google API 호출 성공")
    void verify_success() {
        // given
        GoogleRecaptchaResponse expectedResponse = new GoogleRecaptchaResponse();
        expectedResponse.setSuccess(true);
        expectedResponse.setChallengeTs("2025-07-25T12:00:00Z");
        expectedResponse.setHostname("example.com");

        when(restTemplate.postForObject(anyString(), any(), eq(GoogleRecaptchaResponse.class)))
                .thenReturn(expectedResponse);

        // when
        GoogleRecaptchaResponse result = googleRecaptchaClient.verify(TEST_TOKEN, TEST_IP);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getChallengeTs()).isEqualTo("2025-07-25T12:00:00Z");
        assertThat(result.getHostname()).isEqualTo("example.com");
    }

    @Test
    @DisplayName("요청 파라미터가 올바르게 설정됨")
    void verify_request_parameters() {
        // given
        GoogleRecaptchaResponse response = new GoogleRecaptchaResponse();
        response.setSuccess(true);

        when(restTemplate.postForObject(eq(VERIFY_URL), requestCaptor.capture(), eq(GoogleRecaptchaResponse.class)))
                .thenReturn(response);

        // when
        googleRecaptchaClient.verify(TEST_TOKEN, TEST_IP);

        // then
        HttpEntity<MultiValueMap<String, String>> capturedRequest = requestCaptor.getValue();
        MultiValueMap<String, String> params = capturedRequest.getBody();

        assertThat(params).isNotNull();
        assertThat(params.getFirst("secret")).isEqualTo(TEST_SECRET_KEY);
        assertThat(params.getFirst("response")).isEqualTo(TEST_TOKEN);
        assertThat(params.getFirst("remoteip")).isEqualTo(TEST_IP);
    }

    @Test
    @DisplayName("Google API가 실패 응답을 반환")
    void verify_google_returns_failure() {
        // given
        GoogleRecaptchaResponse expectedResponse = new GoogleRecaptchaResponse();
        expectedResponse.setSuccess(false);
        expectedResponse.setErrorCodes(List.of("invalid-input-response"));

        when(restTemplate.postForObject(anyString(), any(), eq(GoogleRecaptchaResponse.class)))
                .thenReturn(expectedResponse);

        // when
        GoogleRecaptchaResponse result = googleRecaptchaClient.verify(TEST_TOKEN, TEST_IP);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorCodes()).containsExactly("invalid-input-response");
    }

    @Test
    @DisplayName("네트워크 오류 시 RecaptchaApiException 발생")
    void verify_network_error() {
        // given
        when(restTemplate.postForObject(anyString(), any(), eq(GoogleRecaptchaResponse.class)))
                .thenThrow(new RestClientException("Network error"));

        // when & then
        assertThatThrownBy(() -> googleRecaptchaClient.verify(TEST_TOKEN, TEST_IP))
                .isInstanceOf(RecaptchaApiException.class)
                .hasMessageContaining("Google reCAPTCHA API 호출 중 오류가 발생했습니다.")
                .hasCauseInstanceOf(RestClientException.class);
    }

    @Test
    @DisplayName("타임아웃 시 RecaptchaApiException 발생")
    void verify_timeout() {
        // given
        when(restTemplate.postForObject(anyString(), any(), eq(GoogleRecaptchaResponse.class)))
                .thenThrow(new RestClientException("Read timed out"));

        // when & then
        assertThatThrownBy(() -> googleRecaptchaClient.verify(TEST_TOKEN, TEST_IP))
                .isInstanceOf(RecaptchaApiException.class)
                .hasMessageContaining("Google reCAPTCHA API 호출 중 오류가 발생했습니다.");
    }

    @Test
    @DisplayName("올바른 URL로 API 호출")
    void verify_correct_url() {
        // given
        GoogleRecaptchaResponse response = new GoogleRecaptchaResponse();
        response.setSuccess(true);

        when(restTemplate.postForObject(anyString(), any(), eq(GoogleRecaptchaResponse.class)))
                .thenReturn(response);

        // when
        googleRecaptchaClient.verify(TEST_TOKEN, TEST_IP);

        // then
        verify(restTemplate).postForObject(eq(VERIFY_URL), any(), eq(GoogleRecaptchaResponse.class));
    }

    @Test
    @DisplayName("Content-Type이 application/x-www-form-urlencoded로 설정됨")
    void verify_content_type() {
        // given
        GoogleRecaptchaResponse response = new GoogleRecaptchaResponse();
        response.setSuccess(true);

        when(restTemplate.postForObject(anyString(), requestCaptor.capture(), eq(GoogleRecaptchaResponse.class)))
                .thenReturn(response);

        // when
        googleRecaptchaClient.verify(TEST_TOKEN, TEST_IP);

        // then
        HttpEntity<MultiValueMap<String, String>> capturedRequest = requestCaptor.getValue();
        assertThat(capturedRequest.getHeaders().getContentType().toString())
                .isEqualTo("application/x-www-form-urlencoded");
    }

    @Test
    @DisplayName("빈 토큰으로도 API 호출 가능 (Google에서 검증 실패)")
    void verify_empty_token() {
        // given
        GoogleRecaptchaResponse response = new GoogleRecaptchaResponse();
        response.setSuccess(false);
        response.setErrorCodes(List.of("missing-input-response"));

        when(restTemplate.postForObject(anyString(), any(), eq(GoogleRecaptchaResponse.class)))
                .thenReturn(response);

        // when
        GoogleRecaptchaResponse result = googleRecaptchaClient.verify("", TEST_IP);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();
    }
}
