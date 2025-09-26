package me.bombom.api.v1.pet.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.UUID;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.CServerErrorException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.pet.domain.Pet;
import me.bombom.api.v1.pet.domain.Stage;
import me.bombom.api.v1.pet.dto.PetResponse;
import me.bombom.api.v1.pet.repository.PetRepository;
import me.bombom.api.v1.pet.repository.StageRepository;
import me.bombom.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@IntegrationTest
class PetServiceTest {

    @Autowired
    private PetService petService;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private StageRepository stageRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private EntityManager entityManager;

    private Member member;
    private Stage firstStage;
    private Stage secondStage;

    @BeforeEach
    void setUp() {
        member = TestFixture.createUniqueMember(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        memberRepository.save(member);
        firstStage = TestFixture.createStage(1, 0);
        stageRepository.save(firstStage);
        secondStage = TestFixture.createStage(2, 50);
        stageRepository.save(secondStage);
    }

    @Test
    void 키우기_정보_조회() {
        // given
        Pet pet = TestFixture.createPet(member, firstStage.getId());
        petRepository.save(pet);

        // when
        PetResponse result = petService.getPet(member);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.level()).isEqualTo(1);
            softly.assertThat(result.currentStageScore()).isEqualTo(0);
            softly.assertThat(result.requiredStageScore()).isEqualTo(50);
        });
    }

    @Test
    void 키우기_정보_조회_시_키우기가_없을_경우_에러() {
        // when & then
        assertThatThrownBy(() -> petService.getPet(member))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.ENTITY_NOT_FOUND);
    }

    @Test
    void 키우기_정보_조회_시_성장_단계가_없을_경우_에러() {
        // given
        Pet pet = TestFixture.createPet(member, 100L);
        petRepository.save(pet);

        // when & then
        assertThatThrownBy(() -> petService.getPet(member))
                .isInstanceOf(CServerErrorException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.INTERNAL_SERVER_ERROR);
    }

    @Test
    void 키우기_출석_점수_반영() {
        // given
        Stage stage = TestFixture.createStage(1, 0);
        stageRepository.save(stage);
        Pet pet = TestFixture.createPet(member, stage.getId());
        petRepository.save(pet);

        // when
        petService.attend(member);

        // then
        assertSoftly(softly -> {
                    softly.assertThat(pet.getCurrentScore()).isEqualTo(5);
                    softly.assertThat(pet.isAttended()).isTrue();
                }
        );
    }

    @Test
    void 키우기_출석_시_키우기가_없을_경우_에러() {
        // when & then
        assertThatThrownBy(() -> petService.attend(member))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.ENTITY_NOT_FOUND);
    }

    @Test
    void 키우기_출석_초기화() {
        // given
        petRepository.saveAll(
                List.of(
                        createPet(false),
                        createPet(false),
                        createPet(true),
                        createPet(true)
                )
        );
        entityManager.clear();

        // when
        petService.resetAttendance();

        // then
        long notAttendedCount = petRepository.findAll()
                .stream()
                .filter(pet -> !pet.isAttended())
                .count();
        assertThat(notAttendedCount).isEqualTo(4);
    }

    private Pet createPet(boolean isAttend) {
        return Pet.builder()
                .memberId(1L)
                .stageId(1L)
                .isAttended(isAttend)
                .build();
    }

    @Test
    void 펫_스테이지_업데이트_성공() {
        // given
        Pet pet = TestFixture.createPetWithScore(member, firstStage.getId(), 49);
        petRepository.save(pet);

        TestTransaction.flagForCommit();
        TestTransaction.end();

        // when
        petService.increaseCurrentScore(member.getId(), 1);
        Pet updatedPet = petRepository.findById(pet.getId()).orElseThrow();
        Stage stage = stageRepository.findById(updatedPet.getStageId()).orElseThrow();

        // then
        assertThat(stage.getLevel()).isEqualTo(secondStage.getLevel());
    }

    @Test
    void 점수_부족_시_펫_스테이지_업데이트_실패() {
        // given
        Pet pet = TestFixture.createPetWithScore(member, firstStage.getId(), 48);
        petRepository.save(pet);

        TestTransaction.flagForCommit();
        TestTransaction.end();

        // when
        petService.increaseCurrentScore(member.getId(), 1);
        Pet updatedPet = petRepository.findById(pet.getId()).orElseThrow();
        Stage stage = stageRepository.findById(updatedPet.getStageId()).orElseThrow();

        // then
        assertThat(stage.getLevel()).isEqualTo(firstStage.getLevel());
    }
}
