package me.bombom.api.v1.inquiry.repository;

import me.bombom.api.v1.inquiry.domain.InquiryRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomInquiryRoomRepository {

    Page<InquiryRoom> findRoomsByRequester(Long memberId, String guestId, Pageable pageable);
}
