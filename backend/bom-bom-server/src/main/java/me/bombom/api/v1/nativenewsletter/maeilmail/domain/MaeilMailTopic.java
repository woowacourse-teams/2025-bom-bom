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
import lombok.NonNull;
import me.bombom.api.v1.common.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "uk_maeil_mail_topic_track_name", columnNames = {"track", "name"}),
        @UniqueConstraint(name = "uk_maeil_mail_topic_track_order", columnNames = {"track", "display_order"})
})
public class MaeilMailTopic extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private MaeilMailTrack track;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false)
    private int displayOrder;

    @Builder
    public MaeilMailTopic(
            @NonNull MaeilMailTrack track,
            @NonNull String name,
            int displayOrder
    ) {
        this.track = track;
        this.name = name;
        this.displayOrder = displayOrder;
    }
}
