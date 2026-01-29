package me.bombom.api.v1.challenge.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.challenge.dto.request.CreateCommentReplyRequest;
import me.bombom.api.v1.challenge.service.ChallengeCommentReplyService;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/challenges/comments")
public class ChallengeCommentReplyController implements ChallengeCommentReplyControllerApi{

    private final ChallengeCommentReplyService challengeCommentReplyService;

    @Override
    @PostMapping("/{commentId}/replies")
    @ResponseStatus(HttpStatus.CREATED)
    public void createCommentReply(
            @LoginMember Member member,
            @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long commentId,
            @Valid @RequestBody CreateCommentReplyRequest request
    ){
        challengeCommentReplyService.createCommentReply(commentId, member.getId(), request);
    }
}
