package me.bombom.api.v1.challenge.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import me.bombom.api.v1.challenge.dto.GetChallengeInfoResponse;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Challenge", description = "챌린지 관련 API")
public interface ChallengeControllerApi {

    @Operation(
            summary = "챌린지 상세 조회",
            description = "특정 챌린지 상세 조회를 합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "챌린지 상세 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효하지 않은 ID)", content = @Content),
            @ApiResponse(responseCode = "404", description = "뉴스레터를 찾을 수 없음", content = @Content)
    })
    GetChallengeInfoResponse getChallengeInfo(@PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long id);
}
