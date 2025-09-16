package me.bombom.api.v1.auth.service;

import java.util.Optional;
import me.bombom.api.v1.auth.dto.request.NativeLoginRequest;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;

public interface OAuth2LoginService {

    OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException;
    String getProviderType();
    boolean supports(String provider);

    // 네이티브: idToken + authorizationCode 기반 로그인(선택 구현)
    default Optional<Member> loginWithNative(NativeLoginRequest request) {
        throw new UnsupportedOperationException("Native login is not supported by this provider");
    }
}
