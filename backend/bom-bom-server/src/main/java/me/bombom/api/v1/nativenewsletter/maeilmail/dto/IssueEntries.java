package me.bombom.api.v1.nativenewsletter.maeilmail.dto;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import me.bombom.api.v1.nativenewsletter.maeilmail.repository.MemberTopicKey;

public record IssueEntries(
        List<IssueEntry> entries,
        List<Long> alreadyIssuedTrackIds
) {

    public boolean isEmpty() {
        return entries.isEmpty() && alreadyIssuedTrackIds.isEmpty();
    }

    public Set<MemberTopicKey> memberTopicKeys() {
        return entries.stream()
                .map(entry -> new MemberTopicKey(entry.article().getMemberId(), entry.sentContent().getTopicId()))
                .collect(Collectors.toSet());
    }
}
