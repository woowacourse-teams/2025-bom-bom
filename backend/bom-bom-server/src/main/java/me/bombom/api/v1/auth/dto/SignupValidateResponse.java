package me.bombom.api.v1.auth.dto;

import jakarta.validation.constraints.NotNull;
import me.bombom.api.v1.auth.enums.SignupValidateField;
import me.bombom.api.v1.auth.enums.SignupValidateStatus;

public record SignupValidateResponse(

        @NotNull
        SignupValidateField field,

        @NotNull
        SignupValidateStatus status
) {

    public static SignupValidateResponse of(SignupValidateField field, SignupValidateStatus status) {
        return new SignupValidateResponse(field, status);
    }
}
