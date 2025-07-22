package me.bombom.api.v1.article.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.article.dto.ArticleDetailResponse;
import me.bombom.api.v1.article.dto.ArticleResponse;
import me.bombom.api.v1.article.dto.GetArticleCategoryStatisticsResponse;
import me.bombom.api.v1.article.dto.GetArticlesOptions;
import me.bombom.api.v1.article.service.ArticleService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/articles")
public class ArticleController {

    private final ArticleService articleService;

    @GetMapping
    public Page<ArticleResponse> getArticles(
            @RequestParam Long memberId,
            @ModelAttribute GetArticlesOptions options,
            @PageableDefault Pageable pageable
    ) {
        return articleService.getArticles(memberId, options, pageable);
    }

    @GetMapping("/{id}")
    public ArticleDetailResponse getArticleDetail(@PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long id, @RequestParam Long memberId) {
        return articleService.getArticleDetail(id, memberId);
    }

    @PatchMapping("/{id}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateIsRead(@PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long id, @RequestParam Long memberId) {
        articleService.markAsRead(id, memberId);
    }

    @GetMapping("/statistics/categories")
    public GetArticleCategoryStatisticsResponse getArticleCategoryStatistics(@RequestParam Long memberId){
        return articleService.getArticleCategoryStatistics(memberId);
    }
}
