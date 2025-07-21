package me.bombom.api.v1.logging;

import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class LoggingInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        log.info("Request: [{}] {}", request.getMethod(), request.getRequestURI());
        return true;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            @Nullable Exception ex
    ) {
        if (ex != null) {
            log.error("예외 발생: {}", ex);
        } else {
            log.debug("요청 정상 완료: [{}] {}", request.getMethod(), request.getRequestURI());
        }
    }
}
