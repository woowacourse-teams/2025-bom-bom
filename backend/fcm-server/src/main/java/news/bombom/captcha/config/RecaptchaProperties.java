package news.bombom.captcha.config;

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
    private long maxAgeSeconds = 120;
}
