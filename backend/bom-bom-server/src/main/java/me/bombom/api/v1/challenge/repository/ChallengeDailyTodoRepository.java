package me.bombom.api.v1.challenge.repository;

import java.time.LocalDate;
import me.bombom.api.v1.challenge.domain.ChallengeDailyTodo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChallengeDailyTodoRepository extends JpaRepository<ChallengeDailyTodo, Long> {

    @Modifying
    @Query(value = """
                INSERT IGNORE INTO challenge_daily_todo(participant_id, challenge_todo_id, todo_date)
                SELECT cp.id, ct.id, :today
                FROM challenge_participant cp
                JOIN challenge c ON c.id = cp.challenge_id
                JOIN challenge_todo ct ON ct.challenge_id = cp.challenge_id AND ct.todo_type = :todoType
                LEFT JOIN challenge_daily_todo dt
                    ON dt.participant_id = cp.id
                   AND dt.challenge_todo_id = ct.id
                   AND dt.todo_date = :today
                WHERE cp.member_id = :memberId
                  AND :today BETWEEN c.start_date AND c.end_date
                  AND cp.is_survived = true
                  AND dt.id IS NULL
                  AND (
                        :articleId IS NULL
                        OR EXISTS (
                            SELECT 1
                            FROM article a
                            WHERE a.id = :articleId
                              AND a.member_id = cp.member_id
                        )
                  )
            """, nativeQuery = true)
    int insertTodayReadTodoIfMissing(
            @Param("memberId") Long memberId,
            @Param("articleId") Long articleId,
            @Param("today") LocalDate today,
            @Param("todoType") String todoType
    );

    boolean existsByParticipantIdAndTodoDateAndChallengeTodoId(Long participantId, LocalDate todoDate,
                                                               Long challengeTodoId);
}
