package me.bombom.api.v1.pet.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.pet.dto.PetResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Pet", description = "펫 관련 API")
@RequestMapping("/api/v1/members/me/pet")
public interface PetControllerApi {

    @Operation(summary = "내 펫 정보 조회", description = "현재 로그인한 사용자의 펫 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "펫 정보 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    PetResponse getPet(Member member);

    @Operation(summary = "출석 점수 부여", description = "오늘의 출석 점수를 펫에게 부여합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "출석 점수 부여 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    void addAttendanceScore(Member member);
}
