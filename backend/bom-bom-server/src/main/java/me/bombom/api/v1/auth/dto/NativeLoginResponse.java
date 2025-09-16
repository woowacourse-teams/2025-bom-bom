package me.bombom.api.v1.auth.dto;

public record NativeLoginResponse(
        String status,
        String message,
        String sessionId
) {
}
