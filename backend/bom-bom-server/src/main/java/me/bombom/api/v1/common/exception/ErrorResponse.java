package me.bombom.api.v1.common.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public record ErrorResponse(
        HttpStatus status,
        String code,
        String message,
        String reason
) {

    public static ErrorResponse from(ErrorDetail errorDetail){
        return new ErrorResponse(errorDetail.getStatus(), errorDetail.getCode(), errorDetail.getMessage(), null);
    }

    public static ErrorResponse from(ErrorDetail errorDetail, Map<String, Object> context) {
        String reason = context != null ? String.valueOf(context.getOrDefault(ErrorContextKeys.REASON.getKey(), null)) : null;
        if ("null".equals(reason)) {
            reason = null;
        }
        return new ErrorResponse(errorDetail.getStatus(), errorDetail.getCode(), errorDetail.getMessage(), reason);
    }
}
