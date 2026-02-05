package me.bombom.api.v1.subscribe.dto;

public record PlaywrightResponse(
        Integer statusCode,
        boolean success,
        String message,
        String error,
        String method) {
}
