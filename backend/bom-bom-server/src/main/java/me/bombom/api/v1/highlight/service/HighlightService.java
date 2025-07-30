package me.bombom.api.v1.highlight.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.highlight.domain.Highlight;
import me.bombom.api.v1.highlight.domain.HighlightLocation;
import me.bombom.api.v1.highlight.dto.HighlightCreateRequest;
import me.bombom.api.v1.highlight.dto.HighlightResponse;
import me.bombom.api.v1.highlight.repository.HighlightRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HighlightService {

    private final HighlightRepository highlightRepository;

    public List<HighlightResponse> getHighlights(Long articleId) {
        return HighlightResponse.from(highlightRepository.findByArticleId(articleId));
    }

    @Transactional
    public void create(HighlightCreateRequest createRequest) {
        validateArticleExist(createRequest);
        Highlight highlight = Highlight.builder()
                .articleId(createRequest.articleId())
                .highlightLocation(new HighlightLocation(createRequest.startOffset(), createRequest.startPath(), createRequest.endOffset(), createRequest.endPath()))
                .color(createRequest.color())
                .text(createRequest.text())
                .build();
        highlightRepository.save(highlight);
    }
}
