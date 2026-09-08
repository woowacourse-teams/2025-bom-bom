package me.bombom.api.v1.inquiry.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.bombom.api.v1.common.BaseEntity;

@Entity
@Getter
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

    @Column
    private Long adminId;

    @Column(nullable = false, length = 500)
    private String content;

    private InquiryMessage(Long roomId, InquirySenderType senderType, Long adminId, String content) {
        this.roomId = roomId;
        this.senderType = senderType;
        this.adminId = adminId;
        this.content = content;
    }

    public static InquiryMessage createUserMessage(Long roomId, String content) {
        return new InquiryMessage(roomId, InquirySenderType.USER, null, content);
    }

    public boolean isWrittenByUser() {
        return this.senderType == InquirySenderType.USER;
    }

    public void updateContent(String content) {
        this.content = content;
    }
}
