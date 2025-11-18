package me.bombom.api.v1.article.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.beans.PropertyEditorSupport;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.article.dto.request.PreviousArticleRequest;
import me.bombom.api.v1.article.dto.response.PreviousArticleDetailResponse;
import me.bombom.api.v1.article.dto.response.PreviousArticleResponse;
import me.bombom.api.v1.article.service.PreviousArticleService;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.newsletter.domain.PreviousArticleSource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/articles/previous")
public class PreviousArticleController implements PreviousArticleControllerApi {

    private final PreviousArticleService previousArticleService;

    @Override
    @GetMapping
    public List<PreviousArticleResponse> getPreviousArticles(
            @Valid @ModelAttribute PreviousArticleRequest previousArticleRequest
    ) {
        return previousArticleService.getPreviousArticles(previousArticleRequest);
    }

    @Override
    @GetMapping("/{id}")
    public PreviousArticleDetailResponse getPreviousArticleDetail(
            @LoginMember(anonymous = true) Member member,
            @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long id,
            @RequestParam PreviousArticleSource source
            ) {
        return previousArticleService.getPreviousArticleDetail(id, source, member);
    }

    @InitBinder
    private void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(PreviousArticleSource.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) throws IllegalArgumentException {
                setValue(PreviousArticleSource.valueOf(text.toUpperCase()));
            }
        });
    }
}
