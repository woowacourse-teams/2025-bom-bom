package me.bombom.api.v1.common.exception;

import org.springframework.http.HttpStatus;

public record ErrorResponse(
        HttpStatus status,
        String code,
        String message
) {

    public static ErrorResponse from(ErrorDetail errorDetail){
        return new ErrorResponse(errorDetail.getStatus(), errorDetail.getCode(), errorDetail.getMessage());
    }
}
