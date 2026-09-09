package me.bombom.api.v1.inquiry.service;

import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.inquiry.domain.InquiryRoom;
import me.bombom.api.v1.inquiry.dto.InquiryRequester;
import me.bombom.api.v1.inquiry.dto.response.InquiryRoomResponse;
import me.bombom.api.v1.inquiry.repository.InquiryRoomRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryRoomService {

    private final InquiryRoomRepository inquiryRoomRepository;

    @Transactional
    public InquiryRoomResponse createRoom(InquiryRequester requester, Long categoryId) {
        validateRequester(requester);

        InquiryRoom room = requester.isMember()
                ? InquiryRoom.createMemberInquiryRoom(requester.memberId(), categoryId)
                : InquiryRoom.createGuestInquiryRoom(requester.guestId(), categoryId);

        InquiryRoom saved = inquiryRoomRepository.save(room);
        return InquiryRoomResponse.from(saved);
    }

    public Page<InquiryRoomResponse> getRooms(InquiryRequester requester, Pageable pageable) {
        validateRequester(requester);

        return inquiryRoomRepository.findRoomsByRequester(requester.memberId(), requester.guestId(), pageable)
                .map(InquiryRoomResponse::from);
    }

    public InquiryRoom getOwnedRoom(Long roomId, InquiryRequester requester) {
        validateRequester(requester);

        InquiryRoom room = inquiryRoomRepository.findById(roomId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext("roomId", roomId));

        if (!room.isOwnedBy(requester.memberId(), requester.guestId())) {
            throw new CIllegalArgumentException(ErrorDetail.FORBIDDEN_RESOURCE)
                    .addContext("roomId", roomId);
        }

        return room;
    }

    private void validateRequester(InquiryRequester requester) {
        if (requester.memberId() == null && requester.guestId() == null) {
            throw new CIllegalArgumentException(ErrorDetail.INVALID_INPUT_VALUE)
                    .addContext("reason", "member_id_or_guest_id_required");
        }
    }
}
