package me.bombom.api.v1.article.controller;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.article.dto.ArticleResponse;
import me.bombom.api.v1.article.enums.SortOption;
import me.bombom.api.v1.article.service.ArticleService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @GetMapping("/api/v1/articles/{memberId}")
    public Page<ArticleResponse> getArticles(
            @PathVariable Long memberId,
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) String category,
            @RequestParam(required = false, name = "sorted", defaultValue = "DESC") SortOption sortOption,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return articleService.getArticles(
                memberId,
                date,
                category,
                sortOption,
                pageable
        );
    }
}
