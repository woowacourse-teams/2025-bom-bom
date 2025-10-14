package me.bombom.api.v1.auth.extractor;

import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.auth.dto.CustomOAuth2User;
import me.bombom.api.v1.auth.dto.OAuth2LoginInfo;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.stereotype.Component;

/**
 * Google OAuth2 사용자 정보 추출기
 */
@Slf4j
@Component
public class GoogleUserInfoExtractor implements OAuth2UserInfoExtractor {

    @Override
    public OAuth2LoginInfo extractLoginInfo(CustomOAuth2User oauth2User, Member member) {
        String email = extractEmail(oauth2User);
        String name = extractName(oauth2User);

        log.info("Google 로그인 정보 추출 - 이메일: {}, 이름: {}", email, name);
        return OAuth2LoginInfo.of(member, email, name);
    }

    private String extractEmail(CustomOAuth2User oauth2User) {
        return (String) oauth2User.getAttributes().get("email");
    }

    private String extractName(CustomOAuth2User oauth2User) {
        return (String) oauth2User.getAttributes().get("name");
    }

    @Override
    public boolean supports(String providerName) {
        return "google".equals(providerName);
    }
}
