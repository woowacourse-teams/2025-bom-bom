package me.bombom.api.v1.reading.dto.response;

public enum StreakShieldStatus {

    AVAILABLE,
    USED,
    ;

    public static StreakShieldStatus from(int remainingCount) {
        if (remainingCount > 0) {
            return AVAILABLE;
        }
        return USED;
    }
}
