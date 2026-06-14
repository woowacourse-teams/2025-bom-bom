package me.bombom.openapi.mypage.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import jakarta.annotation.Generated;

/**
 * 자주 읽은 뉴스레터 정보
 */
@Generated("org.openapitools.codegen.languages.SpringCodegen")
public record FrequentReadNewsletterResponse(

        @Schema(description = "순위 (1-base)", requiredMode = REQUIRED)
        int rank,

        @NotNull
        @Schema(description = "뉴스레터 ID", requiredMode = REQUIRED)
        Long newsletterId,

        @NotNull
        @Schema(description = "뉴스레터명", requiredMode = REQUIRED)
        String name,

        @Schema(description = "이번 달 읽은 아티클 수", requiredMode = REQUIRED)
        long readCount
) {

    public static FrequentReadNewsletterResponse of(
            int rank,
            Long newsletterId,
            String name,
            long readCount
    ) {
        return new FrequentReadNewsletterResponse(
                rank,
                newsletterId,
                name,
                readCount
        );
    }
}
