package me.bombom.api.v1.member.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
import me.bombom.api.v1.member.enums.Gender;
import me.bombom.api.v1.member.util.UserInfoValidator;
import org.hibernate.validator.constraints.Length;

public record MemberInfoUpdateRequest(

    @Pattern(regexp = UserInfoValidator.NICKNAME_REGEX_PATTERN)
    @Length(min = UserInfoValidator.NICKNAME_MIN_LENGTH, max = UserInfoValidator.NICKNAME_MAX_LENGTH)
    String nickname,

    String profileImageUrl,

    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate birthDate,

    Gender gender
) {

}
