package me.bombom.openapi.mypage.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import jakarta.annotation.Generated;

/**
 * 회원 가입일 기준 경과일 정보
 */
@Generated("org.openapitools.codegen.languages.SpringCodegen")
public record MemberJoinDaysResponse(

        @Schema(description = "가입일 이래로 지난 일수", requiredMode = REQUIRED)
        int daysSinceJoined,

        @NotNull
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        @Schema(description = "가입일 (yyyy-MM-dd)", requiredMode = REQUIRED)
        LocalDate joinedAt
) {

    public static MemberJoinDaysResponse of(
            int daysSinceJoined,
            LocalDate joinedAt
    ) {
        return new MemberJoinDaysResponse(
                daysSinceJoined,
                joinedAt
        );
    }
}
