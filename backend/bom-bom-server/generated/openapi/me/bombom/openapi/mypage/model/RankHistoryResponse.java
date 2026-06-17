package me.bombom.openapi.mypage.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import jakarta.annotation.Generated;

/**
 * 마이페이지 랭킹 히스토리 정보
 */
@Generated("org.openapitools.codegen.languages.SpringCodegen")
public record RankHistoryResponse(

        @NotNull
        @Schema(description = "랭킹 기준 월 (yyyy-MM)", requiredMode = REQUIRED)
        String month,

        @NotNull
        @Schema(description = "화면 표시용 월 라벨", requiredMode = REQUIRED)
        String label,

        @Schema(description = "해당 월의 순위", requiredMode = REQUIRED)
        long rank
) {

    public static RankHistoryResponse of(
            String month,
            String label,
            long rank
    ) {
        return new RankHistoryResponse(
                month,
                label,
                rank
        );
    }
}
