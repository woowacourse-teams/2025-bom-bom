package me.bombom.api.v1.pet.scheduler;

import static org.assertj.core.api.Assertions.assertThat;

import me.bombom.api.v1.pet.domain.Pet;
import me.bombom.api.v1.pet.repository.PetRepository;
import me.bombom.support.integration.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
class PetSchedulerTest {

    @Autowired
    private PetScheduler petScheduler;

    @Autowired
    private PetRepository petRepository;

    @Test
    void 출석_초기화_스케줄러는_출석한_펫을_미출석으로_변경한다() {
        // given
        Pet pet = Pet.builder()
                .memberId(1L)
                .stageId(1L)
                .isAttended(true)
                .build();
        petRepository.save(pet);

        // when
        petScheduler.resetAttendance();

        // then
        Pet updatedPet = petRepository.findById(pet.getId()).orElseThrow();
        assertThat(updatedPet.isAttended()).isFalse();
    }
}
