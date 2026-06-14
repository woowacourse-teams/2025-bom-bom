package me.bombom.api.v1.reading.dto;

public record ReadCountComparison(

        long currentReadCount,
        long previousReadCount
) {
}
