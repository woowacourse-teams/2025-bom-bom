package me.bombom.api.v1.article.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.article.dto.request.ArticleNewsletterStatisticOptionsRequest;
import me.bombom.api.v1.article.dto.request.DeleteArticlesRequest;
import me.bombom.api.v1.article.dto.response.ArticleDetailResponse;
import me.bombom.api.v1.article.dto.response.ArticleResponse;
import me.bombom.api.v1.article.dto.response.ArticleNewsletterStatisticsResponse;
import me.bombom.api.v1.article.dto.request.ArticlesOptionsRequest;
import me.bombom.api.v1.article.service.ArticleService;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.highlight.dto.response.ArticleHighlightResponse;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/articles")
public class ArticleController implements ArticleControllerApi{

    private final ArticleService articleService;

    @Override
    @GetMapping
    public Page<ArticleResponse> getArticles(
            @LoginMember Member member,
            @Valid @ModelAttribute ArticlesOptionsRequest articlesOptionsRequest,
            @PageableDefault(sort = "arrivedDateTime", direction = Direction.DESC) Pageable pageable
    ) {
        return articleService.getArticles(
                member,
                articlesOptionsRequest,
                pageable
        );
    }

    @Override
    @GetMapping("/{id}")
    public ArticleDetailResponse getArticleDetail(
            @LoginMember Member member,
            @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long id
    ) {
        return articleService.getArticleDetail(id, member);
    }

    @Override
    @PatchMapping("/{id}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateIsRead(
            @LoginMember Member member,
            @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long id
    ) {
        articleService.markAsRead(id, member);
    }

    @Override
    @GetMapping("/statistics/newsletters")
    public ArticleNewsletterStatisticsResponse getArticleNewsletterStatistics(
            @LoginMember Member member,
            @Valid @ModelAttribute ArticleNewsletterStatisticOptionsRequest request
    ) {
        return articleService.getArticleNewsletterStatistics(member, request.keyword());
    }

    @Override
    @GetMapping("/{articleId}/highlights")
    public List<ArticleHighlightResponse> getHighlights(
            @LoginMember Member member,
            @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long articleId
    ) {
        return articleService.getHighlights(member, articleId);
    }

    @Override
    @PostMapping("/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteArticles(
            @LoginMember Member member,
            @Valid @RequestBody DeleteArticlesRequest request
    ) {
        articleService.delete(member, request);
    }
}
