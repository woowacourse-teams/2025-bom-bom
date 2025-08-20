package me.bombom.api.v1.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import me.bombom.api.v1.common.BaseEntity;
import me.bombom.api.v1.member.enums.Gender;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "member",
        uniqueConstraints = @UniqueConstraint(columnNames = {"provider", "providerId"})
)
@SQLRestriction("deleted_at IS NULL")
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String provider;

    @Column(nullable = false)
    private String providerId;

    @Column(nullable = false, length = 30)
    private String email;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Column(length = 512)
    private String profileImageUrl;

    private LocalDate birthDate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(nullable = false, columnDefinition = "BIGINT")
    private Long roleId = 0L;

    private LocalDateTime deletedAt;

    @Builder
    public Member(
            Long id,
            @NonNull String provider,
            @NonNull String providerId,
            @NonNull String email,
            @NonNull String nickname,
            String profileImageUrl,
            LocalDate birthDate,
            @NonNull Gender gender,
            @NonNull Long roleId
    ) {
        this.id = id;
        this.provider = provider;
        this.providerId = providerId;
        this.email = email;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.birthDate = birthDate;
        this.gender = gender;
        this.roleId = roleId;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isWithdrawnMember() {
        return this.deletedAt != null;
    }
}
