package me.bombom.api.v1.article.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.article.dto.request.PreviousArticleRequest;
import me.bombom.api.v1.article.dto.response.PreviousArticleDetailResponse;
import me.bombom.api.v1.article.dto.response.PreviousArticleResponse;
import me.bombom.api.v1.article.service.ArticleService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/articles/previous")
public class PreviousArticleController implements PreviousArticleControllerApi {

    private final ArticleService articleService;

    @Override
    @GetMapping
    public List<PreviousArticleResponse> getPreviousArticles(
            @Valid @ModelAttribute PreviousArticleRequest previousArticleRequest
    ) {
        return articleService.getPreviousArticles(previousArticleRequest);
    }

    @Override
    @GetMapping("/{id}")
    public PreviousArticleDetailResponse getPreviousArticleDetail(
            //TODO: 이미 구독되어 있으면 구독 버튼 안보이게
            // @LoginMember(nullable = true) Member member,
            @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long id
    ) {
        return articleService.getPreviousArticleDetail(id);
    }
}
