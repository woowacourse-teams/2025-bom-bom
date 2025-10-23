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

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WithdrawnMember extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false, length = 50)
    private String email;

    private LocalDate birthDate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(nullable = false)
    private LocalDate joinedDate;

    @Column(nullable = false)
    private LocalDate deletedDate;

    @Column(nullable = false)
    private LocalDate expireDate;

    private int continueReading;

    private int bookmarkedCount;

    private int highlightCount;

    @Builder
    public WithdrawnMember(
            Long id,
            @NonNull Long memberId,
            @NonNull String email,
            LocalDate birthDate,
            Gender gender,
            @NonNull LocalDate joinedDate,
            @NonNull LocalDate deletedDate,
            @NotNull LocalDate expireDate,
            int continueReading,
            int bookmarkedCount,
            int highlightCount
    ) {
        this.id = id;
        this.memberId = memberId;
        this.email = email;
        this.birthDate = birthDate;
        this.gender = gender;
        this.joinedDate = joinedDate;
        this.deletedDate = deletedDate;
        this.expireDate = expireDate;
        this.continueReading = continueReading;
        this.bookmarkedCount = bookmarkedCount;
        this.highlightCount = highlightCount;
    }
}
