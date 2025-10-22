package me.bombom.api.v1.auth.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.auth.dto.CustomOAuth2User;
import me.bombom.api.v1.auth.dto.OAuth2LoginInfo;
import me.bombom.api.v1.auth.extractor.AppleUserInfoExtractor;
import me.bombom.api.v1.auth.extractor.GoogleUserInfoExtractor;
import me.bombom.api.v1.auth.extractor.OAuth2UserInfoExtractor;
import me.bombom.api.v1.auth.util.UniqueUserInfoGenerator;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.common.exception.UnauthorizedException;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.service.MemberService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final List<OAuth2UserInfoExtractor> extractors = List.of(
            new AppleUserInfoExtractor(),
            new GoogleUserInfoExtractor()
    );

    private static final String LOCAL_ENV = "local";
    private static final String SIGNUP_PATH = "/signup";
    private static final String HOME_PATH = "/";
    private static final String EMAIL_PARAM = "email";
    private static final String NAME_PARAM = "name";

    @Value("${frontend.base-url}")
    private String frontendBaseUrl;

    @Value("${frontend.local-url}")
    private String frontendLocalUrl;

    private final MemberService memberService;
    private final UniqueUserInfoGenerator uniqueUserInfoGenerator;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        HttpSession session = request.getSession();
        Boolean pendingWithdraw = (Boolean) session.getAttribute("pendingWithdraw");
        Long withdrawMemberId = (Long) session.getAttribute("withdrawMemberId");

        // === 탈퇴 처리 ===
        if (pendingWithdraw != null && pendingWithdraw && withdrawMemberId != null) {
            handleWithdrawAfterReAuth(withdrawMemberId, request, response);
        } else {
            // === 로그인 처리 ===
            OAuth2LoginInfo oauth2Info = extractOAuth2LoginInfo(authentication);
            Member member = oauth2Info.getMember();
            String redirectUrl = buildRedirectUrl(request, member, oauth2Info);
            response.sendRedirect(redirectUrl);
        }
    }

    private void handleWithdrawAfterReAuth(Long withdrawMemberId, HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            memberService.withdraw(withdrawMemberId);
        } catch (Exception e) {
            log.error("재인증 후 탈퇴 처리 중 예외 발생 - memberId: {}", withdrawMemberId, e);
        } finally {
            HttpSession session = request.getSession();
            session.invalidate();
            SecurityContextHolder.clearContext();
            response.sendRedirect(getBaseUrlByEnv(request));
        }
    }

    private OAuth2LoginInfo extractOAuth2LoginInfo(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        if (!(principal instanceof CustomOAuth2User oauth2User)) {
            log.warn("알 수 없는 OAuth2User 타입: {}", principal.getClass().getSimpleName());
            return new OAuth2LoginInfo(null, null, null);  // 회원가입 페이지로 리다이렉트 유도
        }

        Member member = oauth2User.getMember();
        OAuth2UserInfoExtractor extractor = getExtractor(authentication);
        return extractor.extractLoginInfo(oauth2User, member);
    }

    private OAuth2UserInfoExtractor getExtractor(Authentication authentication) {
        String provider = resolveProvider(authentication);

        return extractors.stream()
                .filter(extractor -> extractor.supports(provider))
                .findFirst()
                .orElseThrow(() -> {
                    log.error("지원하지 않는 OAuth2 제공자입니다: {}", provider);
                    return new UnauthorizedException(ErrorDetail.UNSUPPORTED_OAUTH2_PROVIDER)
                            .addContext(ErrorContextKeys.OPERATION, "getExtractor")
                            .addContext("provider", provider);
                });
    }

    private String resolveProvider(Authentication authentication) {
        if (authentication instanceof OAuth2AuthenticationToken token) {
            return token.getAuthorizedClientRegistrationId();
        }
        throw new UnauthorizedException(ErrorDetail.UNSUPPORTED_OAUTH2_PROVIDER)
                .addContext(ErrorContextKeys.OPERATION, "resolveProvider")
                .addContext("provider", "unknown");
    }

    private String buildRedirectUrl(HttpServletRequest request, Member member, OAuth2LoginInfo oauth2Info) {
        String baseUrl = getBaseUrlByEnv(request);
        if (member != null) {
            return baseUrl + HOME_PATH;
        }
        if (oauth2Info.getEmail() != null || oauth2Info.getName() != null) {
            String queryParams = buildQueryParams(oauth2Info);
            return baseUrl + SIGNUP_PATH + queryParams;
        }
        return baseUrl + SIGNUP_PATH;
    }

    private String buildQueryParams(OAuth2LoginInfo oauth2Info) {
        StringBuilder params = new StringBuilder("?");
        boolean hasParam = false;
        try {
            if (oauth2Info.getEmail() != null) {
                String uniqueEmailLocalPart = uniqueUserInfoGenerator.getUniqueEmailLocalPart(oauth2Info.getEmail());
                params.append(EMAIL_PARAM)
                        .append("=")
                        .append(URLEncoder.encode(uniqueEmailLocalPart, StandardCharsets.UTF_8));
                hasParam = true;
            }
            if (hasParam) params.append("&");
            String uniqueNickname = uniqueUserInfoGenerator.getUniqueNickname(oauth2Info.getName());
            params.append(NAME_PARAM)
                    .append("=")
                    .append(URLEncoder.encode(uniqueNickname, StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.warn("쿼리 파라미터 인코딩 실패", e);
        }
        return params.toString();
    }

    private String getBaseUrlByEnv(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        String env = session != null ? (String) session.getAttribute("env") : null;
        if (LOCAL_ENV.equals(env)) {
            return frontendLocalUrl;
        }
        return frontendBaseUrl;
    }
}
