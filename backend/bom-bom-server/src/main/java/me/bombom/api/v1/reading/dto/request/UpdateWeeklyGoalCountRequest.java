package me.bombom.api.v1.reading.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateWeeklyGoalCountRequest(

        @NotNull(message = "회원 아이디는 필수 입력 값입니다.")
        Long memberId,

        @NotNull(message = "주간 목표 개수는 필수 입력 값입니다.")
        Integer weeklyGoalCount
) {
}
