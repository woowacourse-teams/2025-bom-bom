package me.bombom.api.v1.auth.filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.auth.dto.CustomOAuth2User;
import me.bombom.api.v1.auth.service.LoadTestTokenService;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.enums.Gender;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
public class LoadTestAuthFilter extends OncePerRequestFilter {

    private static final String LOAD_TEST_TOKEN_PATH = "/api/v1/auth/load-test/tokens";
    private final AntPathRequestMatcher tokenIssuePathMatcher = new AntPathRequestMatcher(LOAD_TEST_TOKEN_PATH);
    private final List<AntPathRequestMatcher> protectedPathMatchers;

    private final LoadTestTokenService loadTestTokenService;
    private final String headerName;
    private final boolean enabled;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (!enabled) {
            filterChain.doFilter(request, response);
            return;
        }

        if (tokenIssuePathMatcher.matches(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!isProtectedPath(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = request.getHeader(headerName);
        if (token == null || token.isBlank()) {
            log.warn("Missing load test token. headerName={}, path={}", headerName, request.getServletPath());
            sendUnauthorized(response);
            return;
        }

        Long memberId = loadTestTokenService.resolveMemberId(token);
        if (memberId == null) {
            log.warn("Invalid load test token. headerName={}, path={}", headerName, request.getServletPath());
            sendUnauthorized(response);
            return;
        }

        Member member = Member.builder()
                .id(memberId)
                .provider("load-test")
                .providerId(String.valueOf(memberId))
                .email(memberId + "@load-test.local")
                .nickname("load-test-" + memberId)
                .gender(Gender.NONE)
                .roleId(1L)
                .build();

        var attributes = Collections.<String, Object>emptyMap();
        CustomOAuth2User principal = new CustomOAuth2User(attributes, member, null, null);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.getAuthorities()
        );
        ((UsernamePasswordAuthenticationToken) authentication).setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request)
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }

    private void sendUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(ErrorDetail.INVALID_TOKEN.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("""
            {
                "status":"%s",
                "code":"%s",
                "message":"%s"
            }
            """.formatted(
                ErrorDetail.INVALID_TOKEN.getStatus(),
                ErrorDetail.INVALID_TOKEN.getCode(),
                ErrorDetail.INVALID_TOKEN.getMessage()
            ));
    }

    private boolean isProtectedPath(HttpServletRequest request) {
        for (AntPathRequestMatcher matcher : protectedPathMatchers) {
            if (matcher.matches(request)) {
                return true;
            }
        }
        return false;
    }
}
