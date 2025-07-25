package me.bombom.api.v1.article.controller;

import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.article.dto.ArticleDetailResponse;
import me.bombom.api.v1.article.dto.ArticleResponse;
import me.bombom.api.v1.article.dto.GetArticleCategoryStatisticsResponse;
import me.bombom.api.v1.article.enums.SortOption;
import me.bombom.api.v1.article.service.ArticleService;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
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
            @LoginMember Member member,
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate date,
            @RequestParam(required = false) String category,
            @RequestParam(required = false, name = "sorted", defaultValue = "desc") String sorted,
            @RequestParam(required = false) String keyword,
            @PageableDefault Pageable pageable
    ) {
        return articleService.getArticles(
                member,
                date,
                category,
                SortOption.from(sorted),
                keyword,
                pageable
        );
    }

    @GetMapping("/{id}")
    public ArticleDetailResponse getArticleDetail(
            @LoginMember Member member,
            @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long id
    ) {
        return articleService.getArticleDetail(id, member);
    }

    @PatchMapping("/{id}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateIsRead(
            @LoginMember Member member,
            @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long id
    ) {
        articleService.markAsRead(id, member);
    }

    @GetMapping("/statistics/categories")
    public GetArticleCategoryStatisticsResponse getArticleCategoryStatistics(
            @LoginMember Member member,
            @RequestParam(required = false) String keyword
    ){
        return articleService.getArticleCategoryStatistics(member, keyword);
    }
}
