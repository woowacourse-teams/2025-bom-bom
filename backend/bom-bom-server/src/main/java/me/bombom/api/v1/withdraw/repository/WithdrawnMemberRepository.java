package me.bombom.api.v1.withdraw.repository;

import java.time.LocalDate;
import me.bombom.api.v1.withdraw.domain.WithdrawnMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface WithdrawnMemberRepository extends JpaRepository<WithdrawnMember, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM WithdrawnMember wm WHERE wm.expireDate = :date")
    void deleteAllByExpireDate(LocalDate date);
}
