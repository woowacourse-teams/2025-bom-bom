package me.bombom.api.v1.auth.service;

import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;

/**
 * OAuth2 로그인 서비스 인터페이스
 * 각 OAuth 제공자별로 구현체를 만들어 사용
 */
public interface OAuth2LoginService {
    
    /**
     * OAuth2 사용자 정보를 로드합니다
     * @param userRequest OAuth2 사용자 요청
     * @return OAuth2 사용자 정보
     * @throws OAuth2AuthenticationException 인증 예외
     */
    OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException;
    
    /**
     * 지원하는 제공자 타입을 반환합니다
     * @return 제공자 타입 (google, apple 등)
     */
    String getProviderType();
    
    /**
     * 해당 제공자를 지원하는지 확인합니다
     * @param provider 제공자 이름
     * @return 지원 여부
     */
    boolean supports(String provider);
}
