package me.bombom.api.v1.member.dto.request;

import java.time.LocalDateTime;
import lombok.Builder;
import me.bombom.api.v1.member.enums.Gender;

@Builder
public record MemberSignupRequest(
        String nickname,
        String email,
        LocalDateTime birthDate,
        Gender gender
) {
}
