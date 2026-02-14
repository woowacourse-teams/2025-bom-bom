package news.bombom.captcha.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ClientIpResolverTest {

    @Mock
    private HttpServletRequest request;

    @Test
    @DisplayName("X-Forwarded-For 헤더에서 IP 추출")
    void getClientIp_from_x_forwarded_for() {
        // given
        String expectedIp = "192.168.1.100";
        when(request.getHeader("X-Forwarded-For")).thenReturn(expectedIp);

        // when
        String actualIp = ClientIpResolver.getClientIp(request);

        // then
        assertThat(actualIp).isEqualTo(expectedIp);
    }

    @Test
    @DisplayName("X-Forwarded-For에 여러 IP가 있는 경우 첫 번째 IP 추출")
    void getClientIp_from_x_forwarded_for_multiple() {
        // given
        String multipleIps = "192.168.1.100, 10.0.0.1, 172.16.0.1";
        when(request.getHeader("X-Forwarded-For")).thenReturn(multipleIps);

        // when
        String actualIp = ClientIpResolver.getClientIp(request);

        // then
        assertThat(actualIp).isEqualTo("192.168.1.100");
    }

    @Test
    @DisplayName("X-Forwarded-For가 unknown인 경우 Proxy-Client-IP에서 추출")
    void getClientIp_from_proxy_client_ip() {
        // given
        String expectedIp = "192.168.1.101";
        when(request.getHeader("X-Forwarded-For")).thenReturn("unknown");
        when(request.getHeader("Proxy-Client-IP")).thenReturn(expectedIp);

        // when
        String actualIp = ClientIpResolver.getClientIp(request);

        // then
        assertThat(actualIp).isEqualTo(expectedIp);
    }

    @Test
    @DisplayName("모든 헤더가 없는 경우 RemoteAddr에서 추출")
    void getClientIp_from_remote_addr() {
        // given
        String expectedIp = "127.0.0.1";
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("Proxy-Client-IP")).thenReturn(null);
        when(request.getHeader("WL-Proxy-Client-IP")).thenReturn(null);
        when(request.getHeader("HTTP_CLIENT_IP")).thenReturn(null);
        when(request.getHeader("HTTP_X_FORWARDED_FOR")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn(expectedIp);

        // when
        String actualIp = ClientIpResolver.getClientIp(request);

        // then
        assertThat(actualIp).isEqualTo(expectedIp);
    }

    @Test
    @DisplayName("빈 문자열은 무시하고 다음 헤더 확인")
    void getClientIp_skip_empty_string() {
        // given
        String expectedIp = "192.168.1.102";
        when(request.getHeader("X-Forwarded-For")).thenReturn("");
        when(request.getHeader("Proxy-Client-IP")).thenReturn(expectedIp);

        // when
        String actualIp = ClientIpResolver.getClientIp(request);

        // then
        assertThat(actualIp).isEqualTo(expectedIp);
    }

    @Test
    @DisplayName("WL-Proxy-Client-IP 헤더에서 IP 추출")
    void getClientIp_from_wl_proxy_client_ip() {
        // given
        String expectedIp = "192.168.1.103";
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("Proxy-Client-IP")).thenReturn(null);
        when(request.getHeader("WL-Proxy-Client-IP")).thenReturn(expectedIp);

        // when
        String actualIp = ClientIpResolver.getClientIp(request);

        // then
        assertThat(actualIp).isEqualTo(expectedIp);
    }

    @Test
    @DisplayName("HTTP_CLIENT_IP 헤더에서 IP 추출")
    void getClientIp_from_http_client_ip() {
        // given
        String expectedIp = "192.168.1.104";
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("Proxy-Client-IP")).thenReturn(null);
        when(request.getHeader("WL-Proxy-Client-IP")).thenReturn(null);
        when(request.getHeader("HTTP_CLIENT_IP")).thenReturn(expectedIp);

        // when
        String actualIp = ClientIpResolver.getClientIp(request);

        // then
        assertThat(actualIp).isEqualTo(expectedIp);
    }

    @Test
    @DisplayName("HTTP_X_FORWARDED_FOR 헤더에서 IP 추출")
    void getClientIp_from_http_x_forwarded_for() {
        // given
        String expectedIp = "192.168.1.105";
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("Proxy-Client-IP")).thenReturn(null);
        when(request.getHeader("WL-Proxy-Client-IP")).thenReturn(null);
        when(request.getHeader("HTTP_CLIENT_IP")).thenReturn(null);
        when(request.getHeader("HTTP_X_FORWARDED_FOR")).thenReturn(expectedIp);

        // when
        String actualIp = ClientIpResolver.getClientIp(request);

        // then
        assertThat(actualIp).isEqualTo(expectedIp);
    }

    @Test
    @DisplayName("모든 헤더와 RemoteAddr이 null인 경우 null 반환")
    void getClientIp_all_null_returns_null() {
        // given
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("Proxy-Client-IP")).thenReturn(null);
        when(request.getHeader("WL-Proxy-Client-IP")).thenReturn(null);
        when(request.getHeader("HTTP_CLIENT_IP")).thenReturn(null);
        when(request.getHeader("HTTP_X_FORWARDED_FOR")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn(null);

        // when
        String actualIp = ClientIpResolver.getClientIp(request);

        // then
        assertThat(actualIp).isNull();
    }
}
