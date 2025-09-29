package me.bombom.api.v1.member.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import me.bombom.api.v1.member.enums.Gender;

public record MemberSignupRequest(

        @NotNull
        String nickname,

        @NotNull
        String email,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate birthDate,

        Gender gender
) {
        public MemberSignupRequest {
                if (gender == null) {
                        gender = Gender.NONE;
                }
        }
}
