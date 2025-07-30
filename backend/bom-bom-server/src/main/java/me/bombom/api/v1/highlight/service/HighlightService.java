package me.bombom.api.v1.highlight.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.highlight.dto.HighlightResponse;
import me.bombom.api.v1.highlight.repository.HighlightRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HighlightService {

    private final HighlightRepository highlightRepository;

    public List<HighlightResponse> getHighlights(Long articleId) {
        return HighlightResponse.from(highlightRepository.findByArticleId(articleId));
    }
}
