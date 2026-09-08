package me.bombom.api.v1.inquiry.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
public class InquiryMessageImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long messageId;

    @Column(nullable = false, length = 512)
    private String imageUrl;

    @Column(nullable = false)
    private Integer sortOrder;

    public InquiryMessageImage(Long messageId, String imageUrl, Integer sortOrder) {
        this.messageId = messageId;
        this.imageUrl = imageUrl;
        this.sortOrder = sortOrder;
    }
}
