package me.bombom.api.v1.signupsource.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import me.bombom.api.v1.signupsource.domain.SignupSource;

public record SaveSignupSourceRequest(

        @NotNull
        SignupSource source,

        @Schema(description = "기타(ETC)를 골랐을 때 직접 입력한 경로")
        @Size(max = 50)
        String detail
) {
}
