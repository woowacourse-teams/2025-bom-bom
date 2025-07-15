package me.bombom.api.v1.article.controller;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.article.dto.ArticleResponse;
import me.bombom.api.v1.article.enums.SortOption;
import me.bombom.api.v1.article.service.ArticleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @GetMapping("/api/v1/articles/{memberId}")
    public List<ArticleResponse> getArticles(
            @PathVariable Long memberId,
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false, defaultValue = "DESC") SortOption sortOption
    ) {
        return articleService.getArticles(memberId, date, categoryName, sortOption);
    }
}
