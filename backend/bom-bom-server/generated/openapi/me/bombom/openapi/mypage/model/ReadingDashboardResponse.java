package me.bombom.openapi.mypage.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import jakarta.annotation.Generated;

/**
 * 월간 읽기 대시보드 응답
 */
@Generated("org.openapitools.codegen.languages.SpringCodegen")
public record ReadingDashboardResponse(

        @Schema(description = "이번 달 읽은 아티클 수", requiredMode = REQUIRED)
        long readArticleCount,

        @Schema(description = "지난 달 대비 읽은 아티클 수 증감률 (%)", requiredMode = REQUIRED)
        Double readArticleChangeRate,

        ChangeDirection readArticleChangeDirection,

        @Schema(description = "북마크 개수", requiredMode = REQUIRED)
        long bookmarkCount,

        @Valid
        @NotNull
        @Schema(description = "자주 읽은 뉴스레터 TOP", requiredMode = REQUIRED)
        List<FrequentReadNewsletterResponse> frequentReadNewsletters
) {

    public static ReadingDashboardResponse of(
            long readArticleCount,
            Double readArticleChangeRate,
            ChangeDirection readArticleChangeDirection,
            long bookmarkCount,
            List<FrequentReadNewsletterResponse> frequentReadNewsletters
    ) {
        return new ReadingDashboardResponse(
                readArticleCount,
                readArticleChangeRate,
                readArticleChangeDirection,
                bookmarkCount,
                frequentReadNewsletters
        );
    }
}
