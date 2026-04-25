package me.bombom.api.v1.nativenewsletter.maeilmail.service.internal;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.article.domain.Article;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailContent;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailSentContent;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.IssueEntry;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.IssueEntrySelectionResult;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.IssueEntrySelectionResult.PendingEntry;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.PreparedIssueEntries;
import me.bombom.api.v1.nativenewsletter.maeilmail.repository.MaeilMailContentRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MaeilMailIssueEntryAssembler {

    private final Clock clock;
    private final MaeilMailContentRepository contentRepository;

    public PreparedIssueEntries assemble(IssueEntrySelectionResult selection) {
        LocalDateTime arrivedAt = LocalDateTime.now(clock);
        return new PreparedIssueEntries(
                buildIssueEntries(selection.pendingEntries(), arrivedAt),
                selection.previouslyIssuedTrackIds()
        );
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

        return pendingEntries.stream()
                .map(pendingEntry -> toIssueEntry(pendingEntry, contentById, arrivedAt))
                .flatMap(Optional::stream)
                .toList();
    }

    private Optional<IssueEntry> toIssueEntry(
            PendingEntry pendingEntry,
            Map<Long, MaeilMailContent> contentById,
            LocalDateTime arrivedAt
    ) {
        MaeilMailContent content = contentById.get(pendingEntry.contentId());
        if (content == null) {
            return Optional.empty();
        }

        return Optional.of(new IssueEntry(
                buildArticle(content, pendingEntry.memberId(), pendingEntry.newsletterId(), arrivedAt),
                pendingEntry.trackIds(),
                buildSentContent(pendingEntry.memberId(), pendingEntry.topicId(), content.getId())
        ));
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
}
