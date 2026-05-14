package me.bombom.api.v1.auth.dto;

import jakarta.validation.constraints.NotNull;

public record NativeLoginResponse(

        @NotNull
        boolean isRegistered,
        String email,
        String nickname
) {
}
