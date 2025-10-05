package me.bombom.api.v1.auth.dto;

import me.bombom.api.v1.member.domain.Member;

public record OAuth2LoginInfo(
        Member member,
        String email,
        String nickname
) {

    public static OAuth2LoginInfo of(Member member, String email, String name) {
        return new OAuth2LoginInfo(member, email, name);
    }
}
