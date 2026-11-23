package me.bombom.api.v1.inquiry.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.List;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.inquiry.domain.InquiryCategory;
import me.bombom.api.v1.inquiry.domain.InquirySenderType;
import me.bombom.api.v1.inquiry.domain.InquiryStatus;
import me.bombom.api.v1.inquiry.dto.InquiryRequester;
import me.bombom.api.v1.inquiry.dto.request.CreateInquiryRoomRequest;
import me.bombom.api.v1.inquiry.dto.response.InquiryRoomResponse;
import me.bombom.api.v1.inquiry.event.InquiryMessageSentEvent;
import me.bombom.api.v1.inquiry.repository.InquiryCategoryRepository;
import me.bombom.support.integration.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

@RecordApplicationEvents
@IntegrationTest
class InquiryStartServiceTest {

    @Autowired
    private InquiryStartService inquiryStartService;

    @Autowired
    private InquiryCategoryRepository inquiryCategoryRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ApplicationEvents applicationEvents;

    private Long categoryId;

    @BeforeEach
    void setUp() {
        categoryId = inquiryCategoryRepository.save(InquiryCategory.builder().name("뉴스레터").build()).getId();
    }

    @Test
    void 첫_메시지와_함께_문의방을_생성한다() {
        InquiryRequester requester = new InquiryRequester(1L, null);
        List<String> imageUrls = List.of("https://s3/img1.png", "https://s3/img2.png");

        InquiryRoomResponse response = inquiryStartService.startInquiry(
                requester, new CreateInquiryRoomRequest(categoryId, "첫 문의입니다", imageUrls));

        String savedContent = jdbcTemplate.queryForObject(
                "SELECT content FROM inquiry_message WHERE room_id = ?", String.class, response.id());
        List<String> savedImageUrls = jdbcTemplate.queryForList("""
                SELECT i.image_url FROM inquiry_message_image i
                JOIN inquiry_message m ON m.id = i.message_id
                WHERE m.room_id = ?
                ORDER BY i.sort_order
                """, String.class, response.id());
        assertSoftly(softly -> {
            softly.assertThat(response.categoryId()).isEqualTo(categoryId);
            softly.assertThat(response.status()).isEqualTo(InquiryStatus.UNCONFIRMED);
            softly.assertThat(response.lastMessage().content()).isEqualTo("첫 문의입니다");
            softly.assertThat(response.lastMessage().senderType()).isEqualTo(InquirySenderType.USER);
            softly.assertThat(savedContent).isEqualTo("첫 문의입니다");
            softly.assertThat(savedImageUrls).containsExactlyElementsOf(imageUrls);
        });
    }

    @Test
    void 비회원도_첫_메시지와_함께_문의방을_생성한다() {
        String guestId = "11111111-1111-4111-8111-111111111111";
        InquiryRequester requester = new InquiryRequester(null, guestId);

        InquiryRoomResponse response = inquiryStartService.startInquiry(
                requester, new CreateInquiryRoomRequest(categoryId, "비회원 문의", List.of()));

        String savedGuestId = jdbcTemplate.queryForObject(
                "SELECT guest_id FROM inquiry_room WHERE id = ?", String.class, response.id());
        assertSoftly(softly -> {
            softly.assertThat(savedGuestId).isEqualTo(guestId);
            softly.assertThat(response.lastMessage().content()).isEqualTo("비회원 문의");
        });
    }

    @Test
    void 이미지만으로도_문의방을_생성할_수_있다() {
        InquiryRequester requester = new InquiryRequester(1L, null);

        InquiryRoomResponse response = inquiryStartService.startInquiry(
                requester, new CreateInquiryRoomRequest(categoryId, null, List.of("https://s3/img1.png")));

        assertSoftly(softly -> {
            softly.assertThat(countRooms()).isEqualTo(1);
            softly.assertThat(countMessages()).isEqualTo(1);
            softly.assertThat(response.lastMessage().content()).isEmpty();
        });
    }

    @Test
    void 첫_메시지를_작성하면_디스코드_알림_이벤트가_발행된다() {
        InquiryRequester requester = new InquiryRequester(1L, null);

        inquiryStartService.startInquiry(requester, new CreateInquiryRoomRequest(categoryId, "첫 문의입니다", null));

        List<InquiryMessageSentEvent> events = applicationEvents.stream(InquiryMessageSentEvent.class).toList();
        assertSoftly(softly -> {
            softly.assertThat(events).hasSize(1);
            softly.assertThat(events.get(0).content()).isEqualTo("첫 문의입니다");
            softly.assertThat(events.get(0).assigneeId()).isNull();
        });
    }

    @Test
    void 내용과_이미지가_모두_없으면_문의방도_생성되지_않는다() {
        InquiryRequester requester = new InquiryRequester(1L, null);
        CreateInquiryRoomRequest request = new CreateInquiryRoomRequest(categoryId, "", List.of());

        assertThatThrownBy(() -> inquiryStartService.startInquiry(requester, request))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.INVALID_INPUT_VALUE);
        assertSoftly(softly -> {
            softly.assertThat(countRooms()).isZero();
            softly.assertThat(countMessages()).isZero();
            softly.assertThat(applicationEvents.stream(InquiryMessageSentEvent.class)).isEmpty();
        });
    }

    @Test
    void 존재하지_않는_카테고리면_문의방과_메시지가_생성되지_않는다() {
        InquiryRequester requester = new InquiryRequester(1L, null);
        CreateInquiryRoomRequest request = new CreateInquiryRoomRequest(-1L, "문의합니다", List.of());

        assertThatThrownBy(() -> inquiryStartService.startInquiry(requester, request))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.ENTITY_NOT_FOUND);
        assertSoftly(softly -> {
            softly.assertThat(countRooms()).isZero();
            softly.assertThat(countMessages()).isZero();
        });
    }

    @Test
    void 회원도_비회원도_아니면_문의방을_생성할_수_없다() {
        InquiryRequester requester = new InquiryRequester(null, null);
        CreateInquiryRoomRequest request = new CreateInquiryRoomRequest(categoryId, "문의합니다", List.of());

        assertThatThrownBy(() -> inquiryStartService.startInquiry(requester, request))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.MISSING_REQUESTER_IDENTIFIER);
    }

    private Integer countRooms() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM inquiry_room", Integer.class);
    }

    private Integer countMessages() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM inquiry_message", Integer.class);
    }
}
