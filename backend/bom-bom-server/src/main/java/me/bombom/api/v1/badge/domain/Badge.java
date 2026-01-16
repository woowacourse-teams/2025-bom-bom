package me.bombom.api.v1.badge.domain;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import me.bombom.api.v1.common.BaseEntity;

@Entity
@Getter
@Table(name = "badge")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "badge_category", discriminatorType = DiscriminatorType.STRING)
public abstract class Badge extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, insertable = false, updatable = false)
    private BadgeCategory badgeCategory;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private BadgeType badgeType;

    protected Badge(
            Long id,
            @NonNull Long memberId,
            @NonNull BadgeType badgeType
    ) {
        this.id = id;
        this.memberId = memberId;
        this.badgeType = badgeType;
        this.badgeCategory = badgeType.getCategory();
    }
}
