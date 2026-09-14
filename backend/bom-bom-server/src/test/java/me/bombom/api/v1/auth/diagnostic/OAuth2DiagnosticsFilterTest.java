package me.bombom.api.v1.auth.diagnostic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.servlet.ServletException;
import java.time.Clock;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class OAuth2DiagnosticsFilterTest {
    private final OAuth2Diagnostics diagnostics = new OAuth2Diagnostics(Clock.systemUTC(), "SESSION", "test");
    private final OAuth2DiagnosticsFilter filter = new OAuth2DiagnosticsFilter(diagnostics);

    @Test
    void Apple_POST_콜백의_예외에서도_요청_진단_정보를_정리한다() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/login/oauth2/code/apple");
        request.setServletPath("/login/oauth2/code/apple");
        request.setParameter("state", "secret-state");
        assertThatThrownBy(() -> filter.doFilter(request, new MockHttpServletResponse(), (req, res) -> {
            assertThat(diagnostics.snapshot()).containsEntry("provider", "apple");
            throw new ServletException("downstream failure");
        })).isInstanceOf(ServletException.class).hasMessage("downstream failure");
        assertThat(diagnostics.snapshot()).isEmpty();
    }

    @Test
    void 일반_API_요청에는_진단_세션을_생성하지_않는다() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/articles");
        request.setServletPath("/api/v1/articles");
        filter.doFilter(request, new MockHttpServletResponse(), (req, res) -> assertThat(diagnostics.snapshot()).isEmpty());
        assertThat(request.getSession(false)).isNull();
    }

    @Test
    void 진단_중_세션_조회가_실패해도_필터_체인을_중단하지_않는다() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/login/oauth2/code/google") {
            @Override
            public jakarta.servlet.http.HttpSession getSession(boolean create) {
                throw new IllegalStateException("diagnostic session read failed");
            }
        };
        request.setServletPath("/login/oauth2/code/google");
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, (req, res) -> {
            assertThat(diagnostics.snapshot()).containsEntry("diagnostic_error", true);
            ((jakarta.servlet.http.HttpServletResponse) res).setStatus(302);
        });
        assertThat(response.getStatus()).isEqualTo(302);
        assertThat(diagnostics.snapshot()).isEmpty();
    }
    @Test
    void 진단은_기존_세션_속성을_추가하거나_변경하지_않는다() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/login/oauth2/code/google");
        request.setServletPath("/login/oauth2/code/google");
        var session = request.getSession();
        Object authorization = new Object();
        session.setAttribute("authorization", authorization);
        filter.doFilter(request, new MockHttpServletResponse(), (req, res) -> {});
        assertThat(java.util.Collections.list(session.getAttributeNames())).containsExactly("authorization");
        assertThat(session.getAttribute("authorization")).isSameAs(authorization);
    }

}
