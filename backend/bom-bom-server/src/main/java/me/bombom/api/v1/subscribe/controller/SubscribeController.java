package me.bombom.api.v1.subscribe.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.subscribe.dto.SubscribedNewsletterResponse;
import me.bombom.api.v1.subscribe.service.SubscribeService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
public class SubscribeController implements SubscribeControllerApi {

    private final SubscribeService subscribeService;

    @Override
    @GetMapping("/me/subscribes")
    public List<SubscribedNewsletterResponse> getSubscribedNewsletters(@LoginMember Member member) {
        return subscribeService.getSubscribedNewsletters(member);
    }

    @Override
    @DeleteMapping("/me/subscribes/{subscribeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelSubscription(
        @LoginMember Member member,
        @PathVariable Long subscribeId
    ) {
        subscribeService.cancelSubscription(member.getId(), subscribeId);
    }
}
