package me.bombom.api.v1.reading.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberReadRateLimit {

    @Id
    private Long memberId;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal tokens;

    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
