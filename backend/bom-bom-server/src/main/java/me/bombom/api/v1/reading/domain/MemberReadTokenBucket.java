package me.bombom.api.v1.reading.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberReadTokenBucket {

    @Id
    private Long memberId;

    @Column(nullable = false)
    private double tokens;

    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
