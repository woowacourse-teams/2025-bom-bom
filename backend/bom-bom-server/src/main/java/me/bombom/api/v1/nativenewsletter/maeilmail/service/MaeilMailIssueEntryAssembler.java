package me.bombom.api.v1.nativenewsletter.maeilmail.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.article.domain.Article;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailContent;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailSentContent;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.IssueContext;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.IssueEntries;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.IssueEntry;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.IssueEntrySelection;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.PendingIssueEntry;
import me.bombom.api.v1.nativenewsletter.maeilmail.repository.MaeilMailContentRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MaeilMailIssueEntryAssembler {

    private final MaeilMailContentRepository contentRepository;

    public IssueEntries assemble(IssueEntrySelection selection, IssueContext context) {
        return new IssueEntries(
                buildIssueEntries(selection.pendingEntries(), context.issuedAt()),
                selection.alreadyIssuedTrackIds()
        );
    }

    private List<IssueEntry> buildIssueEntries(
            List<PendingIssueEntry> pendingEntries,
            LocalDateTime issuedAt
    ) {
        if (pendingEntries.isEmpty()) {
            return List.of();
        }

        List<Long> contentIds = pendingEntries.stream()
                .map(PendingIssueEntry::contentId)
                .distinct()
                .toList();
        Map<Long, MaeilMailContent> contentById = contentRepository.findAllById(contentIds).stream()
                .collect(Collectors.toMap(MaeilMailContent::getId, content -> content));

        return pendingEntries.stream()
                .map(pendingEntry -> toIssueEntry(pendingEntry, contentById, issuedAt))
                .flatMap(Optional::stream)
                .toList();
    }

    private Optional<IssueEntry> toIssueEntry(
            PendingIssueEntry pendingEntry,
            Map<Long, MaeilMailContent> contentById,
            LocalDateTime issuedAt
    ) {
        MaeilMailContent content = contentById.get(pendingEntry.contentId());
        if (content == null) {
            return Optional.empty();
        }

        return Optional.of(new IssueEntry(
                buildArticle(content, pendingEntry.memberId(), pendingEntry.newsletterId(), issuedAt),
                pendingEntry.trackIds(),
                buildSentContent(pendingEntry.memberId(), pendingEntry.topicId(), content.getId())
        ));
    }

    private Article buildArticle(
            MaeilMailContent content,
            Long memberId,
            Long newsletterId,
            LocalDateTime issuedAt
    ) {
        return Article.builder()
                .title(content.getTitle())
                .contents(content.getContent())
                .contentsText(content.getContentsText())
                .contentsSummary(content.getContentsSummary())
                .expectedReadTime(content.getExpectedReadTime())
                .memberId(memberId)
                .newsletterId(newsletterId)
                .arrivedDateTime(issuedAt)
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
