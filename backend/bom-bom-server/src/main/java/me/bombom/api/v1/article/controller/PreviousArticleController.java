package me.bombom.api.v1.article.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.article.dto.request.PreviousArticleRequest;
import me.bombom.api.v1.article.dto.response.PreviousArticleResponse;
import me.bombom.api.v1.article.service.ArticleService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/articles")
public class PreviousArticleController implements PreviousArticleControllerApi {

    private final ArticleService articleService;

    @Override
    @GetMapping
    public List<PreviousArticleResponse> getPreviousArticles(
            @Valid @ModelAttribute PreviousArticleRequest previousArticleRequest
    ) {
        return articleService.getPreviousArticles(previousArticleRequest);
    }
}
