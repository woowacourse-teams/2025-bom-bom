package me.bombom.api.v1.nativenewsletter.maeilmail.dto;

import java.util.ArrayList;
import java.util.List;
import me.bombom.api.v1.article.domain.Article;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailSentContent;

public record IssueEntry(
        Article article,
        List<Long> trackIds,
        MaeilMailSentContent sentContent
) {

    public IssueEntry {
        trackIds = List.copyOf(trackIds);
    }

    public IssueEntry withTrackId(Long trackId) {
        List<Long> nextTrackIds = new ArrayList<>(trackIds);
        nextTrackIds.add(trackId);
        return new IssueEntry(article, nextTrackIds, sentContent);
    }
}
