package me.bombom.api.v1.reading.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
public class MonthlyReadingSnapshotMeta {

    @Id
    @Column(nullable = false)
    private Long id;

    @Column(nullable= false)
    private LocalDateTime snapshotAt;

    @Builder
    public MonthlyReadingSnapshotMeta(
            @NotNull Long id,
            @NotNull LocalDateTime snapshotAt
    ) {
        this.id = id;
        this.snapshotAt = snapshotAt;
    }
}
