package me.bombom.api.v1.auth.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import me.bombom.api.v1.auth.service.LoadTestTokenService;
import me.bombom.api.v1.member.repository.MemberRepository;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class LoadTestAuthFilterTest {

    @Mock
    LoadTestTokenService loadTestTokenService;

    @Mock
    MemberRepository memberRepository;

    @Test
    void 보호_대상_아닌_경로는_토큰_검증을_거치지_않음() throws Exception {
        LoadTestAuthFilter filter = new LoadTestAuthFilter(
                List.of(new AntPathRequestMatcher("/api/v1/coupons/**")),
                loadTestTokenService,
                memberRepository,
                "X-LoadTest-Token",
                true
        );

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/auth/login");
        request.setServletPath("/api/v1/auth/login");
        request.addHeader("X-LoadTest-Token", "invalid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = org.mockito.Mockito.mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        verify(loadTestTokenService, never()).resolveMemberId(anyString());
        verify(chain).doFilter(eq(request), eq(response));
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void 보호_대상_경로는_유효하지_않은_토큰을_동일_코드로_거부() throws Exception {
        LoadTestAuthFilter filter = new LoadTestAuthFilter(
                List.of(new AntPathRequestMatcher("/api/v1/coupons/**")),
                loadTestTokenService,
                memberRepository,
                "X-LoadTest-Token",
                true
        );
        when(loadTestTokenService.resolveMemberId("invalid-token")).thenReturn(null);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/coupons/day1/queue-entries");
        request.setServletPath("/api/v1/coupons/day1/queue-entries");
        request.addHeader("X-LoadTest-Token", "invalid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = org.mockito.Mockito.mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        verify(memberRepository, never()).findById(999L);
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("\"code\":\"J002\"");
    }

    @Test
    void 보호_대상_경로는_토큰_미제공시_거부() throws Exception {
        LoadTestAuthFilter filter = new LoadTestAuthFilter(
                List.of(new AntPathRequestMatcher("/api/v1/coupons/**")),
                loadTestTokenService,
                memberRepository,
                "X-LoadTest-Token",
                true
        );

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/coupons/day1/queue-entries");
        request.setServletPath("/api/v1/coupons/day1/queue-entries");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = org.mockito.Mockito.mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("\"code\":\"J002\"");
    }

    @Test
    void 멤버_미존재_토큰도_동일_코드로_거부() throws Exception {
        LoadTestAuthFilter filter = new LoadTestAuthFilter(
                List.of(new AntPathRequestMatcher("/api/v1/coupons/**")),
                loadTestTokenService,
                memberRepository,
                "X-LoadTest-Token",
                true
        );
        when(loadTestTokenService.resolveMemberId("valid-but-deleted-member-token")).thenReturn(999L);
        when(memberRepository.findById(999L)).thenReturn(Optional.empty());

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/coupons/day1/queue-entries");
        request.setServletPath("/api/v1/coupons/day1/queue-entries");
        request.addHeader("X-LoadTest-Token", "valid-but-deleted-member-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = org.mockito.Mockito.mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("\"code\":\"J002\"");
    }
}
