package me.bombom.api.v1.challenge.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import me.bombom.api.v1.challenge.dto.response.ChallengeResponse;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.web.bind.annotation.GetMapping;

@Tag(name = "Challenge", description = "챌린지 관련 API")
public interface ChallengeControllerApi {

    @Operation(
            summary = "챌린지 전체 목록 조회",
            description = "진행중이거나 예정된 챌린지 전체 목록을 조회합니다. 로그인 시 참여 여부 및 상세 정보가 함께 반환됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "챌린지 목록 조회 성공")
    })
    @GetMapping
    List<ChallengeResponse> getChallenges(@Parameter(hidden = true) @LoginMember Member member);
}
