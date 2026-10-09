package news.bombom.inquiry.repository;

import news.bombom.inquiry.domain.InquiryMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryMessageRepository extends JpaRepository<InquiryMessage, Long> {
}
