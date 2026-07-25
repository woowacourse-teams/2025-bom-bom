package me.bombom.api.v1.withdraw.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import me.bombom.api.v1.common.BaseEntity;
import me.bombom.api.v1.member.enums.Gender;
import me.bombom.api.v1.subscribe.domain.AgeGroup;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WithdrawnMember extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    private AgeGroup ageGroup;

    @Column(length = 20)
    private String provider;

    @Column(nullable = false)
    private LocalDate joinedDate;

    @Column(nullable = false)
    private LocalDate deletedDate;

    @Column(nullable = false)
    private LocalDate expireDate;

    private LocalDate lastReadDate;

    private int continueReading;

    private int bookmarkedCount;

    private int highlightCount;

    private int totalReadCount;

    private int subscribeCount;

    private int challengeCount;

    private int badgeCount;

    @Column(nullable = false)
    private boolean cleanupCompleted;

    @Builder
    public WithdrawnMember(
            Long id,
            @NonNull Long memberId,
            Gender gender,
            AgeGroup ageGroup,
            String provider,
            @NonNull LocalDate joinedDate,
            @NonNull LocalDate deletedDate,
            @NotNull LocalDate expireDate,
            LocalDate lastReadDate,
            int continueReading,
            int bookmarkedCount,
            int highlightCount,
            int totalReadCount,
            int subscribeCount,
            int challengeCount,
            int badgeCount,
            boolean cleanupCompleted
    ) {
        this.id = id;
        this.memberId = memberId;
        this.gender = gender;
        this.ageGroup = ageGroup;
        this.provider = provider;
        this.joinedDate = joinedDate;
        this.deletedDate = deletedDate;
        this.expireDate = expireDate;
        this.lastReadDate = lastReadDate;
        this.continueReading = continueReading;
        this.bookmarkedCount = bookmarkedCount;
        this.highlightCount = highlightCount;
        this.totalReadCount = totalReadCount;
        this.subscribeCount = subscribeCount;
        this.challengeCount = challengeCount;
        this.badgeCount = badgeCount;
        this.cleanupCompleted = cleanupCompleted;
    }

    public void completeCleanup() {
        cleanupCompleted = true;
    }
}
