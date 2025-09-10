package me.bombom.api.v1.auth.dto;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import lombok.Getter;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

@Getter
public class CustomOAuth2User implements OidcUser {

    private final Map<String, Object> attributes;
    private final Member member;
    private final OidcUser oidcUser;

    public CustomOAuth2User(Map<String, Object> attributes, Member member) {
        this.attributes = attributes;
        this.member = member;
        this.oidcUser = null;
    }

    public CustomOAuth2User(OidcUser oidcUser, Member member) {
        this.attributes = oidcUser.getAttributes();
        this.member = member;
        this.oidcUser = oidcUser;
    }

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
        Object name = attributes.get("name");
        if (name != null) {
            return name.toString();
        }
        // Apple의 경우 name이 없을 수 있으므로 sub를 사용
        Object sub = attributes.get("sub");
        return sub != null ? sub.toString() : "Unknown";
    }

    @Override
    public OidcIdToken getIdToken() {
        return oidcUser != null ? oidcUser.getIdToken() : null;
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return oidcUser != null ? oidcUser.getUserInfo() : null;
    }

    @Override
    public Map<String, Object> getClaims() {
        return oidcUser != null ? oidcUser.getClaims() : attributes;
    }
}
