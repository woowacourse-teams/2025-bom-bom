package me.bombom.api.v1.auth.dto;

import java.io.Serializable;
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
public class CustomOAuth2User implements OidcUser, Serializable {

    private static final long serialVersionUID = 1L;

    private final Map<String, Object> attributes;
    private final Member member;
    private final OidcIdToken idToken;
    private final OidcUserInfo userInfo;

    public CustomOAuth2User(Map<String, Object> attributes, Member member, OidcIdToken idToken, OidcUserInfo userInfo) {
        this.attributes = attributes;
        this.member = member;
        this.idToken = idToken;
        this.userInfo = userInfo;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public String getName() {
        Object name = attributes.get("name");
        if (name != null) {
            return name.toString();
        }
        Object sub = attributes.get("sub");
        return sub != null ? sub.toString() : "Unknown";
    }

    @Override
    public OidcIdToken getIdToken() {
        return idToken;
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return userInfo;
    }

    @Override
    public Map<String, Object> getClaims() {
        return attributes;
    }
}
