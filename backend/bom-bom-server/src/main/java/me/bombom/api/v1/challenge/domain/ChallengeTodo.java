package me.bombom.api.v1.challenge.domain;

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
@Table(name = "challenge_todo", uniqueConstraints = {
        @UniqueConstraint(name = "uk_challenge_todo", columnNames = { "challengeId", "todoType" })
})
public class ChallengeTodo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long challengeId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ChallengeTodoType todoType;

    @Builder
    public ChallengeTodo(
            Long id,
            @NonNull Long challengeId,
            @NonNull ChallengeTodoType todoType
    ) {
        this.id = id;
        this.challengeId = challengeId;
        this.todoType = todoType;
    }
}
