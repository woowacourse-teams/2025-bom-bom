package me.bombom.api.v1.article.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.article.dto.request.ArticleNewsletterStatisticOptionsRequest;
import me.bombom.api.v1.article.dto.request.DeleteArticlesRequest;
import me.bombom.api.v1.article.dto.response.ArticleDetailResponse;
import me.bombom.api.v1.article.dto.response.ArticleResponse;
import me.bombom.api.v1.article.dto.response.ArticleNewsletterStatisticsResponse;
import me.bombom.api.v1.article.dto.request.ArticlesOptionsRequest;
import me.bombom.api.v1.article.dto.request.ArticleSearchOptionsRequest;
import me.bombom.api.v1.article.service.ArticleService;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.highlight.dto.response.ArticleHighlightResponse;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.openapi.api.ArticleApi;
import me.bombom.openapi.common.ArticleResponseMapper;
import me.bombom.openapi.model.PageArticleResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * FIXME: 여기에 적힌 API 엔드포인트와 `ArticleApi` 인터페이스에서 설정한 API 엔드포인트가 중복되어서 들어감.
 * 그래서 `/api/v1/articles/api/v1/articles` 이렇게 요청해야함.
 *
 * 해결 방향은 둘 중 하나
 * 1. generated interface가 full path를 갖는다면, controller의 class-level @RequestMapping을 제거한다.
 * 2. controller class-level prefix를 유지하고 싶다면, generated interface가 method path만 갖도록 template/spec/generator 설정을 바꾼다.
 *
 * codex는 1번이 자연스럽다고 함.
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/articles")
public class ArticleController implements ArticleControllerApi, ArticleApi {

    private final ArticleService articleService;

    /**
     * FIXME: 이후에는 generated model을 프로덕션 응답 타입으로 그대로 사용하는 방향이 나아 보임.
     * generated model을 그대로 사용하면 기존 응답 DTO -> generated DTO 변환 mapper를 매번 만드는 부담을 줄일 수 있음.
     * 다만 Page<T>처럼 서버 내부 표현과 OpenAPI schema 표현이 다른 경우에는 별도 변환이 필요할 수 있음. (Mapper를 사용하는 부담이 확실히 줄어들긴함)
     * 클라이언트와의 계약이 직접 드러나는 타입은 generated model이므로, 이를 응답 경계에서 사용해야 계약을 더 잘 지킬 수 있음.
     */
    @Override
    public PageArticleResponse getArticles(
            Member member, //FIXME: Long memberId로도 가능한 것도 확인함. 아래처럼 @LoginMember를 붙여도 상관없음.
            //FIXME: 그런데, @LoginMember에 있는 두가지 옵션이 제대로 동작하는지 확인 필요
            LocalDate date,
            Long newsletterId,
            @PageableDefault(sort = "arrivedDateTime", direction = Direction.DESC) Pageable pageable
    ) {
        Page<ArticleResponse> page = articleService.getArticles(
                member,
                ArticlesOptionsRequest.of(date, newsletterId),
                pageable
        );
        return ArticleResponseMapper.toPage(page);
    }

    @Override
    @GetMapping("/search")
    public Page<ArticleResponse> getArticlesBySearch(
            @LoginMember Member member,
            @Valid @ModelAttribute ArticleSearchOptionsRequest articleSearchOptionsRequest,
            @PageableDefault(sort = "arrivedDateTime", direction = Direction.DESC) Pageable pageable
    ) {
        return articleService.getArticlesBySearch(
                member,
                articleSearchOptionsRequest,
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
