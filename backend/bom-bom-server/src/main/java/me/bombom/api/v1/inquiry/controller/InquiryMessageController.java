package me.bombom.api.v1.inquiry.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.inquiry.dto.InquiryMessageResponse;
import me.bombom.api.v1.inquiry.dto.InquiryRequester;
import me.bombom.api.v1.inquiry.dto.SendInquiryMessageRequest;
import me.bombom.api.v1.inquiry.resolver.GuestId;
import me.bombom.api.v1.inquiry.service.InquiryMessageService;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/inquiries/rooms/{roomId}/messages")
public class InquiryMessageController implements InquiryMessageControllerApi {

    private final InquiryMessageService inquiryMessageService;

    @Override
    @PostMapping
    public InquiryMessageResponse sendMessage(
            @LoginMember(anonymous = true) Member member,
            @GuestId String guestId,
            @PathVariable Long roomId,
            @Valid @RequestBody SendInquiryMessageRequest request
    ) {
        InquiryRequester requester = new InquiryRequester(memberId(member), guestId);
        return inquiryMessageService.sendMessage(requester, roomId, request);
    }

    private Long memberId(Member member) {
        return member == null ? null : member.getId();
    }
}
