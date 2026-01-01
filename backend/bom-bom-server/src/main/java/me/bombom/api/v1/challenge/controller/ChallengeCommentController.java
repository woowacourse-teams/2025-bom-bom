package me.bombom.api.v1.challenge.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.challenge.dto.request.ChallengeCommentOptionsRequest;
import me.bombom.api.v1.challenge.dto.response.ChallengeCommentCandidateArticleResponse;
import me.bombom.api.v1.challenge.dto.response.ChallengeCommentResponse;
import me.bombom.api.v1.challenge.service.ChallengeCommentService;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/challenges")
public class ChallengeCommentController implements ChallengeCommentControllerApi {

    private final ChallengeCommentService challengeCommentService;

    @Override
    @GetMapping("/{challengeId}/comments")
    public Page<ChallengeCommentResponse> getChallengeComments(
            @LoginMember Member member,
            @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long challengeId,
            @Valid @ModelAttribute ChallengeCommentOptionsRequest request,
            @PageableDefault(size = 20)
            @SortDefault.SortDefaults({
                    @SortDefault(sort = "createdAt", direction = Sort.Direction.DESC),
                    @SortDefault(sort = "id", direction = Sort.Direction.ASC)
            }) Pageable pageable
    ){
        return challengeCommentService.getChallengeComments(challengeId, member.getId(), request, pageable);
    }

    @Override
    @GetMapping("/comments/articles/candidates")
    public List<ChallengeCommentCandidateArticleResponse> getChallengeCommentCandidateArticles(
            @LoginMember Member member,
            @RequestParam LocalDate date
    ) {
        return challengeCommentService.getChallengeCommentCandidateArticles(member.getId(), date);
    }
}
