package news.bombom.captcha.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 클라이언트 IP 주소 추출 유틸리티
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ClientIpResolver {

    private static final String[] IP_HEADER_CANDIDATES = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR"
    };

    private static final String UNKNOWN = "unknown";

    public static String getClientIp(HttpServletRequest request) {
        // 헤더에서 IP 찾기
        for (String header : IP_HEADER_CANDIDATES) {
            String ip = request.getHeader(header);
            if (isValidIp(ip)) {
                return extractFirstIp(ip);
            }
        }

        // 헤더에 없으면 RemoteAddr 사용
        return extractFirstIp(request.getRemoteAddr());
    }

    /**
     * 여러 IP가 콤마로 구분된 경우 첫 번째 IP만 추출 (X-Forwarded-For: "client-ip, proxy1-ip, proxy2-ip")
     */
    private static String extractFirstIp(String ip) {
        if (ip == null || !ip.contains(",")) {
            return ip;
        }
        return ip.split(",")[0].trim();
    }

    private static boolean isValidIp(String ip) {
        return ip != null && !ip.isEmpty() && !UNKNOWN.equalsIgnoreCase(ip);
    }
}
