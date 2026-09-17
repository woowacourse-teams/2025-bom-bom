package me.bombom.api.v1.inquiry.repository;

import me.bombom.api.v1.inquiry.domain.InquiryMessageEditHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryMessageEditHistoryRepository extends JpaRepository<InquiryMessageEditHistory, Long> {
}
