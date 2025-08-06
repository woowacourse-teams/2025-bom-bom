package me.bombom.api.v1.highlight.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.highlight.dto.request.HighlightCreateRequest;
import me.bombom.api.v1.highlight.dto.request.UpdateHighlightRequest;
import me.bombom.api.v1.highlight.dto.response.HighlightResponse;
import me.bombom.api.v1.highlight.service.HighlightService;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/highlights")
public class HighlightController {

    private final HighlightService highlightService;

    @GetMapping
    public List<HighlightResponse> getHighlights(@LoginMember Member member) {
        return highlightService.getHighlights(member);
    }

    @GetMapping
    public List<HighlightResponse> getHighlightsByArticleId(
            @LoginMember Member member,
            @RequestParam @Positive(message = "id는 1 이상의 값이어야 합니다.") Long articleId
    ) {
        return highlightService.getHighlightsByArticleId(articleId, member);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HighlightResponse createHighlight(@LoginMember Member member, @Valid @RequestBody HighlightCreateRequest createRequest) {
        return highlightService.create(createRequest, member);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteHighlight(
            @LoginMember Member member,
            @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long id
    ) {
        highlightService.delete(id, member);
    }

    @PatchMapping("/{id}")
    public HighlightResponse updateHighlight(
            @LoginMember Member member,
            @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long id,
            @Valid @RequestBody UpdateHighlightRequest request
    ) {
        return highlightService.update(id, request, member);
    }
}
