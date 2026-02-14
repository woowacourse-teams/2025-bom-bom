package news.bombom.captcha.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import news.bombom.captcha.config.RecaptchaProperties;
import news.bombom.captcha.dto.response.GoogleRecaptchaResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleRecaptchaClient {

    private final RecaptchaProperties recaptchaProperties;
    private final RestTemplate restTemplate;

    public GoogleRecaptchaResponse verify(String gRecaptchaResponse, String remoteIp) {
        try {
            MultiValueMap<String, String> params = buildRequestParams(gRecaptchaResponse, remoteIp);
            HttpEntity<MultiValueMap<String, String>> request = buildRequest(params);
            return restTemplate.postForObject(recaptchaProperties.getVerifyUrl(), request, GoogleRecaptchaResponse.class);
        } catch (Exception e) {
            log.error("Google reCAPTCHA API 호출 실패: {}", e.getMessage(), e);
            throw new RecaptchaApiException("Google reCAPTCHA API 호출 중 오류가 발생했습니다.", e);
        }
    }

    private MultiValueMap<String, String> buildRequestParams(String gRecaptchaResponse, String remoteIp) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("secret", recaptchaProperties.getSecretKey());
        params.add("response", gRecaptchaResponse);
        if (StringUtils.hasText(remoteIp)) {
            params.add("remoteip", remoteIp);
        }
        return params;
    }

    private HttpEntity<MultiValueMap<String, String>> buildRequest(MultiValueMap<String, String> params) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return new HttpEntity<>(params, headers);
    }
}
