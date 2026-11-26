package news.bombom.notification.config;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

/**
 * 어드민 전용 API의 헤더 키를 검증한다. 서버에 키가 설정되지 않았으면 모든 요청을 거부한다.
 */
@Component
public class AdminApiKeyVerifier {

    public static final String HEADER_NAME = "X-Admin-Api-Key";

    private final String apiKey;

    public AdminApiKeyVerifier(@Value("${notification.admin.api-key:}") String apiKey) {
        this.apiKey = apiKey;
    }

    public void verify(String requestApiKey) {
        if (!StringUtils.hasText(apiKey) || !StringUtils.hasText(requestApiKey)
                || !MessageDigest.isEqual(
                apiKey.getBytes(StandardCharsets.UTF_8),
                requestApiKey.getBytes(StandardCharsets.UTF_8))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "어드민 API 키가 유효하지 않습니다.");
        }
    }
}
