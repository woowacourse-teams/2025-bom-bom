package me.bombom.api.v1.inquiry.repository;

import java.util.List;
import java.util.Map;
import me.bombom.api.v1.inquiry.domain.InquiryMessage;

public interface CustomInquiryMessageRepository {

    List<InquiryMessage> findMessagesByCursor(Long roomId, Long cursor, int size);

    Map<Long, Long> findLatestAdminMessageIdByRoomIdIn(List<Long> roomIds);
}
