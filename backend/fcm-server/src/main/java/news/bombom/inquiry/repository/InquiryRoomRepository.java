package news.bombom.inquiry.repository;

import news.bombom.inquiry.domain.InquiryRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryRoomRepository extends JpaRepository<InquiryRoom, Long> {
}
