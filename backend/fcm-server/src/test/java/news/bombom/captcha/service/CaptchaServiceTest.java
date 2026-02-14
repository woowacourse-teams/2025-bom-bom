package news.bombom.captcha.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import news.bombom.captcha.client.GoogleRecaptchaClient;
import news.bombom.captcha.config.RecaptchaProperties;
import news.bombom.captcha.dto.response.GoogleRecaptchaResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CaptchaServiceTest {

    @Mock
    private GoogleRecaptchaClient googleRecaptchaClient;

    @Mock
    private RecaptchaProperties recaptchaProperties;

    @InjectMocks
    private CaptchaService captchaService;

    private static final String TEST_IP = "127.0.0.1";
    private static final String TEST_TOKEN = "test-token";

    @BeforeEach
    void setUp() {
        lenient().when(recaptchaProperties.getMaxAgeSeconds()).thenReturn(120L);
    }

    @Test
    @DisplayName("reCAPTCHA 검증 성공")
    void verify_success() {
        // given
        String currentTime = Instant.now().toString();
        
        GoogleRecaptchaResponse mockResponse = new GoogleRecaptchaResponse();
        mockResponse.setSuccess(true);
        mockResponse.setChallengeTs(currentTime);
        mockResponse.setHostname("example.com");

        when(googleRecaptchaClient.verify(anyString(), anyString()))
                .thenReturn(mockResponse);

        // when
        var result = captchaService.verify(TEST_TOKEN, TEST_IP);

        // then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.message()).isEqualTo("캡차 검증 성공");
    }

    @Test
    @DisplayName("reCAPTCHA 검증 실패 - Google에서 success=false 반환")
    void verify_fail_google_returns_false() {
        // given
        GoogleRecaptchaResponse mockResponse = new GoogleRecaptchaResponse();
        mockResponse.setSuccess(false);
        mockResponse.setErrorCodes(List.of("invalid-input-response"));

        when(googleRecaptchaClient.verify(anyString(), anyString()))
                .thenReturn(mockResponse);

        // when
        var result = captchaService.verify(TEST_TOKEN, TEST_IP);

        // then
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.message()).isEqualTo("캡차 검증에 실패했습니다.");
    }

    @Test
    @DisplayName("reCAPTCHA 검증 실패 - 토큰 만료 (2분 초과)")
    void verify_fail_token_expired() {
        // given
        // 3분 전 시간
        String expiredTime = Instant.now().minusSeconds(180).toString();
        
        GoogleRecaptchaResponse mockResponse = new GoogleRecaptchaResponse();
        mockResponse.setSuccess(true);
        mockResponse.setChallengeTs(expiredTime);
        mockResponse.setHostname("example.com");

        when(googleRecaptchaClient.verify(anyString(), anyString()))
                .thenReturn(mockResponse);

        // when
        var result = captchaService.verify(TEST_TOKEN, TEST_IP);

        // then
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.message()).isEqualTo("캡차 토큰이 만료되었습니다.");
    }

    @Test
    @DisplayName("reCAPTCHA 검증 성공 - 2분 이내 토큰")
    void verify_success_within_two_minutes() {
        // given
        // 1분 전 시간
        String recentTime = Instant.now().minusSeconds(60).toString();
        
        GoogleRecaptchaResponse mockResponse = new GoogleRecaptchaResponse();
        mockResponse.setSuccess(true);
        mockResponse.setChallengeTs(recentTime);
        mockResponse.setHostname("example.com");

        when(googleRecaptchaClient.verify(anyString(), anyString()))
                .thenReturn(mockResponse);

        // when
        var result = captchaService.verify(TEST_TOKEN, TEST_IP);

        // then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.message()).isEqualTo("캡차 검증 성공");
    }

    @Test
    @DisplayName("reCAPTCHA 검증 실패 - Google 응답이 null")
    void verify_fail_null_response() {
        // given
        when(googleRecaptchaClient.verify(anyString(), anyString()))
                .thenReturn(null);

        // when
        var result = captchaService.verify(TEST_TOKEN, TEST_IP);

        // then
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.message()).isEqualTo("캡차 검증 중 오류가 발생했습니다.");
    }

    @Test
    @DisplayName("reCAPTCHA 검증 실패 - challenge_ts가 null")
    void verify_fail_null_challenge_ts() {
        // given
        GoogleRecaptchaResponse mockResponse = new GoogleRecaptchaResponse();
        mockResponse.setSuccess(true);
        mockResponse.setChallengeTs(null);
        mockResponse.setHostname("example.com");

        when(googleRecaptchaClient.verify(anyString(), anyString()))
                .thenReturn(mockResponse);

        // when
        var result = captchaService.verify(TEST_TOKEN, TEST_IP);

        // then
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.message()).isEqualTo("캡차 토큰이 만료되었습니다.");
    }

    @Test
    @DisplayName("reCAPTCHA 검증 실패 - 예외 발생")
    void verify_fail_exception() {
        // given
        when(googleRecaptchaClient.verify(anyString(), anyString()))
                .thenThrow(new RuntimeException("Network error"));

        // when
        var result = captchaService.verify(TEST_TOKEN, TEST_IP);

        // then
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.message()).isEqualTo("캡차 검증 중 오류가 발생했습니다.");
    }
}
