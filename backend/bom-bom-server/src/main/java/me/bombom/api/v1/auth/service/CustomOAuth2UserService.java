package me.bombom.api.v1.auth.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.common.exception.UnauthorizedException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 통합 OAuth2 사용자 서비스
 * 각 OAuth 제공자별 서비스로 요청을 라우팅
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    // 현재는 구글만 존재하지만, 추후에 다른 OAuth2 기반 로그인이 생길 경우 고려
    private final List<OAuth2LoginService> loginServices;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        String provider = userRequest.getClientRegistration().getRegistrationId();
        log.info("OAuth2 로그인 요청 - provider: {}", provider);
        OAuth2LoginService loginService = loginServices.stream()
                .filter(service -> service.supports(provider))
                .findFirst()
                .orElseThrow(() -> {
                    log.error("지원하지 않는 OAuth 제공자 - provider: {}", provider);
                    return new UnauthorizedException(ErrorDetail.UNSUPPORTED_OAUTH2_PROVIDER)
                            .addContext("provider", provider);
                });

        return loginService.loadUser(userRequest);
    }
}
