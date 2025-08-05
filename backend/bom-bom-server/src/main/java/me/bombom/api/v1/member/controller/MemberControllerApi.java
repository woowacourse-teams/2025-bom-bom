package me.bombom.api.v1.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.dto.response.MemberProfileResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Member", description = "회원 관련 API")
public interface MemberControllerApi {

    @Operation(
        summary = "내 프로필 조회",
        description = "로그인한 회원의 프로필 정보를 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "프로필 조회 성공"),
        @ApiResponse(responseCode = "401", description = "로그인이 필요합니다")
    })
    MemberProfileResponse getMember(Member member);
} 
