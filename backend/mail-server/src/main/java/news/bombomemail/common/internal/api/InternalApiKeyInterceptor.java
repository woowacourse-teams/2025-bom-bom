package news.bombomemail.common.internal.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class InternalApiKeyInterceptor implements HandlerInterceptor {

    public static final String INTERNAL_API_KEY_HEADER = "X-Internal-Api-Key";

    @Value("${internal.api.key:}")
    private String configuredApiKey;

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler
    ) {
        if (!StringUtils.hasText(configuredApiKey)) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "내부 API 키가 설정되지 않았습니다.");
        }

        String requestApiKey = request.getHeader(INTERNAL_API_KEY_HEADER);
        if (!configuredApiKey.equals(requestApiKey)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "내부 API 키가 올바르지 않습니다.");
        }

        return true;
    }
}
