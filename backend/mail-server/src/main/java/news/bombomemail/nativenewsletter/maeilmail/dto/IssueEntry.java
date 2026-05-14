package news.bombomemail.nativenewsletter.maeilmail.dto;

import java.util.List;
import java.util.Objects;
import news.bombomemail.article.domain.Article;
import news.bombomemail.nativenewsletter.maeilmail.domain.MaeilMailSentContent;

public record IssueEntry(
        Article article,
        List<Long> trackIds,
        MaeilMailSentContent sentContent
) {

    public IssueEntry {
        article = Objects.requireNonNull(article);
        trackIds = List.copyOf(Objects.requireNonNull(trackIds));
        sentContent = Objects.requireNonNull(sentContent);
    }

    public Long contentId() {
        return sentContent.getContentId();
    }
}
