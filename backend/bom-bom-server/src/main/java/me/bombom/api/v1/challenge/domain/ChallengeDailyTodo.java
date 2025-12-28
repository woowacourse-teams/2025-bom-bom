package me.bombom.api.v1.challenge.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import me.bombom.api.v1.common.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "challenge_daily_todo", uniqueConstraints = {
        @UniqueConstraint(name = "uk_challenge_daily_todo", columnNames = { "participantId", "todoDate", "challengeTodoId" })
})
public class ChallengeDailyTodo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long participantId;

    @Column(nullable = false)
    private LocalDate todoDate;

    @Column(nullable = false)
    private Long challengeTodoId;

    @Builder
    public ChallengeDailyTodo(
            Long id,
            @NonNull Long participantId,
            @NonNull LocalDate todoDate,
            @NonNull Long challengeTodoId
    ) {
        this.id = id;
        this.participantId = participantId;
        this.todoDate = todoDate;
        this.challengeTodoId = challengeTodoId;
    }
}
