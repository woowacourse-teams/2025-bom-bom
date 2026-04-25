package me.bombom.api.v1.nativenewsletter.maeilmail.dto;

import java.util.ArrayList;
import java.util.List;

public record IssueEntrySelectionResult(
        List<PendingEntry> pendingEntries,
        List<Long> previouslyIssuedTrackIds
) {

    public record PendingEntry(
            Long memberId,
            Long topicId,
            Long newsletterId,
            Long contentId,
            List<Long> trackIds
    ) {

        public PendingEntry {
            trackIds = List.copyOf(trackIds);
        }

        public PendingEntry withTrackId(Long trackId) {
            List<Long> nextTrackIds = new ArrayList<>(trackIds);
            nextTrackIds.add(trackId);
            return new PendingEntry(memberId, topicId, newsletterId, contentId, nextTrackIds);
        }
    }
}
