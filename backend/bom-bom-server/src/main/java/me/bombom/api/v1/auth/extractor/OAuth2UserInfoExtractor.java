package me.bombom.api.v1.auth.extractor;

import me.bombom.api.v1.auth.dto.CustomOAuth2User;
import me.bombom.api.v1.auth.dto.OAuth2LoginInfo;
import me.bombom.api.v1.member.domain.Member;

/**
 * OAuth2 사용자 정보를 추출하는 인터페이스
 */
public interface OAuth2UserInfoExtractor {

    OAuth2LoginInfo extractLoginInfo(CustomOAuth2User oauth2User, Member member);
    boolean supports(String providerName);
}
