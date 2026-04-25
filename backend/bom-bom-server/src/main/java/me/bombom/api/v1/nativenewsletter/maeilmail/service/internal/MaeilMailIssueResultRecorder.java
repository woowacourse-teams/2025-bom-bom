package me.bombom.api.v1.nativenewsletter.maeilmail.service.internal;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.article.domain.Article;
import me.bombom.api.v1.article.domain.RecentArticle;
import me.bombom.api.v1.article.repository.ArticleRepository;
import me.bombom.api.v1.article.repository.RecentArticleRepository;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailIssueHistory;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.IssueEntry;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.PreparedIssueEntries;
import me.bombom.api.v1.nativenewsletter.maeilmail.repository.MaeilMailIssueHistoryRepository;
import me.bombom.api.v1.nativenewsletter.maeilmail.repository.MaeilMailSentContentRepository;
import me.bombom.api.v1.nativenewsletter.maeilmail.repository.MaeilMailSubscriptionTrackRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MaeilMailIssueResultRecorder {

    private final ArticleRepository articleRepository;
    private final RecentArticleRepository recentArticleRepository;
    private final MaeilMailSentContentRepository sentContentRepository;
    private final MaeilMailIssueHistoryRepository issueHistoryRepository;
    private final MaeilMailSubscriptionTrackRepository trackRepository;

    public void record(PreparedIssueEntries preparedIssueEntries, LocalDate issueDate) {
        if (preparedIssueEntries.isEmpty()) {
            return;
        }
        List<IssueEntry> entries = preparedIssueEntries.entries();
        List<Article> savedArticles = articleRepository.saveAll(
                entries.stream().map(IssueEntry::article).toList());
        recentArticleRepository.saveAll(toRecentArticles(savedArticles));

        sentContentRepository.saveAll(entries.stream().map(IssueEntry::sentContent).toList());
        issueHistoryRepository.saveAll(toIssueHistories(issueDate, entries));
        List<Long> issuedTrackIds = entries.stream()
                .flatMap(entry -> entry.trackIds().stream())
                .collect(Collectors.toCollection(ArrayList::new));
        issuedTrackIds.addAll(preparedIssueEntries.previouslyIssuedTrackIds());
        markIssued(issuedTrackIds, issueDate);
    }

    private void markIssued(List<Long> trackIds, LocalDate issueDate) {
        if (trackIds.isEmpty()) {
            return;
        }

        trackRepository.markIssuedByIds(trackIds, issueDate);
    }

    private List<RecentArticle> toRecentArticles(List<Article> articles) {
        return articles.stream()
                .map(this::toRecentArticle)
                .toList();
    }

    private RecentArticle toRecentArticle(Article article) {
        return RecentArticle.builder()
                .articleId(article.getId())
                .title(article.getTitle())
                .contents(article.getContents())
                .contentsText(article.getContentsText())
                .contentsSummary(article.getContentsSummary())
                .expectedReadTime(article.getExpectedReadTime())
                .memberId(article.getMemberId())
                .newsletterId(article.getNewsletterId())
                .arrivedDateTime(article.getArrivedDateTime())
                .build();
    }

    private List<MaeilMailIssueHistory> toIssueHistories(
            LocalDate issueDate,
            List<IssueEntry> entries
    ) {
        List<MaeilMailIssueHistory> issueHistories = new ArrayList<>();
        for (IssueEntry entry : entries) {
            issueHistories.add(MaeilMailIssueHistory.builder()
                    .issueDate(issueDate)
                    .memberId(entry.sentContent().getMemberId())
                    .topicId(entry.sentContent().getTopicId())
                    .build());
        }
        return issueHistories;
    }
}
