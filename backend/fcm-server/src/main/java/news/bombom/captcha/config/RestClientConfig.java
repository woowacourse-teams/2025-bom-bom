package news.bombom.captcha.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
public class RestClientConfig {

    private final RecaptchaProperties recaptchaProperties;

    /**
     * reCAPTCHA 검증용 RestClient 타임아웃 설정은 application.yml에서 조정 가능
     */
    @Bean
    public RestClient restClient(RestClient.Builder builder) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(recaptchaProperties.getConnectTimeout());
        requestFactory.setReadTimeout(recaptchaProperties.getReadTimeout());

        return builder.requestFactory(requestFactory)
                .build();
    }
}
