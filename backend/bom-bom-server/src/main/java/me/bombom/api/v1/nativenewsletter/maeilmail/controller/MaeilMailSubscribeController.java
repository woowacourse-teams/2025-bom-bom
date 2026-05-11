package me.bombom.api.v1.nativenewsletter.maeilmail.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.MaeilMailSubscriptionResponse;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.MaeilMailUpdateSubscriptionRequest;
import me.bombom.api.v1.nativenewsletter.maeilmail.service.MaeilMailSubscribeService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/subscriptions/native")
public class MaeilMailSubscribeController implements MaeilMailSubscribeControllerApi {

    private final MaeilMailSubscribeService maeilMailSubscribeService;

    @Override
    @GetMapping("/maeil-mail")
    public MaeilMailSubscriptionResponse getSubscription(@LoginMember Member member) {
        return maeilMailSubscribeService.getSubscription(member.getId());
    }

    @Override
    @PutMapping("/maeil-mail")
    public void putSubscription(
            @LoginMember Member member,
            @RequestBody @Valid MaeilMailUpdateSubscriptionRequest request
    ) {
        maeilMailSubscribeService.putSubscription(member, request);
    }
}
