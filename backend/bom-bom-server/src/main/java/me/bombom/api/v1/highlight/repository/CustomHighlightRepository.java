package me.bombom.api.v1.highlight.repository;

import java.util.List;
import me.bombom.api.v1.highlight.dto.response.HighlightResponse;

public interface CustomHighlightRepository {

    List<HighlightResponse> findHighlights(Long memberId, Long articleId, Long newsletterId);
}
