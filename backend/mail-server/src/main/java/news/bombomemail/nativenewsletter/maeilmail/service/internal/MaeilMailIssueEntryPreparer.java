package news.bombomemail.nativenewsletter.maeilmail.service.internal;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import news.bombomemail.article.domain.Article;
import news.bombomemail.nativenewsletter.maeilmail.domain.MaeilMailContent;
import news.bombomemail.nativenewsletter.maeilmail.domain.MaeilMailSentContent;
import news.bombomemail.nativenewsletter.maeilmail.domain.MaeilMailSubscriptionTrack;
import news.bombomemail.nativenewsletter.maeilmail.domain.MaeilMailTopic;
import news.bombomemail.nativenewsletter.maeilmail.dto.IssueData;
import news.bombomemail.nativenewsletter.maeilmail.dto.IssueEntry;
import news.bombomemail.nativenewsletter.maeilmail.dto.MemberTopicKey;
import news.bombomemail.nativenewsletter.maeilmail.dto.PreparedIssueEntries;
import news.bombomemail.nativenewsletter.maeilmail.repository.MaeilMailContentRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MaeilMailIssueEntryPreparer {

    private final Clock clock;
    private final MaeilMailContentRepository contentRepository;
    private final MaeilMailIssueContentAssigner contentAssigner;

    public PreparedIssueEntries prepare(
            List<MaeilMailSubscriptionTrack> tracks,
            IssueData issueData
    ) {
        EntrySelection selection = selectEntries(tracks, issueData);
        LocalDateTime arrivedAt = LocalDateTime.now(clock);
        return new PreparedIssueEntries(
                buildIssueEntries(selection.pendingEntries(), arrivedAt),
                selection.previouslyIssuedTrackIds()
        );
    }

    private EntrySelection selectEntries(
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
                // 동일 member-topic이 같은 날 먼저 발행됐으면 Article은 만들지 않고 track 진행 상태만 맞춘다.
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
        return new EntrySelection(new ArrayList<>(entriesByMemberTopic.values()), previouslyIssuedTrackIds);
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

    private List<IssueEntry> buildIssueEntries(
            List<PendingEntry> pendingEntries,
            LocalDateTime arrivedAt
    ) {
        if (pendingEntries.isEmpty()) {
            return List.of();
        }

        List<Long> contentIds = pendingEntries.stream()
                .map(PendingEntry::contentId)
                .distinct()
                .toList();
        Map<Long, MaeilMailContent> contentById = contentRepository.findAllById(contentIds).stream()
                .collect(Collectors.toMap(MaeilMailContent::getId, content -> content));
        validateAllContentsLoaded(contentIds, contentById);

        return pendingEntries.stream()
                .map(pendingEntry -> toIssueEntry(pendingEntry, contentById, arrivedAt))
                .toList();
    }

    private void validateAllContentsLoaded(
            List<Long> contentIds,
            Map<Long, MaeilMailContent> contentById
    ) {
        List<Long> missingContentIds = contentIds.stream()
                .filter(contentId -> !contentById.containsKey(contentId))
                .toList();
        if (!missingContentIds.isEmpty()) {
            throw new IllegalStateException("매일메일 발행 content를 찾을 수 없습니다. contentIds=" + missingContentIds);
        }
    }

    private IssueEntry toIssueEntry(
            PendingEntry pendingEntry,
            Map<Long, MaeilMailContent> contentById,
            LocalDateTime arrivedAt
    ) {
        MaeilMailContent content = contentById.get(pendingEntry.contentId());

        return new IssueEntry(
                buildArticle(content, pendingEntry.memberId(), pendingEntry.newsletterId(), arrivedAt),
                pendingEntry.trackIds(),
                buildSentContent(pendingEntry.memberId(), pendingEntry.topicId(), content.getId())
        );
    }

    private Article buildArticle(
            MaeilMailContent content,
            Long memberId,
            Long newsletterId,
            LocalDateTime arrivedAt
    ) {
        return Article.builder()
                .title(content.getTitle())
                .contents(content.getContent())
                .contentsText(content.getContentsText())
                .contentsSummary(content.getContentsSummary())
                .expectedReadTime(content.getExpectedReadTime())
                .memberId(memberId)
                .newsletterId(newsletterId)
                .arrivedDateTime(arrivedAt)
                .build();
    }

    private MaeilMailSentContent buildSentContent(
            Long memberId,
            Long topicId,
            Long contentId
    ) {
        return MaeilMailSentContent.builder()
                .memberId(memberId)
                .topicId(topicId)
                .contentId(contentId)
                .build();
    }

    private record EntrySelection(
            List<PendingEntry> pendingEntries,
            List<Long> previouslyIssuedTrackIds
    ) {
    }

    private record PendingEntry(
            Long memberId,
            Long topicId,
            Long newsletterId,
            Long contentId,
            List<Long> trackIds
    ) {

        private PendingEntry {
            trackIds = List.copyOf(trackIds);
        }

        private PendingEntry withTrackId(Long trackId) {
            List<Long> nextTrackIds = new ArrayList<>(trackIds);
            nextTrackIds.add(trackId);
            return new PendingEntry(memberId, topicId, newsletterId, contentId, nextTrackIds);
        }
    }
}
