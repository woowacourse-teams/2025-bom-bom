package me.bombom.api.v1.inquiry.service;

import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.inquiry.domain.InquiryRoom;
import me.bombom.api.v1.inquiry.dto.InquiryRequester;
import me.bombom.api.v1.inquiry.dto.request.CreateInquiryRoomRequest;
import me.bombom.api.v1.inquiry.dto.response.InquiryRoomResponse;
import me.bombom.api.v1.inquiry.dto.response.LastMessageResponse;
import me.bombom.api.v1.inquiry.service.InquiryMessageService.WrittenUserMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 문의 시작(방 생성 + 첫 메시지 작성)을 하나의 트랜잭션으로 묶습니다.
 * 첫 메시지 작성에 실패하면 방 생성도 함께 롤백됩니다.
 */
@Service
@RequiredArgsConstructor
public class InquiryStartService {

    private final InquiryRoomService inquiryRoomService;
    private final InquiryMessageService inquiryMessageService;

    @Transactional
    public InquiryRoomResponse startInquiry(InquiryRequester requester, CreateInquiryRoomRequest request) {
        inquiryMessageService.validateContentOrImages(request.content(), request.imageUrls());

        InquiryRoom room = inquiryRoomService.createRoom(requester, request.categoryId());
        WrittenUserMessage firstMessage = inquiryMessageService.writeUserMessage(
                room, request.content(), request.imageUrls());

        return InquiryRoomResponse.of(room, false, null, LastMessageResponse.of(firstMessage.message(), null));
    }
}
