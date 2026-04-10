package me.bombom.api.v1.reading.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReadingSnapshotMeta {

    @Id
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReadingSnapshotType snapshotType;

    @Column(nullable = false)
    private LocalDateTime snapshotAt;

    @Builder
    public ReadingSnapshotMeta(
            @NotNull ReadingSnapshotType snapshotType,
            @NotNull LocalDateTime snapshotAt
    ) {
        this.snapshotType = snapshotType;
        this.snapshotAt = snapshotAt;
    }
}
