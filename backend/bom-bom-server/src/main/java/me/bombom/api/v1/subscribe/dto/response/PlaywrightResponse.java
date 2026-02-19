package me.bombom.api.v1.subscribe.dto.response;

public record PlaywrightResponse(

        Integer statusCode,
        boolean success,
        String message,
        String method
) {
}
