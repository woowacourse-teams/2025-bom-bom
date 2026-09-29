package me.bombom.api.v1.inquiry.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import me.bombom.api.v1.inquiry.domain.InquiryRoom;
import me.bombom.api.v1.inquiry.domain.InquiryStatus;
import me.bombom.api.v1.inquiry.dto.InquiryRequester;
import me.bombom.api.v1.inquiry.dto.UnresolvedInquiryRoomCounts;
import me.bombom.api.v1.inquiry.dto.response.InquiryRoomResponse;
import me.bombom.api.v1.inquiry.dto.response.InquiryUnreadStatusResponse;
import me.bombom.api.v1.inquiry.repository.InquiryRoomRepository;
import me.bombom.support.integration.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;

@IntegrationTest
class InquiryRoomServiceTest {

    @Autowired
    private InquiryRoomService inquiryRoomService;

    @Autowired
    private InquiryRoomRepository inquiryRoomRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void 어드민_메시지가_없으면_안읽음이_아니다() {
        InquiryRoom room = inquiryRoomRepository.save(InquiryRoom.createMemberInquiryRoom(1L, 1L));
        insertUserMessage(room.getId(), "문의합니다");
        InquiryRequester requester = new InquiryRequester(1L, null);

        InquiryRoomResponse response = getFirstRoom(requester);

        assertThat(response.hasUnreadMessage()).isFalse();
    }

    @Test
    void 어드민_메시지가_있고_아직_읽지_않았으면_안읽음이다() {
        InquiryRoom room = inquiryRoomRepository.save(InquiryRoom.createMemberInquiryRoom(1L, 1L));
        insertUserMessage(room.getId(), "문의합니다");
        insertAdminMessage(room.getId(), "안녕하세요");
        InquiryRequester requester = new InquiryRequester(1L, null);

        InquiryRoomResponse response = getFirstRoom(requester);

        assertThat(response.hasUnreadMessage()).isTrue();
    }

    @Test
    void 어드민_메시지를_읽은_이후에는_안읽음이_아니다() {
        InquiryRoom room = inquiryRoomRepository.save(InquiryRoom.createMemberInquiryRoom(1L, 1L));
        insertUserMessage(room.getId(), "문의합니다");
        Long adminMessageId = insertAdminMessage(room.getId(), "안녕하세요");
        jdbcTemplate.update(
                "UPDATE inquiry_room SET last_read_message_id_by_user = ? WHERE id = ?",
                adminMessageId, room.getId());
        InquiryRequester requester = new InquiryRequester(1L, null);

        InquiryRoomResponse response = getFirstRoom(requester);

        assertThat(response.hasUnreadMessage()).isFalse();
    }

    @Test
    void 안읽은_어드민_메시지가_있으면_미확인_상태가_true다() {
        InquiryRoom room = inquiryRoomRepository.save(InquiryRoom.createMemberInquiryRoom(1L, 1L));
        insertAdminMessage(room.getId(), "안녕하세요");
        InquiryRequester requester = new InquiryRequester(1L, null);

        InquiryUnreadStatusResponse response = inquiryRoomService.getUnreadStatus(requester);

        assertThat(response.hasUnread()).isTrue();
    }

    @Test
    void 안읽은_어드민_메시지가_없으면_미확인_상태가_false다() {
        InquiryRoom room = inquiryRoomRepository.save(InquiryRoom.createMemberInquiryRoom(1L, 1L));
        insertUserMessage(room.getId(), "문의합니다");
        InquiryRequester requester = new InquiryRequester(1L, null);

        InquiryUnreadStatusResponse response = inquiryRoomService.getUnreadStatus(requester);

        assertThat(response.hasUnread()).isFalse();
    }

    @Test
    void 상태별_미확인_방_개수를_집계한다() {
        InquiryRoom unconfirmed = inquiryRoomRepository.save(InquiryRoom.createMemberInquiryRoom(1L, 1L));
        InquiryRoom inProgress = inquiryRoomRepository.save(InquiryRoom.createMemberInquiryRoom(2L, 1L));
        jdbcTemplate.update(
                "UPDATE inquiry_room SET status = ? WHERE id = ?",
                InquiryStatus.IN_PROGRESS.name(), inProgress.getId());
        InquiryRoom done = inquiryRoomRepository.save(InquiryRoom.createMemberInquiryRoom(3L, 1L));
        jdbcTemplate.update(
                "UPDATE inquiry_room SET status = ? WHERE id = ?",
                InquiryStatus.DONE.name(), done.getId());

        UnresolvedInquiryRoomCounts counts = inquiryRoomService.countUnresolvedRooms();

        assertSoftly(softly -> {
            softly.assertThat(counts.unconfirmedCount()).isEqualTo(1);
            softly.assertThat(counts.inProgressCount()).isEqualTo(1);
        });
    }

    private InquiryRoomResponse getFirstRoom(InquiryRequester requester) {
        Pageable pageable = PageRequest.of(0, 20);
        return inquiryRoomService.getRooms(requester, pageable).getContent().get(0);
    }

    private void insertUserMessage(Long roomId, String content) {
        jdbcTemplate.update(
                "INSERT INTO inquiry_message (room_id, sender_type, content, created_at, updated_at) "
                        + "VALUES (?, 'USER', ?, NOW(), NOW())",
                roomId, content);
    }

    private Long insertAdminMessage(Long roomId, String content) {
        jdbcTemplate.update(
                "INSERT INTO inquiry_message (room_id, sender_type, admin_id, content, created_at, updated_at) "
                        + "VALUES (?, 'ADMIN', 1, ?, NOW(), NOW())",
                roomId, content);
        return jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
    }
}
