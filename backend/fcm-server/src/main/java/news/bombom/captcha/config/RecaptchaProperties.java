package news.bombom.captcha.config;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "recaptcha")
public class RecaptchaProperties {

    private String secretKey;
    private String verifyUrl = "https://www.google.com/recaptcha/api/siteverify";
    
    /**
     * reCAPTCHA 토큰 유효 시간 (초)
     */
    private long maxAgeSeconds = 120;
    
    /**
     * Google API 연결 타임아웃
     */
    private Duration connectTimeout = Duration.ofSeconds(3);
    
    /**
     * Google API 응답 읽기 타임아웃
     */
    private Duration readTimeout = Duration.ofSeconds(5);
}
