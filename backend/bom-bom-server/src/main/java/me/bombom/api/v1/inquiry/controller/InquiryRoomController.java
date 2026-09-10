package me.bombom.api.v1.inquiry.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.inquiry.dto.InquiryRequester;
import me.bombom.api.v1.inquiry.dto.request.CreateInquiryRoomRequest;
import me.bombom.api.v1.inquiry.dto.response.InquiryRoomResponse;
import me.bombom.api.v1.inquiry.resolver.GuestId;
import me.bombom.api.v1.inquiry.service.InquiryRoomService;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
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
        InquiryRequester requester = InquiryRequester.of(member, guestId);
        return inquiryRoomService.createRoom(requester, request.categoryId());
    }

    @Override
    @GetMapping
    public Page<InquiryRoomResponse> getRooms(
            @LoginMember(anonymous = true) Member member,
            @GuestId String guestId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        InquiryRequester requester = InquiryRequester.of(member, guestId);
        return inquiryRoomService.getRooms(requester, pageable);
    }
}
