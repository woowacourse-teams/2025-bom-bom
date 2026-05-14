package me.bombom.api.v1.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Builder;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.enums.Gender;

@Builder
public record MemberInfoResponse(

        @NotNull
        Long id,

        @NotNull
        String email,

        @NotNull
        String nickname,

        String profileImageUrl,

        Gender gender,

        LocalDate birthDate
) {

    public static MemberInfoResponse from(Member member) {
        return MemberInfoResponse.builder()
                .id(member.getId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .profileImageUrl(member.getProfileImageUrl())
                .gender(member.getGender())
                .birthDate(member.getBirthDate())
                .build();
    }
}
