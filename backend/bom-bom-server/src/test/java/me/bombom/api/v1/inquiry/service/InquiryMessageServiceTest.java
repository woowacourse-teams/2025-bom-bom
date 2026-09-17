package me.bombom.api.v1.inquiry.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.sql.Timestamp;
import java.util.List;
import me.bombom.api.v1.inquiry.domain.InquiryMessageEditHistory;
import me.bombom.api.v1.inquiry.domain.InquiryRoom;
import me.bombom.api.v1.inquiry.dto.InquiryRequester;
import me.bombom.api.v1.inquiry.dto.request.SendInquiryMessageRequest;
import me.bombom.api.v1.inquiry.dto.request.UpdateInquiryMessageRequest;
import me.bombom.api.v1.inquiry.dto.response.InquiryMessageResponse;
import me.bombom.api.v1.inquiry.event.InquiryMessageSentEvent;
import me.bombom.api.v1.inquiry.repository.InquiryMessageEditHistoryRepository;
import me.bombom.api.v1.inquiry.repository.InquiryRoomRepository;
import me.bombom.support.integration.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

@RecordApplicationEvents
@IntegrationTest
class InquiryMessageServiceTest {

    @Autowired
    private InquiryMessageService inquiryMessageService;

    @Autowired
    private InquiryRoomRepository inquiryRoomRepository;

    @Autowired
    private InquiryMessageEditHistoryRepository inquiryMessageEditHistoryRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ApplicationEvents applicationEvents;

    @Test
    void 메시지를_수정하면_수정_전_내용이_이력으로_남는다() {
        InquiryRoom room = inquiryRoomRepository.save(InquiryRoom.createMemberInquiryRoom(1L, 1L));
        InquiryRequester requester = new InquiryRequester(1L, null);
        InquiryMessageResponse sent = inquiryMessageService.sendMessage(
                requester, room.getId(), new SendInquiryMessageRequest("원본 메시지", List.of()));

        inquiryMessageService.updateMessage(
                requester, room.getId(), sent.id(), new UpdateInquiryMessageRequest("수정된 메시지"));

        List<InquiryMessageEditHistory> histories = inquiryMessageEditHistoryRepository.findAll().stream()
                .filter(history -> history.getMessageId().equals(sent.id()))
                .toList();
        assertSoftly(softly -> {
            softly.assertThat(histories).hasSize(1);
            softly.assertThat(histories.get(0).getContent()).isEqualTo("원본 메시지");
        });
    }

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
    void 메시지를_전송하면_담당자_정보를_담은_이벤트가_발행된다() {
        InquiryRoom room = inquiryRoomRepository.save(InquiryRoom.createMemberInquiryRoom(1L, 1L));
        jdbcTemplate.update("UPDATE inquiry_room SET assignee_id = ? WHERE id = ?", 99L, room.getId());
        InquiryRequester requester = new InquiryRequester(1L, null);

        inquiryMessageService.sendMessage(requester, room.getId(), new SendInquiryMessageRequest("문의합니다", List.of()));

        List<InquiryMessageSentEvent> events = applicationEvents.stream(InquiryMessageSentEvent.class).toList();
        assertSoftly(softly -> {
            softly.assertThat(events).hasSize(1);
            softly.assertThat(events.get(0).content()).isEqualTo("문의합니다");
            softly.assertThat(events.get(0).assigneeId()).isEqualTo(99L);
        });
    }

    @Test
    void 삭제된_메시지는_조회되지_않는다() {
        InquiryRoom room = inquiryRoomRepository.save(InquiryRoom.createMemberInquiryRoom(1L, 1L));
        InquiryRequester requester = new InquiryRequester(1L, null);
        InquiryMessageResponse sent = inquiryMessageService.sendMessage(
                requester, room.getId(), new SendInquiryMessageRequest("삭제될 메시지", List.of()));

        inquiryMessageService.deleteMessage(requester, room.getId(), sent.id());

        List<InquiryMessageResponse> messages = inquiryMessageService
                .getMessagesAndMarkAsRead(requester, room.getId(), null, 20)
                .messages();
        assertThat(messages).isEmpty();
    }

    @Test
    void cursor가_없으면_조회된_메시지_중_최신_id로_읽음_처리된다() {
        InquiryRoom room = inquiryRoomRepository.save(InquiryRoom.createMemberInquiryRoom(1L, 1L));
        InquiryRequester requester = new InquiryRequester(1L, null);
        inquiryMessageService.sendMessage(requester, room.getId(), new SendInquiryMessageRequest("메시지1", List.of()));
        InquiryMessageResponse last = inquiryMessageService.sendMessage(
                requester, room.getId(), new SendInquiryMessageRequest("메시지2", List.of()));

        inquiryMessageService.getMessagesAndMarkAsRead(requester, room.getId(), null, 20);

        Long lastReadMessageIdByUser = jdbcTemplate.queryForObject(
                "SELECT last_read_message_id_by_user FROM inquiry_room WHERE id = ?", Long.class, room.getId());
        assertThat(lastReadMessageIdByUser).isEqualTo(last.id());
    }

    @Test
    void cursor가_있으면_읽음_처리되지_않는다() {
        InquiryRoom room = inquiryRoomRepository.save(InquiryRoom.createMemberInquiryRoom(1L, 1L));
        InquiryRequester requester = new InquiryRequester(1L, null);
        InquiryMessageResponse first = inquiryMessageService.sendMessage(
                requester, room.getId(), new SendInquiryMessageRequest("메시지1", List.of()));
        inquiryMessageService.sendMessage(requester, room.getId(), new SendInquiryMessageRequest("메시지2", List.of()));

        inquiryMessageService.getMessagesAndMarkAsRead(requester, room.getId(), first.id(), 20);

        Long lastReadMessageIdByUser = jdbcTemplate.queryForObject(
                "SELECT last_read_message_id_by_user FROM inquiry_room WHERE id = ?", Long.class, room.getId());
        assertThat(lastReadMessageIdByUser).isNull();
    }
}
