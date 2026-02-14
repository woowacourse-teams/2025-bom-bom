package news.bombom.captcha.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
@RequiredArgsConstructor
public class RestTemplateConfig {

    private final RecaptchaProperties recaptchaProperties;

    /**
     * reCAPTCHA 검증용 RestTemplate 타임아웃 설정은 application.yml에서 조정 가능
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(recaptchaProperties.getConnectTimeout());
        requestFactory.setReadTimeout(recaptchaProperties.getReadTimeout());

        return builder.requestFactory(() -> requestFactory)
                .build();
    }
}
