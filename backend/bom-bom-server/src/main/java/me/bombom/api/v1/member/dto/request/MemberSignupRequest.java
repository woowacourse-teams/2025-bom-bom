package me.bombom.api.v1.member.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import me.bombom.api.v1.member.enums.Gender;

public record MemberSignupRequest(
        @NotNull String nickname,
        @NotNull @Email String email,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate birthDate,
        @NotNull Gender gender
) {
}
