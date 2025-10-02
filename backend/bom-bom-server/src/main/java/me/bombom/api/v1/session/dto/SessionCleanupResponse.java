package me.bombom.api.v1.session.dto;

import lombok.Builder;

/**
 * 세션 정리 응답 DTO
 */
@Builder
public record SessionCleanupResponse(int deletedCount, String message) {
}
