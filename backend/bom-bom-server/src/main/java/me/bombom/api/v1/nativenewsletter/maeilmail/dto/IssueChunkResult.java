package me.bombom.api.v1.nativenewsletter.maeilmail.dto;

import java.util.Set;
import me.bombom.api.v1.nativenewsletter.maeilmail.repository.MemberTopicKey;

public record IssueChunkResult(
        boolean hasTracks,
        Long lastTrackId,
        Set<MemberTopicKey> issuedMemberTopicKeys
) {

    public static IssueChunkResult empty() {
        return new IssueChunkResult(false, null, Set.of());
    }

    public static IssueChunkResult of(Long lastTrackId, Set<MemberTopicKey> issuedMemberTopicKeys) {
        return new IssueChunkResult(true, lastTrackId, issuedMemberTopicKeys);
    }
}
