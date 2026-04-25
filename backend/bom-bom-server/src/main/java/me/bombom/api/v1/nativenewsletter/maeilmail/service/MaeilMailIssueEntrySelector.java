package me.bombom.api.v1.nativenewsletter.maeilmail.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailSubscriptionTrack;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailTopic;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.IssueContext;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.IssueEntrySelection;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.PendingIssueEntry;
import me.bombom.api.v1.nativenewsletter.maeilmail.repository.MemberTopicKey;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MaeilMailIssueEntrySelector {

    private final MaeilMailIssueContentPicker contentPicker;

    public IssueEntrySelection select(
            List<MaeilMailSubscriptionTrack> tracks,
            IssueContext context,
            Set<MemberTopicKey> issuedMemberTopicKeys
    ) {
        Map<MemberTopicKey, PendingIssueEntry> entriesByMemberTopic = new LinkedHashMap<>();
        List<Long> alreadyIssuedTrackIds = new ArrayList<>();
        for (MaeilMailSubscriptionTrack track : tracks) {
            MemberTopicKey issueKey = resolveIssueKey(track, context);
            if (issueKey == null) {
                continue;
            }

            if (issuedMemberTopicKeys.contains(issueKey)) {
                alreadyIssuedTrackIds.add(track.getId());
                continue;
            }

            PendingIssueEntry existingEntry = entriesByMemberTopic.get(issueKey);
            if (existingEntry != null) {
                entriesByMemberTopic.put(issueKey, existingEntry.withTrackId(track.getId()));
                continue;
            }

            buildPendingEntryForTrack(track, context)
                    .ifPresent(entry -> entriesByMemberTopic.put(issueKey, entry));
        }
        return new IssueEntrySelection(new ArrayList<>(entriesByMemberTopic.values()), alreadyIssuedTrackIds);
    }

    private MemberTopicKey resolveIssueKey(
            MaeilMailSubscriptionTrack track,
            IssueContext context
    ) {
        MaeilMailTopic todayTopic = context.todayTopicsByTrackId().get(track.getId());
        if (todayTopic == null) {
            return null;
        }

        return new MemberTopicKey(track.getMemberId(), todayTopic.getId());
    }

    private Optional<PendingIssueEntry> buildPendingEntryForTrack(
            MaeilMailSubscriptionTrack track,
            IssueContext context
    ) {
        MaeilMailTopic todayTopic = context.todayTopicsByTrackId().get(track.getId());
        if (todayTopic == null) {
            return Optional.empty();
        }

        Optional<Long> contentId = contentPicker.pick(
                track.getMemberId(),
                todayTopic.getId(),
                context.contentIdsByTopicId(),
                context.sentContentIdsByMemberTopic()
        );
        if (contentId.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new PendingIssueEntry(
                track.getMemberId(),
                todayTopic.getId(),
                context.newsletterId(),
                contentId.get(),
                List.of(track.getId())
        ));
    }
}
