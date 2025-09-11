package me.bombom.api.v1.auth.service;

import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;

public interface OAuth2LoginService {

    OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException;
    String getProviderType();
    boolean supports(String provider);
}
