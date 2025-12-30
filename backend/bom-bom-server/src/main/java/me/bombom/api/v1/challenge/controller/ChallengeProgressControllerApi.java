package me.bombom.api.v1.challenge.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import me.bombom.api.v1.challenge.dto.response.MemberChallengeProgressResponse;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "ChallengeProgress", description = "챌린지 API")
public interface ChallengeProgressControllerApi {

    @Operation(summary = "챌린지 내 사용자 진행도 조회", description = "사용자의 챌린지 진행도(투두 완료 현황, 총일수, 완료일수)를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "사용자 진행도 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
            @ApiResponse(responseCode = "404", description = "챌린지/사용자를 찾을 수 없음", content = @Content)
    })
    MemberChallengeProgressResponse getMemberProgress(
            @Parameter(hidden = true) @LoginMember Member member,
            @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long id
    );
}
