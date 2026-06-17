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
 * 마이페이지 랭킹 카드 정보
 */
@Generated("org.openapitools.codegen.languages.SpringCodegen")
public record RankCardResponse(

        @NotNull
        @Schema(description = "랭킹 타입 (streak: 연속 읽기, reading: 다독왕)", requiredMode = REQUIRED)
        String type,

        @Schema(description = "이전달까지의 최신 확정 순위. 랭킹 이력이 없으면 null입니다.", requiredMode = REQUIRED)
        Long currentRank,

        @Valid
        @NotNull
        List<RankHistoryResponse> rankHistory,

        @Schema(description = "카드 표시 값. streak는 현재 연속 읽기 일수, reading은 누적 읽은 아티클 수입니다.", requiredMode = REQUIRED)
        int value
) {

    public static RankCardResponse of(
            String type,
            Long currentRank,
            List<RankHistoryResponse> rankHistory,
            int value
    ) {
        return new RankCardResponse(
                type,
                currentRank,
                rankHistory,
                value
        );
    }
}
