package me.bombom.api.v1.reading.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import me.bombom.api.v1.reading.domain.RecentStreakDays.StreakDay;
import me.bombom.api.v1.reading.domain.StreakDayStatus;

public record StreakDayResponse(

        @NotNull
        @Schema(description = "날짜 (KST)", example = "2026-11-22")
        LocalDate date,

        @NotNull
        @Schema(description = """
                그날의 연속 읽기 상태 (위에서부터 먼저 맞는 값)
                - READ: 그날 읽은 아티클이 있음
                - PENDING: 오늘이고 아직 읽지 않음 (오늘이 주말·공휴일이면 REST)
                - BEFORE_JOIN: 가입일 이전
                - SHIELD: 그날을 놓쳐 보호막이 쓰임
                - REST: 주말·공휴일 (연속 읽기가 끊기지 않는 날)
                - NO_ARTICLE: 평일인데 받은 아티클이 없음 (연속 읽기가 끊기지 않는 날)
                - MISSED: 읽지 않음
                """)
        StreakDayStatus status
) {

    public static StreakDayResponse from(StreakDay streakDay) {
        return new StreakDayResponse(streakDay.date(), streakDay.status());
    }
}
