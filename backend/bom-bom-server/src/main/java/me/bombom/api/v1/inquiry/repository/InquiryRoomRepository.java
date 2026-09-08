package me.bombom.api.v1.inquiry.repository;

import me.bombom.api.v1.inquiry.domain.InquiryRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryRoomRepository extends JpaRepository<InquiryRoom, Long> {
}
