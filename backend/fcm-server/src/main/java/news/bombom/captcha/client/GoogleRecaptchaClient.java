package news.bombom.captcha.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import news.bombom.captcha.config.RecaptchaProperties;
import news.bombom.captcha.dto.response.GoogleRecaptchaResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleRecaptchaClient {

    private final RecaptchaProperties recaptchaProperties;
    private final RestClient restClient;

    public GoogleRecaptchaResponse verify(String gRecaptchaResponse, String remoteIp) {
        try {
            MultiValueMap<String, String> params = buildRequestParams(gRecaptchaResponse, remoteIp);

            return restClient.post()
                    .uri(recaptchaProperties.getVerifyUrl())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(params)
                    .retrieve()
                    .body(GoogleRecaptchaResponse.class);
        } catch (Exception e) {
            log.error("Google reCAPTCHA API 호출 실패: {}", e.getMessage(), e);
            throw new RecaptchaApiException("Google reCAPTCHA API 호출 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * API 요청 파라미터 생성
     */
    private MultiValueMap<String, String> buildRequestParams(String gRecaptchaResponse, String remoteIp) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("secret", recaptchaProperties.getSecretKey());
        params.add("response", gRecaptchaResponse);
        // remoteip는 선택적 파라미터 (유효한 값이 있는 경우만 추가)
        if (StringUtils.hasText(remoteIp)) {
            params.add("remoteip", remoteIp);
        }
        return params;
    }
}

