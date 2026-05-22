package me.bombom.api.v1.article.controller;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.article.dto.request.ArticlesOptionsRequest;
import me.bombom.api.v1.article.dto.response.ArticleResponse;
import me.bombom.api.v1.article.service.ArticleService;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.openapi.api.ArticleApi;
import me.bombom.openapi.common.ArticleResponseMapper;
import me.bombom.openapi.model.PageArticleResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ArticleListController implements ArticleApi {

    private final ArticleService articleService;

    @Override
    public PageArticleResponse getArticles(
            @LoginMember Member member,
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long newsletterId,
            @PageableDefault(sort = "arrivedDateTime", direction = Direction.DESC) Pageable pageable
    ) {
        Page<ArticleResponse> page = articleService.getArticles(
                member,
                ArticlesOptionsRequest.of(date, newsletterId),
                pageable
        );
        return ArticleResponseMapper.toPage(page);
    }
}
