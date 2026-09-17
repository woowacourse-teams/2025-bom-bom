package me.bombom.api.v1.inquiry.repository;

import me.bombom.api.v1.inquiry.domain.InquiryMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryMessageRepository extends JpaRepository<InquiryMessage, Long>, CustomInquiryMessageRepository {
}
