package me.bombom.api.v1.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.bombom.api.v1.member.domain.Member;

@Getter
@AllArgsConstructor
public class OAuth2LoginInfo {
    private final Member member;
    private final String email;
    private final String name;
}
