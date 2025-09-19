package me.bombom.api.v1.auth.dto.request;

public record NativeLoginRequest(
        String identityToken,
        String authorizationCode
) {}
