package me.bombom.api.v1.auth.dto;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

@Getter
@RequiredArgsConstructor
public class CustomOAuth2User implements OAuth2User {

    private final Map<String, Object> attributes;
    private final Member member;

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // TODO: 권한 처리 필요시 구현
        return Collections.emptyList();
    }

    @Override
    public String getName() {
        return attributes.get("name").toString();
    }
}
