package me.bombom.api.v1.challenge.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import java.util.List;
import me.bombom.api.v1.challenge.dto.response.ChallengeEligibilityResponse;
import me.bombom.api.v1.challenge.dto.response.ChallengeInfoResponse;
import me.bombom.api.v1.challenge.dto.response.ChallengeResponse;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Challenge", description = "챌린지 관련 API")
public interface ChallengeControllerApi {

    @Operation(
            summary = "챌린지 전체 목록 조회",
            description = "진행중이거나 예정된 챌린지 전체 목록을 조회합니다. 로그인 시 참여 여부 및 상세 정보가 함께 반환됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "챌린지 목록 조회 성공")
    })
    List<ChallengeResponse> getChallenges(@Parameter(hidden = true) @LoginMember Member member);

    @Operation(
            summary = "챌린지 상세 조회",
            description = "특정 챌린지 상세 조회를 합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "챌린지 상세 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효하지 않은 ID)", content = @Content),
            @ApiResponse(responseCode = "404", description = "챌린지를 찾을 수 없음", content = @Content)
    })
    ChallengeInfoResponse getChallengeInfo(@PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long id);

    @Operation(
            summary = "챌린지 신청 가능 여부 조회",
            description = "특정 챌린지에 대한 신청 가능 여부를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "신청 가능 여부 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효하지 않은 ID)", content = @Content),
            @ApiResponse(responseCode = "404", description = "챌린지를 찾을 수 없음", content = @Content)
    })
    ChallengeEligibilityResponse checkEligibility(
            @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long id,
            @Parameter(hidden = true) @LoginMember(anonymous = true) Member member
    );

    @Operation(
            summary = "챌린지 신청",
            description = "특정 챌린지에 신청합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "챌린지 신청 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (이미 시작된 챌린지, 중복 신청, 구독하지 않은 뉴스레터 등)", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 실패 (로그인 필요)", content = @Content),
            @ApiResponse(responseCode = "404", description = "챌린지를 찾을 수 없음", content = @Content)
    })
    void applyChallenge(
            @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long id,
            @Parameter(hidden = true) @LoginMember Member member
    );

    @Operation(
            summary = "챌린지 취소",
            description = "신청한 챌린지를 취소합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "챌린지 취소 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (이미 시작된 챌린지)", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 실패 (로그인 필요)", content = @Content),
            @ApiResponse(responseCode = "404", description = "챌린지 또는 신청 내역을 찾을 수 없음", content = @Content)
    })
    void cancelChallenge(
            @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long id,
            @Parameter(hidden = true) @LoginMember Member member
    );
}
