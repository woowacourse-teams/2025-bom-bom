package me.bombom.api.v1.inquiry.repository;

import java.util.List;
import java.util.Map;
import me.bombom.api.v1.inquiry.dto.InquiryMessageWithAdmin;

public interface CustomInquiryMessageRepository {

    List<InquiryMessageWithAdmin> findMessagesByCursor(Long roomId, Long cursor, int size);

    Map<Long, Long> findLatestAdminMessageIdsByRoomIds(List<Long> roomIds);

    Map<Long, InquiryMessageWithAdmin> findLatestMessagesByRoomIds(List<Long> roomIds);
}
