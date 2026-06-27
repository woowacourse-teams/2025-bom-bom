package me.bombom.api.v1.pet.domain;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import org.junit.jupiter.api.Test;

class PetDomainTest {

    @Test
    void Pet은_점수_출석_스테이지를_갱신한다() {
        // given
        Pet pet = Pet.builder()
                .id(1L)
                .memberId(2L)
                .stageId(1L)
                .currentScore(10)
                .isAttended(false)
                .build();
        Stage nextStage = Stage.builder()
                .id(3L)
                .level(2)
                .requiredScore(50)
                .build();

        // when
        pet.increaseCurrentScore(15);
        pet.markAsAttended();
        pet.updateStage(nextStage);

        // then
        assertSoftly(softly -> {
            softly.assertThat(pet.getId()).isEqualTo(1L);
            softly.assertThat(pet.getMemberId()).isEqualTo(2L);
            softly.assertThat(pet.getCurrentScore()).isEqualTo(25);
            softly.assertThat(pet.isAttended()).isTrue();
            softly.assertThat(pet.getStageId()).isEqualTo(3L);
        });
    }

    @Test
    void Stage는_레벨과_필요점수를_보관한다() {
        Stage stage = Stage.builder()
                .id(1L)
                .level(3)
                .requiredScore(100)
                .build();

        assertSoftly(softly -> {
            softly.assertThat(stage.getId()).isEqualTo(1L);
            softly.assertThat(stage.getLevel()).isEqualTo(3);
            softly.assertThat(stage.getRequiredScore()).isEqualTo(100);
        });
    }
}
