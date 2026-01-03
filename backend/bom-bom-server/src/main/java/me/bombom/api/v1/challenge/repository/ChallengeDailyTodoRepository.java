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
                insert ignore into challenge_daily_todo(participant_id, challenge_todo_id, todo_date)
                select cp.id, ct.id, :today
                from challenge_participant cp
                join challenge c on c.id = cp.challenge_id
                join challenge_todo ct on ct.challenge_id = cp.challenge_id and ct.todo_type = 'READ'
                left join challenge_daily_todo dt
                    on dt.participant_id = cp.id
                   and dt.challenge_todo_id = ct.id
                   and dt.todo_date = :today
                where cp.member_id = :memberId
                  and :today between c.start_date and c.end_date
                  and cp.is_survived = true
                  and dt.id is null
            """, nativeQuery = true)
    int insertTodayReadTodoIfMissing(@Param("memberId") Long memberId,
                                     @Param("today") LocalDate today);
}
