package news.bombomemail.nativenewsletter.maeilmail.dto;

public record IssueChunkResult(
        boolean hasTracks,
        Long lastTrackId,
        int trackCount,
        int issuedArticleCount,
        int previouslyIssuedTrackCount
) {

    public static IssueChunkResult empty() {
        return new IssueChunkResult(false, null, 0, 0, 0);
    }

    public static IssueChunkResult of(
            Long lastTrackId,
            int trackCount,
            int issuedArticleCount,
            int previouslyIssuedTrackCount
    ) {
        return new IssueChunkResult(true, lastTrackId, trackCount, issuedArticleCount, previouslyIssuedTrackCount);
    }
}
