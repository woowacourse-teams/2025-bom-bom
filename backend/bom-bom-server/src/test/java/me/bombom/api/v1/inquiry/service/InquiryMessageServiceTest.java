package me.bombom.api.v1.inquiry.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.sql.Timestamp;
import java.util.List;
import me.bombom.api.v1.inquiry.domain.InquiryRoom;
import me.bombom.api.v1.inquiry.dto.InquiryRequester;
import me.bombom.api.v1.inquiry.dto.request.SendInquiryMessageRequest;
import me.bombom.api.v1.inquiry.dto.response.InquiryMessageResponse;
import me.bombom.api.v1.inquiry.repository.InquiryRoomRepository;
import me.bombom.support.integration.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

@IntegrationTest
class InquiryMessageServiceTest {

    @Autowired
    private InquiryMessageService inquiryMessageService;

    @Autowired
    private InquiryRoomRepository inquiryRoomRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void 메시지를_삭제하면_물리적으로_삭제되지_않고_deletedAt만_채워진다() {
        InquiryRoom room = inquiryRoomRepository.save(InquiryRoom.createMemberInquiryRoom(1L, 1L));
        InquiryRequester requester = new InquiryRequester(1L, null);
        InquiryMessageResponse sent = inquiryMessageService.sendMessage(
                requester, room.getId(), new SendInquiryMessageRequest("삭제될 메시지", List.of()));

        inquiryMessageService.deleteMessage(requester, room.getId(), sent.id());

        Integer physicalCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM inquiry_message WHERE id = ?", Integer.class, sent.id());
        Timestamp deletedAt = jdbcTemplate.queryForObject(
                "SELECT deleted_at FROM inquiry_message WHERE id = ?", Timestamp.class, sent.id());
        assertSoftly(softly -> {
            softly.assertThat(physicalCount).isEqualTo(1);
            softly.assertThat(deletedAt).isNotNull();
        });
    }

    @Test
    void 삭제된_메시지는_조회되지_않는다() {
        InquiryRoom room = inquiryRoomRepository.save(InquiryRoom.createMemberInquiryRoom(1L, 1L));
        InquiryRequester requester = new InquiryRequester(1L, null);
        InquiryMessageResponse sent = inquiryMessageService.sendMessage(
                requester, room.getId(), new SendInquiryMessageRequest("삭제될 메시지", List.of()));

        inquiryMessageService.deleteMessage(requester, room.getId(), sent.id());

        List<InquiryMessageResponse> messages = inquiryMessageService.getMessages(requester, room.getId(), null, 20)
                .messages();
        assertThat(messages).isEmpty();
    }
}
