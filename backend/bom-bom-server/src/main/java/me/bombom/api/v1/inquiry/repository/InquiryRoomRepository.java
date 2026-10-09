package me.bombom.api.v1.inquiry.repository;

import me.bombom.api.v1.inquiry.domain.InquiryRoom;
import me.bombom.api.v1.inquiry.domain.InquiryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryRoomRepository extends JpaRepository<InquiryRoom, Long>, CustomInquiryRoomRepository {

    long countByStatus(InquiryStatus status);
}
