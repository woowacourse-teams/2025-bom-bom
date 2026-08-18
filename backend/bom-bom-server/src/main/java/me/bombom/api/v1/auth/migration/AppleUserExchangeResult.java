package me.bombom.api.v1.auth.migration;

public record AppleUserExchangeResult(
        long targetCount,
        long migratedCount,
        long failedCount
) {
}
