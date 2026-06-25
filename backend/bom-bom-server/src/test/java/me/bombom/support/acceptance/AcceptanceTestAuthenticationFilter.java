package me.bombom.support.acceptance;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import me.bombom.api.v1.auth.dto.CustomOAuth2User;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 인수 테스트 요청의 X-Test-Member-Id 헤더를 읽어 SecurityContext에 테스트 회원 인증을 주입한다.
 */
public class AcceptanceTestAuthenticationFilter extends OncePerRequestFilter {

    private final MemberRepository memberRepository;

    public AcceptanceTestAuthenticationFilter(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String memberIdHeader = request.getHeader(AcceptanceTestHeaders.MEMBER_ID);
        if (!StringUtils.hasText(memberIdHeader)) {
            filterChain.doFilter(request, response);
            return;
        }

        Member member = memberRepository.findById(Long.parseLong(memberIdHeader)).orElse(null);
        if (member == null) {
            filterChain.doFilter(request, response);
            return;
        }

        CustomOAuth2User principal = new CustomOAuth2User(
                Map.of(
                        "id", member.getId().toString(),
                        "email", member.getEmail(),
                        "name", member.getNickname()
                ),
                member,
                null,
                null
        );
        OAuth2AuthenticationToken authentication = new OAuth2AuthenticationToken(
                principal,
                principal.getAuthorities(),
                "acceptance-test"
        );
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        try {
            filterChain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
