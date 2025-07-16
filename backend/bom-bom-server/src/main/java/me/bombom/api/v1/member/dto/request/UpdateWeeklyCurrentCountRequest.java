package me.bombom.api.v1.member.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateWeeklyCurrentCountRequest(
        @NotNull(message = "회원 아이디는 필수 입력 값입니다.") Long memberId
) {
}
