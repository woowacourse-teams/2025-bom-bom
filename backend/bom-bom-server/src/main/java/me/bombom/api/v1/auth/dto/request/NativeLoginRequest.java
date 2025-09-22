package me.bombom.api.v1.auth.dto.request;

import jakarta.validation.constraints.NotNull;

public record NativeLoginRequest(
        @NotNull String identityToken,
        @NotNull String authorizationCode
) {}
