package me.bombom.api.v1.inquiry.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.bombom.api.v1.common.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InquiryRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long memberId;

    @Column(length = 36)
    private String guestId;

    @Column(nullable = false)
    private Long categoryId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private InquiryStatus status;

    @Column
    private Long assigneeId;

    @Column
    private Long lastReadMessageIdByUser;

    @Column
    private Long lastReadMessageIdByAdmin;

    @Column
    private LocalDateTime closedAt;

    private InquiryRoom(Long memberId, String guestId, Long categoryId) {
        this.memberId = memberId;
        this.guestId = guestId;
        this.categoryId = categoryId;
        this.status = InquiryStatus.UNCONFIRMED;
    }

    public static InquiryRoom createMemberInquiryRoom(Long memberId, Long categoryId) {
        return new InquiryRoom(memberId, null, categoryId);
    }

    public static InquiryRoom createGuestInquiryRoom(String guestId, Long categoryId) {
        return new InquiryRoom(null, guestId, categoryId);
    }

    public boolean isOwnedBy(Long memberId, String guestId) {
        if (this.memberId != null) {
            return Objects.equals(this.memberId, memberId);
        }
        return Objects.equals(this.guestId, guestId);
    }
}
