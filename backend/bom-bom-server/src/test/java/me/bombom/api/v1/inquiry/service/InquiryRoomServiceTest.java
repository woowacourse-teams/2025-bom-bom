package me.bombom.api.v1.inquiry.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.inquiry.domain.InquiryRoom;
import me.bombom.api.v1.inquiry.domain.InquiryStatus;
import me.bombom.api.v1.inquiry.dto.InquiryRequester;
import me.bombom.api.v1.inquiry.dto.UnresolvedInquiryRoomCounts;
import me.bombom.api.v1.inquiry.dto.response.InquiryRoomResponse;
import me.bombom.api.v1.inquiry.dto.response.InquiryUnreadStatusResponse;
import me.bombom.api.v1.inquiry.repository.InquiryRoomRepository;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
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
    private MemberRepository memberRepository;

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
    void 본인_소유_방을_단건_조회한다() {
        InquiryRoom room = inquiryRoomRepository.save(InquiryRoom.createMemberInquiryRoom(1L, 1L));
        InquiryRequester requester = new InquiryRequester(1L, null);

        InquiryRoomResponse response = inquiryRoomService.getRoom(room.getId(), requester);

        assertThat(response.id()).isEqualTo(room.getId());
    }

    @Test
    void 존재하지_않는_방을_조회하면_예외가_발생한다() {
        InquiryRequester requester = new InquiryRequester(1L, null);

        assertThatThrownBy(() -> inquiryRoomService.getRoom(-1L, requester))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.ENTITY_NOT_FOUND);
    }

    @Test
    void 타인의_방을_조회하면_예외가_발생한다() {
        InquiryRoom room = inquiryRoomRepository.save(InquiryRoom.createMemberInquiryRoom(1L, 1L));
        InquiryRequester otherRequester = new InquiryRequester(2L, null);

        assertThatThrownBy(() -> inquiryRoomService.getRoom(room.getId(), otherRequester))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.FORBIDDEN_RESOURCE);
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

    @Test
    void 방_목록에_담당자_닉네임이_함께_조회된다() {
        Member assignee = memberRepository.save(TestFixture.createUniqueMember("담당봄봄", "assignee-list-1"));
        InquiryRoom room = inquiryRoomRepository.save(InquiryRoom.createMemberInquiryRoom(1L, 1L));
        jdbcTemplate.update("UPDATE inquiry_room SET assignee_id = ? WHERE id = ?", assignee.getId(), room.getId());
        InquiryRequester requester = new InquiryRequester(1L, null);

        InquiryRoomResponse response = getFirstRoom(requester);

        assertThat(response.assigneeNickname()).isEqualTo("담당봄봄");
    }

    @Test
    void 담당자가_배정되지_않은_방은_담당자_닉네임이_null이다() {
        inquiryRoomRepository.save(InquiryRoom.createMemberInquiryRoom(1L, 1L));
        InquiryRequester requester = new InquiryRequester(1L, null);

        InquiryRoomResponse response = getFirstRoom(requester);

        assertThat(response.assigneeNickname()).isNull();
    }

    @Test
    void 담당자_회원이_존재하지_않으면_닉네임이_null이다() {
        InquiryRoom room = inquiryRoomRepository.save(InquiryRoom.createMemberInquiryRoom(1L, 1L));
        jdbcTemplate.update("UPDATE inquiry_room SET assignee_id = ? WHERE id = ?", 999_999L, room.getId());
        InquiryRequester requester = new InquiryRequester(1L, null);

        InquiryRoomResponse response = getFirstRoom(requester);

        assertThat(response.assigneeNickname()).isNull();
    }

    @Test
    void 담당자가_있는_방과_없는_방이_섞여있어도_각각_매핑된다() {
        Member assignee = memberRepository.save(TestFixture.createUniqueMember("섞임담당", "assignee-mixed-1"));
        InquiryRoom assigned = inquiryRoomRepository.save(InquiryRoom.createMemberInquiryRoom(1L, 1L));
        jdbcTemplate.update("UPDATE inquiry_room SET assignee_id = ? WHERE id = ?", assignee.getId(), assigned.getId());
        InquiryRoom unassigned = inquiryRoomRepository.save(InquiryRoom.createMemberInquiryRoom(1L, 1L));
        InquiryRequester requester = new InquiryRequester(1L, null);

        List<InquiryRoomResponse> rooms = inquiryRoomService.getRooms(requester, PageRequest.of(0, 20)).getContent();

        Map<Long, InquiryRoomResponse> byId = rooms.stream()
                .collect(Collectors.toMap(InquiryRoomResponse::id, room -> room));
        assertSoftly(softly -> {
            softly.assertThat(byId.get(assigned.getId()).assigneeNickname()).isEqualTo("섞임담당");
            softly.assertThat(byId.get(unassigned.getId()).assigneeNickname()).isNull();
        });
    }

    @Test
    void 단건_조회에도_담당자_닉네임이_포함된다() {
        Member assignee = memberRepository.save(TestFixture.createUniqueMember("단건담당", "assignee-single-1"));
        InquiryRoom room = inquiryRoomRepository.save(InquiryRoom.createMemberInquiryRoom(1L, 1L));
        jdbcTemplate.update("UPDATE inquiry_room SET assignee_id = ? WHERE id = ?", assignee.getId(), room.getId());
        InquiryRequester requester = new InquiryRequester(1L, null);

        InquiryRoomResponse response = inquiryRoomService.getRoom(room.getId(), requester);

        assertThat(response.assigneeNickname()).isEqualTo("단건담당");
    }

    @Test
    void 단건_조회에서_담당자가_없으면_담당자_닉네임이_null이다() {
        InquiryRoom room = inquiryRoomRepository.save(InquiryRoom.createMemberInquiryRoom(1L, 1L));
        InquiryRequester requester = new InquiryRequester(1L, null);

        InquiryRoomResponse response = inquiryRoomService.getRoom(room.getId(), requester);

        assertThat(response.assigneeNickname()).isNull();
    }

    @Test
    void 비회원도_단건_조회에서_담당자_닉네임을_받는다() {
        Member assignee = memberRepository.save(TestFixture.createUniqueMember("게스트담당", "assignee-guest-1"));
        String guestId = "11111111-1111-4111-8111-111111111111";
        InquiryRoom room = inquiryRoomRepository.save(InquiryRoom.createGuestInquiryRoom(guestId, 1L));
        jdbcTemplate.update("UPDATE inquiry_room SET assignee_id = ? WHERE id = ?", assignee.getId(), room.getId());
        InquiryRequester requester = new InquiryRequester(null, guestId);

        InquiryRoomResponse response = inquiryRoomService.getRoom(room.getId(), requester);

        assertThat(response.assigneeNickname()).isEqualTo("게스트담당");
    }

    @Test
    void 방_목록은_최신_메시지가_늦은_방부터_정렬된다() {
        InquiryRequester requester = new InquiryRequester(1L, null);
        InquiryRoom olderRoom = inquiryRoomRepository.save(InquiryRoom.createMemberInquiryRoom(1L, 1L));
        InquiryRoom newerRoom = inquiryRoomRepository.save(InquiryRoom.createMemberInquiryRoom(1L, 1L));
        // 먼저 만들어진 방에 더 늦은 메시지를 넣어, 방 생성순과 메시지순이 어긋나게 한다
        insertUserMessageAt(olderRoom.getId(), "나중 메시지", "2026-11-03 12:00:00");
        insertUserMessageAt(newerRoom.getId(), "이전 메시지", "2026-11-03 10:00:00");

        List<InquiryRoomResponse> rooms = getRooms(requester);

        assertThat(rooms).extracting(InquiryRoomResponse::id)
                .containsExactly(olderRoom.getId(), newerRoom.getId());
    }

    @Test
    void 메시지가_없는_방은_방_생성시각을_기준으로_정렬된다() {
        InquiryRequester requester = new InquiryRequester(1L, null);
        InquiryRoom roomWithMessage = inquiryRoomRepository.save(InquiryRoom.createMemberInquiryRoom(1L, 1L));
        InquiryRoom emptyRoom = inquiryRoomRepository.save(InquiryRoom.createMemberInquiryRoom(1L, 1L));
        insertUserMessageAt(roomWithMessage.getId(), "오래된 메시지", "2020-01-01 00:00:00");

        List<InquiryRoomResponse> rooms = getRooms(requester);

        assertThat(rooms).extracting(InquiryRoomResponse::id)
                .containsExactly(emptyRoom.getId(), roomWithMessage.getId());
    }

    @Test
    void 방_목록은_마지막_메시지_내용을_함께_반환한다() {
        InquiryRoom room = inquiryRoomRepository.save(InquiryRoom.createMemberInquiryRoom(1L, 1L));
        insertUserMessageAt(room.getId(), "첫 문의", "2026-11-03 10:00:00");
        insertUserMessageAt(room.getId(), "마지막 문의", "2026-11-03 12:00:00");
        InquiryRequester requester = new InquiryRequester(1L, null);

        InquiryRoomResponse response = getFirstRoom(requester);

        assertThat(response.lastMessage().content()).isEqualTo("마지막 문의");
    }

    @Test
    void 나중에_저장된_메시지의_생성시각이_더_빠르면_생성시각이_늦은_메시지가_마지막_메시지다() {
        InquiryRoom room = inquiryRoomRepository.save(InquiryRoom.createMemberInquiryRoom(1L, 1L));
        insertUserMessageAt(room.getId(), "먼저 저장된 메시지", "2026-11-03 12:00:00");
        insertUserMessageAt(room.getId(), "나중에 저장된 메시지", "2026-11-03 10:00:00");
        InquiryRequester requester = new InquiryRequester(1L, null);

        InquiryRoomResponse response = getFirstRoom(requester);

        assertThat(response.lastMessage().content()).isEqualTo("먼저 저장된 메시지");
    }

    @Test
    void 생성시각이_같으면_나중에_저장된_메시지가_마지막_메시지다() {
        InquiryRoom room = inquiryRoomRepository.save(InquiryRoom.createMemberInquiryRoom(1L, 1L));
        insertUserMessageAt(room.getId(), "먼저 저장된 메시지", "2026-11-03 12:00:00");
        insertUserMessageAt(room.getId(), "나중에 저장된 메시지", "2026-11-03 12:00:00");
        InquiryRequester requester = new InquiryRequester(1L, null);

        InquiryRoomResponse response = getFirstRoom(requester);

        assertThat(response.lastMessage().content()).isEqualTo("나중에 저장된 메시지");
    }

    @Test
    void 생성시각이_저장순서와_어긋나도_정렬된_방의_마지막_메시지가_일치한다() {
        InquiryRoom roomWithEarlierMessage = inquiryRoomRepository.save(InquiryRoom.createMemberInquiryRoom(1L, 1L));
        InquiryRoom roomWithLaterMessage = inquiryRoomRepository.save(InquiryRoom.createMemberInquiryRoom(1L, 1L));
        insertUserMessageAt(roomWithLaterMessage.getId(), "먼저 저장되었지만 시각은 나중", "2026-11-03 12:00:00");
        insertUserMessageAt(roomWithEarlierMessage.getId(), "나중에 저장되었지만 시각은 먼저", "2026-11-03 10:00:00");
        InquiryRequester requester = new InquiryRequester(1L, null);

        List<InquiryRoomResponse> rooms = getRooms(requester);

        assertSoftly(softly -> {
            softly.assertThat(rooms.get(0).id()).isEqualTo(roomWithLaterMessage.getId());
            softly.assertThat(rooms.get(0).lastMessage().content()).isEqualTo("먼저 저장되었지만 시각은 나중");
        });
    }

    @Test
    void 메시지가_없는_방의_마지막_메시지는_null이다() {
        inquiryRoomRepository.save(InquiryRoom.createMemberInquiryRoom(1L, 1L));
        InquiryRequester requester = new InquiryRequester(1L, null);

        InquiryRoomResponse response = getFirstRoom(requester);

        assertThat(response.lastMessage()).isNull();
    }

    @Test
    void 마지막_메시지가_어드민이면_어드민_닉네임을_함께_반환한다() {
        Member admin = memberRepository.save(TestFixture.createUniqueMember("상담원", "admin-last-msg"));
        InquiryRoom room = inquiryRoomRepository.save(InquiryRoom.createMemberInquiryRoom(1L, 1L));
        insertAdminMessageAt(room.getId(), admin.getId(), "답변드립니다", "2026-11-03 12:00:00");
        InquiryRequester requester = new InquiryRequester(1L, null);

        InquiryRoomResponse response = getFirstRoom(requester);

        assertSoftly(softly -> {
            softly.assertThat(response.lastMessage().content()).isEqualTo("답변드립니다");
            softly.assertThat(response.lastMessage().adminNickname()).isEqualTo("상담원");
        });
    }

    @Test
    void 마지막_메시지가_사용자면_어드민_닉네임은_null이다() {
        InquiryRoom room = inquiryRoomRepository.save(InquiryRoom.createMemberInquiryRoom(1L, 1L));
        insertUserMessageAt(room.getId(), "문의합니다", "2026-11-03 12:00:00");
        InquiryRequester requester = new InquiryRequester(1L, null);

        InquiryRoomResponse response = getFirstRoom(requester);

        assertThat(response.lastMessage().adminNickname()).isNull();
    }

    private List<InquiryRoomResponse> getRooms(InquiryRequester requester) {
        return inquiryRoomService.getRooms(requester, PageRequest.of(0, 20)).getContent();
    }

    private void insertUserMessageAt(Long roomId, String content, String createdAt) {
        jdbcTemplate.update(
                "INSERT INTO inquiry_message (room_id, sender_type, content, created_at, updated_at) "
                        + "VALUES (?, 'USER', ?, ?, ?)",
                roomId, content, createdAt, createdAt);
    }

    private void insertAdminMessageAt(Long roomId, Long adminId, String content, String createdAt) {
        jdbcTemplate.update(
                "INSERT INTO inquiry_message (room_id, sender_type, admin_id, content, created_at, updated_at) "
                        + "VALUES (?, 'ADMIN', ?, ?, ?, ?)",
                roomId, adminId, content, createdAt, createdAt);
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
