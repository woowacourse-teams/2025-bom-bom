package me.bombom.api.v1.nativenewsletter.maeilmail.dto;

public record IssueChunkResult(
        boolean hasTracks,
        Long lastTrackId
) {

    public static IssueChunkResult empty() {
        return new IssueChunkResult(false, null);
    }

    public static IssueChunkResult of(Long lastTrackId) {
        return new IssueChunkResult(true, lastTrackId);
    }
}
