package me.bombom.api.v1.signupsource.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record SignupRecommendationsResponse(

        @Schema(description = "추천 뉴스레터가 많은 카테고리 순서")
        @NotNull
        List<String> categories,

        @Schema(description = "추천 순서대로 정렬한 뉴스레터")
        @NotNull
        List<SignupRecommendationResponse> newsletters
) {
}
