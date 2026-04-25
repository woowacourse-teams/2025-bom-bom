package me.bombom.api.v1.nativenewsletter.maeilmail.dto;

import java.util.List;

public record PreparedIssueEntries(
        List<IssueEntry> entries,
        List<Long> previouslyIssuedTrackIds
) {

    public boolean isEmpty() {
        return entries.isEmpty() && previouslyIssuedTrackIds.isEmpty();
    }
}
