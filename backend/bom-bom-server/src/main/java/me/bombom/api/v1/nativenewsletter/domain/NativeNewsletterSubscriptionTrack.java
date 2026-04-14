package me.bombom.api.v1.nativenewsletter.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NativeNewsletterSubscriptionTrack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long nativeNewsletterSubscriptionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private NativeNewsletterTrack field;

    @Column(nullable = false)
    private int curriculumIndex = 0;

    @Builder
    public NativeNewsletterSubscriptionTrack(Long nativeNewsletterSubscriptionId, NativeNewsletterTrack field) {
        this.nativeNewsletterSubscriptionId = nativeNewsletterSubscriptionId;
        this.field = field;
        this.curriculumIndex = 0;
    }
}
