package me.bombom.api.v1.inquiry.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.inquiry.dto.CreateInquiryRoomRequest;
import me.bombom.api.v1.inquiry.dto.InquiryRequester;
import me.bombom.api.v1.inquiry.dto.InquiryRoomResponse;
import me.bombom.api.v1.inquiry.resolver.GuestId;
import me.bombom.api.v1.inquiry.service.InquiryRoomService;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/inquiries/rooms")
public class InquiryRoomController implements InquiryRoomControllerApi {

    private final InquiryRoomService inquiryRoomService;

    @Override
    @PostMapping
    public InquiryRoomResponse createRoom(
            @LoginMember(anonymous = true) Member member,
            @GuestId String guestId,
            @Valid @RequestBody CreateInquiryRoomRequest request
    ) {
        InquiryRequester requester = new InquiryRequester(memberId(member), guestId);
        return inquiryRoomService.createRoom(requester, request.categoryId());
    }

    private Long memberId(Member member) {
        return member == null ? null : member.getId();
    }
}
