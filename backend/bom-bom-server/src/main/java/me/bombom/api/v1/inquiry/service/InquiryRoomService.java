package me.bombom.api.v1.inquiry.service;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.inquiry.domain.InquiryRoom;
import me.bombom.api.v1.inquiry.domain.InquiryStatus;
import me.bombom.api.v1.inquiry.dto.InquiryRequester;
import me.bombom.api.v1.inquiry.dto.UnresolvedInquiryRoomCounts;
import me.bombom.api.v1.inquiry.dto.response.InquiryRoomResponse;
import me.bombom.api.v1.inquiry.dto.response.InquiryUnreadStatusResponse;
import me.bombom.api.v1.inquiry.repository.InquiryCategoryRepository;
import me.bombom.api.v1.inquiry.repository.InquiryMessageRepository;
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
    private final InquiryMessageRepository inquiryMessageRepository;
    private final InquiryCategoryRepository inquiryCategoryRepository;

    @Transactional
    public InquiryRoomResponse createRoom(InquiryRequester requester, Long categoryId) {
        validateRequester(requester);
        validateCategoryExists(categoryId);

        InquiryRoom room = requester.isMember()
                ? InquiryRoom.createMemberInquiryRoom(requester.memberId(), categoryId)
                : InquiryRoom.createGuestInquiryRoom(requester.guestId(), categoryId);

        InquiryRoom saved = inquiryRoomRepository.save(room);
        return InquiryRoomResponse.of(saved, false);
    }

    public Page<InquiryRoomResponse> getRooms(InquiryRequester requester, Pageable pageable) {
        validateRequester(requester);

        Page<InquiryRoom> rooms = inquiryRoomRepository.findRoomsByRequester(
                requester.memberId(), requester.guestId(), pageable);

        List<Long> roomIds = rooms.map(InquiryRoom::getId).toList();
        Map<Long, Long> latestAdminMessageIdByRoomId = inquiryMessageRepository.findLatestAdminMessageIdByRoomIdIn(
                roomIds);

        return rooms.map(room -> InquiryRoomResponse.of(room, hasUnreadMessage(room, latestAdminMessageIdByRoomId)));
    }

    public UnresolvedInquiryRoomCounts countUnresolvedRooms() {
        long unconfirmedCount = inquiryRoomRepository.countByStatus(InquiryStatus.UNCONFIRMED);
        long inProgressCount = inquiryRoomRepository.countByStatus(InquiryStatus.IN_PROGRESS);
        return UnresolvedInquiryRoomCounts.of(unconfirmedCount, inProgressCount);
    }

    public InquiryUnreadStatusResponse getUnreadStatus(InquiryRequester requester) {
        validateRequester(requester);

        boolean hasUnread = inquiryRoomRepository.existsUnreadAdminMessage(requester.memberId(), requester.guestId());
        return InquiryUnreadStatusResponse.of(hasUnread);
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
            throw new CIllegalArgumentException(ErrorDetail.MISSING_REQUESTER_IDENTIFIER);
        }
    }

    private void validateCategoryExists(Long categoryId) {
        if (!inquiryCategoryRepository.existsById(categoryId)) {
            throw new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext("categoryId", categoryId);
        }
    }

    private boolean hasUnreadMessage(InquiryRoom room, Map<Long, Long> latestAdminMessageIdByRoomId) {
        Long latestAdminMessageId = latestAdminMessageIdByRoomId.get(room.getId());
        if (latestAdminMessageId == null) {
            return false;
        }
        return room.getLastReadMessageIdByUser() == null || latestAdminMessageId > room.getLastReadMessageIdByUser();
    }
}
