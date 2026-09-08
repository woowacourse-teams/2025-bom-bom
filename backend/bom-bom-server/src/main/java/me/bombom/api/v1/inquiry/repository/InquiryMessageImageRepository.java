package me.bombom.api.v1.inquiry.repository;

import java.util.List;
import me.bombom.api.v1.inquiry.domain.InquiryMessageImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryMessageImageRepository extends JpaRepository<InquiryMessageImage, Long> {

    List<InquiryMessageImage> findByMessageIdOrderBySortOrderAsc(Long messageId);
}
