package me.bombom.api.v1.signupsource.domain;

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
        @UniqueConstraint(name = "uk_member_signup_source_member_id", columnNames = "member_id")
})
public class MemberSignupSource extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SignupSource source;

    @Column(length = 50)
    private String detail;

    @Builder
    public MemberSignupSource(
            Long id,
            @NonNull Long memberId,
            @NonNull SignupSource source,
            String detail
    ) {
        this.id = id;
        this.memberId = memberId;
        this.source = source;
        this.detail = detail;
    }
}
