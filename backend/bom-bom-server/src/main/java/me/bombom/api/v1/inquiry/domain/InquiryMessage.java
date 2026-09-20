package me.bombom.api.v1.inquiry.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.bombom.api.v1.common.BaseEntity;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InquiryMessage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long roomId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private InquirySenderType senderType;

    // 메시지를 남긴 어드민의 member.id, 회원이 보낸 메시지인 경우 null
    @Column
    private Long adminId;

    @Column(length = 500)
    private String content;

    @Column
    private LocalDateTime deletedAt;

    private InquiryMessage(Long roomId, InquirySenderType senderType, Long adminId, String content) {
        this.roomId = roomId;
        this.senderType = senderType;
        this.adminId = adminId;
        this.content = content;
    }

    public static InquiryMessage createUserMessage(Long roomId, String content) {
        return new InquiryMessage(roomId, InquirySenderType.USER, null, content == null ? "" : content);
    }

    public boolean isWrittenByUser() {
        return this.senderType == InquirySenderType.USER;
    }

    public void delete(LocalDateTime now) {
        this.deletedAt = now;
    }
}
