package me.bombom.api.v1.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.bombom.api.v1.challenge.dto.response.MyChallengeSummaryResponse;
import me.bombom.api.v1.challenge.dto.response.OngoingChallengesResponse;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.member.domain.Member;

@Tag(name = "MyPage", description = "마이페이지 관련 API")
public interface MyPageChallengeControllerApi {

    @Operation(
            summary = "나의 챌린지 요약 정보 조회",
            description = "마이페이지 챌린지 탭에 노출할 나의 챌린지 요약 정보를 조회합니다. "
                    + "종료된 챌린지만 집계 대상이며, 수료한 챌린지 수와 수료율·출석률의 상대 순위(상위 %), 메달 비율을 반환합니다. "
                    + "참여한 종료 챌린지가 없으면 모든 값은 0입니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "나의 챌린지 요약 정보 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패 (로그인 필요)", content = @Content)
    })
    MyChallengeSummaryResponse getChallengeSummary(@Parameter(hidden = true) @LoginMember Member member);

    @Operation(
            summary = "참여 중 챌린지 목록 조회",
            description = "로그인한 회원이 현재 참여 중(진행 중)인 챌린지 목록을 팀 순위, 출석률 비교 정보와 함께 조회합니다. "
                    + "진행 중 챌린지가 없으면 빈 배열을 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "참여 중 챌린지 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패 (로그인 필요)", content = @Content)
    })
    OngoingChallengesResponse getOngoingChallenges(@Parameter(hidden = true) @LoginMember Member member);
}
