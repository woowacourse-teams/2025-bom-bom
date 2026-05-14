package me.bombom.api.v1.auth.extractor;

import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.auth.dto.CustomOAuth2User;
import me.bombom.api.v1.auth.dto.OAuth2LoginInfo;
import me.bombom.api.v1.auth.enums.OAuth2Provider;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.stereotype.Component;

/**
 * Apple OAuth2 사용자 정보 추출기
 */
@Slf4j
@Component
public class AppleUserInfoExtractor implements OAuth2UserInfoExtractor {

    private static final String NAME = "name";
    private static final String EMAIL = "email";

    @Override
    public OAuth2LoginInfo extractLoginInfo(CustomOAuth2User oauth2User, Member member) {
        String email = extractEmail(oauth2User);
        String name = extractName(oauth2User);

        log.info("Apple 로그인 정보 추출 - 이메일: {}, 이름: {}", email, name);

        return new OAuth2LoginInfo(member, email, name);
    }

    @Override
    public boolean supports(String providerName) {
        return OAuth2Provider.APPLE.isEqualProvider(providerName);
    }

    private String extractEmail(CustomOAuth2User oauth2User) {
        return (String) oauth2User.getAttributes().get(EMAIL);
    }

    private String extractName(CustomOAuth2User oauth2User) {
        Object nameObj = oauth2User.getAttributes().get(NAME);
        if (nameObj instanceof String) {
            return (String) nameObj;
        }
        return null;
    }
}
