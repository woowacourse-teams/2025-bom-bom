package me.bombom.api.v1.auth.diagnostic;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class OAuth2DiagnosticsFilter extends OncePerRequestFilter {
    private final OAuth2Diagnostics diagnostics;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return !path.startsWith("/oauth2/authorization/") && !path.startsWith("/login/oauth2/code/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            diagnostics.open(request);
            chain.doFilter(request, response);
        } finally {
            diagnostics.close();
        }
    }
}
