package me.bombom.api.v1.auth.migration;

public record AppleUserMigrationResult(
        long targetCount,
        long migratedCount
) {
}
