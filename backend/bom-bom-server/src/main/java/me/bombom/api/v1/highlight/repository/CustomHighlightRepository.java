package me.bombom.api.v1.highlight.repository;

import java.util.List;
import me.bombom.api.v1.challenge.dto.response.ChallengeCommentHighlightResponse;
import me.bombom.api.v1.highlight.dto.response.HighlightCountPerNewsletterResponse;
import me.bombom.api.v1.highlight.dto.response.HighlightResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomHighlightRepository {

    Page<HighlightResponse> findHighlights(Long memberId, Long articleId, Long newsletterId, Pageable pageable);

    Page<ChallengeCommentHighlightResponse> findChallengeArticleHighlights(
            Long memberId,
            Long articleId,
            Pageable pageable
    );

    List<HighlightCountPerNewsletterResponse> countPerNewsletters(Long memberId);
}
