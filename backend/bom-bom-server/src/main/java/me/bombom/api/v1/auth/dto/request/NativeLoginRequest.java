package me.bombom.api.v1.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record NativeLoginRequest(
        @NotBlank String identityToken,
        String authorizationCode
) {}
