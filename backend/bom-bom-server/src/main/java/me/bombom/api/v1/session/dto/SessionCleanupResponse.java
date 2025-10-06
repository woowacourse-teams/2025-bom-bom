package me.bombom.api.v1.session.dto;

public record SessionCleanupResponse(int deletedCount, String message) {

    public static SessionCleanupResponse of(int deletedCount, String message) {
        return new SessionCleanupResponse(deletedCount, message);
    }
}
