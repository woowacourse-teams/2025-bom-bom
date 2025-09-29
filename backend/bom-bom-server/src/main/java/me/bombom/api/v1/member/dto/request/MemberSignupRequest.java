package me.bombom.api.v1.member.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
import me.bombom.api.v1.member.enums.Gender;
import org.hibernate.validator.constraints.Length;

public record MemberSignupRequest(

        @NotNull
        @Length(min = 2, max = 20)
        @Pattern(regexp = "^(?!.*\\.\\.)[A-Za-z0-9가-힣][A-Za-z0-9가-힣._ ]*[A-Za-z0-9가-힣]$")
        String nickname,

        @Email
        @NotNull
        @Length(min = 15, max = 50)
        @Pattern(regexp = "^[a-zA-Z0-9](?:[a-zA-Z0-9._-]*[a-zA-Z0-9])?@bombom\\.news$")
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
