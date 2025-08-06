package me.bombom.api.v1.pet.repository;

import java.util.List;
import java.util.Optional;
import me.bombom.api.v1.pet.domain.Pet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PetRepository extends JpaRepository<Pet, Long> {

    Optional<Pet> findByMemberId(Long memberId);

    List<Pet> findAllByIsAttended(boolean isAttended);
}
