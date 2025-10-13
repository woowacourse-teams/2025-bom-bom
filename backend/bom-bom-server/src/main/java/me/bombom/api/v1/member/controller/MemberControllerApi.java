package me.bombom.api.v1.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.dto.request.MemberProfileUpdateRequest;
import me.bombom.api.v1.member.dto.response.MemberProfileResponse;

@Tag(name = "Member", description = "회원 관련 API")
@ApiResponses({
    @ApiResponse(responseCode = "401", description = "인증 실패 (로그인 필요)", content = @Content)
})
public interface MemberControllerApi {

    @Operation(
        summary = "내 프로필 조회",
        description = "로그인한 회원의 프로필 정보를 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "프로필 조회 성공")
    })
    MemberProfileResponse getMember(Member member);

    @Operation(
        summary = "내 프로필 수정",
        description = "로그인한 회원의 프로필 정보(닉네임, 프로필이미지, 생년월일, 성별)를 수정합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "프로필 수정 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터")
    })
    MemberProfileResponse updateMember(Member member, MemberProfileUpdateRequest request);
} 
