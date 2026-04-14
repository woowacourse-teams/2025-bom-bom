package me.bombom.api.v1.nativenewsletter.maeilmail.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_maeil_mail_subscription_track_subscription_id_field",
                columnNames = {"maeil_mail_subscription_id", "field"}
        )
})
public class MaeilMailSubscriptionTrack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long maeilMailSubscriptionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private MaeilMailTrack field;

    @Column(nullable = false)
    private int curriculumIndex = 0;

    @Builder
    public MaeilMailSubscriptionTrack(Long maeilMailSubscriptionId, MaeilMailTrack field) {
        this.maeilMailSubscriptionId = maeilMailSubscriptionId;
        this.field = field;
        this.curriculumIndex = 0;
    }
}
