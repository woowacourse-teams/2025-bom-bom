package me.bombom.api.v1.session.dto;

import lombok.Builder;

/**
 * 세션 통계 응답 DTO
 */
@Builder
public record SessionStatisticsResponse(
        int totalSessions,
        int activeSessions,
        int expiredSessions
) {

    public static SessionStatisticsResponse of(int totalSessions, int expiredSessions, int activeSessions) {
        return SessionStatisticsResponse.builder()
                .totalSessions(totalSessions)
                .activeSessions(activeSessions)
                .expiredSessions(expiredSessions)
                .build();
    }
}
