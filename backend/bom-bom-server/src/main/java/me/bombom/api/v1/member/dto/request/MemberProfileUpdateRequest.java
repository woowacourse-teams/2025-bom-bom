package me.bombom.api.v1.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import me.bombom.api.v1.member.enums.Gender;
import org.springframework.lang.Nullable;

public record MemberProfileUpdateRequest(
    @Schema(description = "변경할 닉네임", example = "봄봄")
    @Size(min = 2, max = 20)
    @Nullable
    String nickname,

    @Schema(description = "변경할 프로필 이미지 URL", example = "https://image.bombom.me/profile/1")
    @Nullable
    String profileImageUrl,

    @Schema(description = "변경할 생년월일", example = "2000-01-01")
    @Past
    @Nullable
    LocalDate birthDate,

    @Schema(description = "변경할 성별", example = "MALE")
    @Nullable
    Gender gender
) {

}
