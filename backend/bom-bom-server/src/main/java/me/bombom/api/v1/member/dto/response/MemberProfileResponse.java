package me.bombom.api.v1.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import me.bombom.api.v1.member.domain.Member;

@Builder
public record MemberProfileResponse(

        @NotNull
        @Schema(type = "integer", format = "int64", description = "회원 ID", required = true)
        Long id,

        @NotNull
        @Schema(type = "string", description = "이메일", required = true)
        String email,

        @NotNull
        @Schema(type = "string", description = "닉네임", required = true)
        String nickname,

        @Schema(type = "string", description = "프로필 이미지 URL")
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
