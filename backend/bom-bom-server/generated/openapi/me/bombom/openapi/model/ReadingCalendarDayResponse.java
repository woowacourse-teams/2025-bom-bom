package me.bombom.openapi.model;

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
 * 읽기 캘린더의 하루 정보
 */
@Generated("org.openapitools.codegen.languages.SpringCodegen")
public record ReadingCalendarDayResponse(

        @NotNull
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        @Schema(description = "날짜 (yyyy-MM-dd)", requiredMode = REQUIRED)
        LocalDate date,

        @Schema(description = "해당 날짜에 아티클을 읽었는지 여부", requiredMode = REQUIRED)
        boolean read,

        @Schema(description = "해당 날짜에 읽은 아티클 수", requiredMode = REQUIRED)
        long readCount
) {

    public static ReadingCalendarDayResponse of(
            LocalDate date,
            boolean read,
            long readCount
    ) {
        return new ReadingCalendarDayResponse(
                date,
                read,
                readCount
        );
    }
}
