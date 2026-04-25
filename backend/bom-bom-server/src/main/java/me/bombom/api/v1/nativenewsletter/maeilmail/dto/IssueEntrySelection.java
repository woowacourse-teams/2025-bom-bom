package me.bombom.api.v1.nativenewsletter.maeilmail.dto;

import java.util.List;

public record IssueEntrySelection(
        List<PendingIssueEntry> pendingEntries,
        List<Long> alreadyIssuedTrackIds
) {
}
