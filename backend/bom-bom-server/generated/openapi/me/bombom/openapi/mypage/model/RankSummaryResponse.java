package me.bombom.openapi.mypage.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import jakarta.annotation.Generated;

/**
 * 마이페이지 랭킹 요약 응답
 */
@Generated("org.openapitools.codegen.languages.SpringCodegen")
public record RankSummaryResponse(

        @Valid
        @NotNull
        List<RankCardResponse> cards
) {

    public static RankSummaryResponse from(
            List<RankCardResponse> cards
    ) {
        return new RankSummaryResponse(
                cards
        );
    }
}
