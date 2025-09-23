package me.bombom.api.v1.pet.repository;

import java.util.List;
import java.util.Optional;
import me.bombom.api.v1.pet.domain.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface PetRepository extends JpaRepository<Pet, Long> {

    Optional<Pet> findByMemberId(Long memberId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Pet p SET p.isAttended = false WHERE p.isAttended = true")
    void resetAllAttendance();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    void deleteByMemberId(Long memberId);
}
