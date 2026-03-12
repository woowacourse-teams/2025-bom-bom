package me.bombom.api.v1.challenge.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import me.bombom.api.v1.challenge.dto.response.CertificationInfoResponse;
import me.bombom.api.v1.challenge.dto.response.ChallengeStreakResponse;
import me.bombom.api.v1.challenge.dto.response.MemberChallengeProgressResponse;
import me.bombom.api.v1.challenge.dto.response.TeamChallengeProgressResponse;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "ChallengeProgress", description = "챌린지 진행도 관련 API")
public interface ChallengeProgressControllerApi {

    @Operation(summary = "챌린지 내 사용자 진행도 조회", description = "사용자의 챌린지 진행도(투두 완료 현황, 총일수, 완료일수)를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "사용자 진행도 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
            @ApiResponse(responseCode = "404", description = "챌린지/사용자를 찾을 수 없음", content = @Content),
            @ApiResponse(responseCode = "500", description = "챌린지 참가자에 대한 데이터 정합성 불일치", content = @Content),
    })
    MemberChallengeProgressResponse getMemberProgress(
            @Parameter(hidden = true) @LoginMember Member member,
            @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long id
    );

    @Operation(summary = "챌린지 스트릭 조회", description = "사용자의 현재 스트릭 값과 스트릭을 구성하는 날짜 목록(날짜, 요일, 쉴드 적용 여부)을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "스트릭 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
            @ApiResponse(responseCode = "403", description = "챌린지에 참가하지 않음", content = @Content),
            @ApiResponse(responseCode = "404", description = "챌린지/참가자를 찾을 수 없음", content = @Content),
    })
    ChallengeStreakResponse getMemberStreak(
            @Parameter(hidden = true) @LoginMember Member member,
            @Parameter(description = "챌린지 ID") @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long id,
            @Parameter(description = "조회할 스트릭 날짜 수 (기본값: 5)") @RequestParam(defaultValue = "5") @Positive(message = "limit는 1 이상의 값이어야 합니다.") int limit
    );

    @Operation(summary = "특정 팀 진행도 조회", description = "특정 팀의 진행도 및 팀원들의 진행 상황을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "팀 진행도 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 실패 (로그인 필요)", content = @Content),
            @ApiResponse(responseCode = "403", description = "권한 없음 (챌린지에 참가하지 않음)", content = @Content),
            @ApiResponse(responseCode = "404", description = "챌린지/팀을 찾을 수 없음", content = @Content)
    })
    TeamChallengeProgressResponse getTeamProgressByTeamId(
            @Parameter(hidden = true) @LoginMember Member member,
            @Parameter(description = "챌린지 ID") @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long id,
            @Parameter(description = "팀 ID") @PathVariable @Positive(message = "teamId는 1 이상의 값이어야 합니다.") Long teamId
    );

    @Operation(summary = "수료증 정보 조회", description = "사용자의 챌린지 수료증 정보(닉네임, 챌린지명, 기수, 기간, 메달 등급)를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수료증 정보 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청, 챌린지/참가자를 찾을 수 없음, 또는 진행 중인 챌린지, 탈락한 참가자", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 실패 (로그인 필요)", content = @Content)
    })
    CertificationInfoResponse getCertificationInfo(
            @Parameter(hidden = true) @LoginMember Long memberId,
            @Parameter(description = "챌린지 ID") @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long id
    );
}
