package me.bombom.api.v1.inquiry.repository;

import java.util.Optional;
import me.bombom.api.v1.inquiry.dto.InquiryRoomWithAssignee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomInquiryRoomRepository {

    Page<InquiryRoomWithAssignee> findRoomsByRequester(Long memberId, String guestId, Pageable pageable);

    Optional<InquiryRoomWithAssignee> findRoomWithAssigneeById(Long roomId);

    boolean existsUnreadAdminMessage(Long memberId, String guestId);
}
