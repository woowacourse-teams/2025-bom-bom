package me.bombom.api.log;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
public class MDCLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/.well-known/")) {
            filterChain.doFilter(request, response);
            return;
        }

        UUID traceId = UUID.randomUUID();
        MDC.put("traceId", traceId.toString());
        MDC.put("API", request.getRequestURI());
        MDC.put("method", request.getMethod());

        filterChain.doFilter(request, response);

        MDC.clear();
    }
}
