package me.bombom.api.v1.reading.domain;

public record LowestRankWithDifference(
        long rank,
        long difference
) {

    public static LowestRankWithDifference of(long lowestRank, long difference) {
        return new LowestRankWithDifference(lowestRank, difference);
    }
}
