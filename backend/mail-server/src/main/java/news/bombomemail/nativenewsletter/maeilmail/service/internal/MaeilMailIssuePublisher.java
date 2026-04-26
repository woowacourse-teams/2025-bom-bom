package news.bombomemail.nativenewsletter.maeilmail.service.internal;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import news.bombomemail.article.domain.Article;
import news.bombomemail.article.event.ArticleArrivedEvent;
import news.bombomemail.article.event.ArticleSource;
import news.bombomemail.article.repository.ArticleRepository;
import news.bombomemail.nativenewsletter.maeilmail.domain.MaeilMailIssueHistory;
import news.bombomemail.nativenewsletter.maeilmail.dto.IssueEntry;
import news.bombomemail.nativenewsletter.maeilmail.dto.PreparedIssueEntries;
import news.bombomemail.nativenewsletter.maeilmail.repository.MaeilMailIssueHistoryRepository;
import news.bombomemail.nativenewsletter.maeilmail.repository.MaeilMailSentContentRepository;
import news.bombomemail.nativenewsletter.maeilmail.repository.MaeilMailSubscriptionTrackRepository;
import news.bombomemail.newsletter.domain.Newsletter;
import news.bombomemail.newsletter.repository.NewsletterRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MaeilMailIssuePublisher {

    private final ArticleRepository articleRepository;
    private final NewsletterRepository newsletterRepository;
    private final MaeilMailSentContentRepository sentContentRepository;
    private final MaeilMailIssueHistoryRepository issueHistoryRepository;
    private final MaeilMailSubscriptionTrackRepository trackRepository;
    private final ApplicationEventPublisher eventPublisher;

    public void publish(PreparedIssueEntries preparedIssueEntries, LocalDate issueDate) {
        if (preparedIssueEntries.isEmpty()) {
            return;
        }

        List<IssueEntry> entries = preparedIssueEntries.entries();
        List<Article> savedArticles = saveArticles(entries);
        saveIssueProgress(entries, savedArticles, preparedIssueEntries.previouslyIssuedTrackIds(), issueDate);
        publishArticleArrivedEvents(savedArticles);
    }

    private List<Article> saveArticles(List<IssueEntry> entries) {
        if (entries.isEmpty()) {
            return List.of();
        }

        return articleRepository.saveAll(entries.stream()
                .map(IssueEntry::article)
                .toList());
    }

    private void publishArticleArrivedEvents(List<Article> articles) {
        if (articles.isEmpty()) {
            return;
        }

        Map<Long, String> newsletterNamesById = loadNewsletterNamesById(articles);
        articles.forEach(article -> eventPublisher.publishEvent(ArticleArrivedEvent.of(
                article.getNewsletterId(),
                newsletterNamesById.get(article.getNewsletterId()),
                article.getId(),
                article.getTitle(),
                article.getMemberId(),
                null,
                article.getContents(),
                ArticleSource.MAEIL_MAIL_ISSUED
        )));
    }

    private Map<Long, String> loadNewsletterNamesById(List<Article> articles) {
        Set<Long> newsletterIds = articles.stream()
                .map(Article::getNewsletterId)
                .collect(Collectors.toUnmodifiableSet());
        Map<Long, Newsletter> newslettersById = newsletterRepository.findAllById(newsletterIds)
                .stream()
                .collect(Collectors.toMap(Newsletter::getId, Function.identity()));
        if (newslettersById.size() != newsletterIds.size()) {
            throw new IllegalStateException("매일메일 발행 Article의 Newsletter를 찾을 수 없습니다. newsletterIds=" + newsletterIds);
        }

        return newslettersById.values()
                .stream()
                .collect(Collectors.toMap(Newsletter::getId, Newsletter::getName));
    }

    private void saveIssueProgress(
            List<IssueEntry> entries,
            List<Article> savedArticles,
            List<Long> previouslyIssuedTrackIds,
            LocalDate issueDate
    ) {
        saveMemberTopicIssueHistories(entries, savedArticles);
        updateIssuedTracks(entries, previouslyIssuedTrackIds, issueDate);
    }

    private void saveMemberTopicIssueHistories(List<IssueEntry> entries, List<Article> savedArticles) {
        if (entries.isEmpty()) {
            return;
        }

        if (entries.size() != savedArticles.size()) {
            throw new IllegalStateException("매일메일 발행 Article 저장 결과 수가 일치하지 않습니다. entryCount="
                    + entries.size() + ", savedArticleCount=" + savedArticles.size());
        }

        sentContentRepository.saveAll(entries.stream()
                .map(IssueEntry::sentContent)
                .toList());
        issueHistoryRepository.saveAll(toIssueHistories(entries, savedArticles));
    }

    private void updateIssuedTracks(
            List<IssueEntry> entries,
            List<Long> previouslyIssuedTrackIds,
            LocalDate issueDate
    ) {
        List<Long> issuedTrackIds = entries.stream()
                .flatMap(entry -> entry.trackIds().stream())
                .collect(Collectors.toCollection(ArrayList::new));
        issuedTrackIds.addAll(previouslyIssuedTrackIds);
        if (issuedTrackIds.isEmpty()) {
            return;
        }

        trackRepository.markIssuedByIds(issuedTrackIds.stream().distinct().toList(), issueDate);
    }

    private List<MaeilMailIssueHistory> toIssueHistories(
            List<IssueEntry> entries,
            List<Article> savedArticles
    ) {
        List<MaeilMailIssueHistory> histories = new ArrayList<>();
        for (int i = 0; i < entries.size(); i++) {
            IssueEntry entry = entries.get(i);
            Article savedArticle = savedArticles.get(i);
            histories.add(MaeilMailIssueHistory.builder()
                    .articleId(savedArticle.getId())
                    .contentId(entry.contentId())
                    .build());
        }
        return histories;
    }
}
