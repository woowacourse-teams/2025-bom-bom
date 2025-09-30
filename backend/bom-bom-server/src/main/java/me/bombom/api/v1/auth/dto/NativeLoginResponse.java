package me.bombom.api.v1.auth.dto;

public record NativeLoginResponse(
        boolean isRegistered,
        String email,
        String password
) {
}
