package me.bombom.api.v1.member.dto.request;

import java.time.LocalDateTime;
import me.bombom.api.v1.member.enums.Gender;

public record MemberSignupRequest(
        String nickname,
        LocalDateTime birthDate,
        Gender gender
) {
}
