package me.bombom.api.v1.nativenewsletter.maeilmail.service.internal;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailSubscriptionTrack;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailTopic;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.IssueData;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.IssueEntrySelectionResult;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.IssueEntrySelectionResult.PendingEntry;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.MemberTopicKey;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MaeilMailIssueEntrySelector {

    private final MaeilMailIssueContentAssigner contentAssigner;

    public IssueEntrySelectionResult select(
            List<MaeilMailSubscriptionTrack> tracks,
            IssueData issueData
    ) {
        Map<MemberTopicKey, PendingEntry> entriesByMemberTopic = new LinkedHashMap<>();
        List<Long> previouslyIssuedTrackIds = new ArrayList<>();
        for (MaeilMailSubscriptionTrack track : tracks) {
            MaeilMailTopic issueTopic = issueData.issueTopicsByTrackId().get(track.getId());
            if (issueTopic == null) {
                continue;
            }

            MemberTopicKey memberTopicKey = new MemberTopicKey(track.getMemberId(), issueTopic.getId());
            if (issueData.issuedMemberTopicKeys().contains(memberTopicKey)) {
                previouslyIssuedTrackIds.add(track.getId());
                continue;
            }

            PendingEntry existingEntry = entriesByMemberTopic.get(memberTopicKey);
            if (existingEntry != null) {
                entriesByMemberTopic.put(memberTopicKey, existingEntry.withTrackId(track.getId()));
                continue;
            }

            buildPendingEntryForTrack(track, issueTopic, issueData)
                    .ifPresent(entry -> entriesByMemberTopic.put(memberTopicKey, entry));
        }
        return new IssueEntrySelectionResult(new ArrayList<>(entriesByMemberTopic.values()), previouslyIssuedTrackIds);
    }

    private Optional<PendingEntry> buildPendingEntryForTrack(
            MaeilMailSubscriptionTrack track,
            MaeilMailTopic issueTopic,
            IssueData issueData
    ) {
        Optional<Long> contentId = contentAssigner.assignContentIdOrRecycle(
                track.getMemberId(),
                issueTopic.getId(),
                issueData.contentIdsByTopicId(),
                issueData.sentContentIdsByMemberTopic()
        );
        return contentId.map(pickedContentId -> new PendingEntry(
                track.getMemberId(),
                issueTopic.getId(),
                issueData.newsletterId(),
                pickedContentId,
                List.of(track.getId())
        ));
    }
}
