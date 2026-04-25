package me.bombom.api.v1.nativenewsletter.maeilmail.dto;

import java.util.ArrayList;
import java.util.List;

public record PendingIssueEntry(
        Long memberId,
        Long topicId,
        Long newsletterId,
        Long contentId,
        List<Long> trackIds
) {

    public PendingIssueEntry {
        trackIds = List.copyOf(trackIds);
    }

    public PendingIssueEntry withTrackId(Long trackId) {
        List<Long> nextTrackIds = new ArrayList<>(trackIds);
        nextTrackIds.add(trackId);
        return new PendingIssueEntry(memberId, topicId, newsletterId, contentId, nextTrackIds);
    }
}
