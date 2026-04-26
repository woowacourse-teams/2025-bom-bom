package news.bombomemail.nativenewsletter.maeilmail.dto;

import java.util.List;
import news.bombomemail.article.domain.Article;
import news.bombomemail.nativenewsletter.maeilmail.domain.MaeilMailSentContent;

public record IssueEntry(
        Article article,
        List<Long> trackIds,
        MaeilMailSentContent sentContent,
        Long contentId
) {

    public IssueEntry {
        trackIds = List.copyOf(trackIds);
    }
}
