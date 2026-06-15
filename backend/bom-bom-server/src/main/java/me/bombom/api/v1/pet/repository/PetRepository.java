package me.bombom.api.v1.pet.repository;

import java.util.Optional;
import me.bombom.api.v1.pet.domain.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface PetRepository extends JpaRepository<Pet, Long> {

    Optional<Pet> findByMemberId(Long memberId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Pet p SET p.isAttended = false WHERE p.isAttended = true")
    void bulkResetAllAttendance();

    void deleteByMemberId(Long memberId);
}
