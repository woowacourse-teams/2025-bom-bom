package me.bombom.api.v1.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import me.bombom.api.v1.member.domain.Member;

@Builder
public record MemberProfileResponse(

        @NotNull
        Long id,

        @NotNull
        String email,

        @NotNull
        String nickname,

        String profileImageUrl
) {

    public static MemberProfileResponse from(Member member) {
        return MemberProfileResponse.builder()
                .id(member.getId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .profileImageUrl(member.getProfileImageUrl())
                .build();
    }
}
