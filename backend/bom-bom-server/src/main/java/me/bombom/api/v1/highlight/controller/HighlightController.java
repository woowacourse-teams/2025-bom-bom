package me.bombom.api.v1.highlight.controller;

import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.highlight.dto.HighlightResponse;
import me.bombom.api.v1.highlight.service.HighlightService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/highlights")
public class HighlightController {

    private final HighlightService highlightService;

    //GET, POST, DELETE, PATCH(COLOR만)

    @GetMapping
    public List<HighlightResponse> getHighlight(
            @RequestParam @Positive(message = "id는 1 이상의 값이어야 합니다.") Long articleId
    ) {
        return highlightService.getHighlights(articleId);
    }
}
