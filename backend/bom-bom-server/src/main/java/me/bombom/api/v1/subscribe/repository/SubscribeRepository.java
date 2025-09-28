package me.bombom.api.v1.subscribe.repository;

import me.bombom.api.v1.subscribe.domain.Subscribe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface SubscribeRepository extends JpaRepository<Subscribe, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Subscribe sb WHERE sb.memberId = :memberId")
    void deleteAllByMemberId(Long memberId);
}
