package me.bombom.api.v1.pet.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import jakarta.persistence.EntityManager;
import java.util.List;
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
import me.bombom.support.integration.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

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
        member = TestFixture.createUniqueMember("펫테스트회원", "pet-service-test");
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
    void 키우기_정보_조회_시_다음_성장_단계가_없을_경우_에러() {
        // given
        Pet pet = TestFixture.createPetWithScore(member, secondStage.getId(), secondStage.getRequiredScore());
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
        Pet updatedPet = petRepository.findById(pet.getId()).orElseThrow();
        assertSoftly(softly -> {
                    softly.assertThat(updatedPet.getCurrentScore()).isEqualTo(5);
                    softly.assertThat(updatedPet.isAttended()).isTrue();
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
    void 이미_출석한_펫은_출석할_수_없다() {
        // given
        Pet pet = Pet.builder()
                .memberId(member.getId())
                .stageId(firstStage.getId())
                .isAttended(true)
                .build();
        petRepository.save(pet);

        // when & then
        assertThatThrownBy(() -> petService.attend(member))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.FORBIDDEN_RESOURCE);
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
        petRepository.saveAndFlush(pet);
        entityManager.clear();

        // when
        petService.increaseCurrentScore(member.getId(), 1);
        Pet updatedPet = petRepository.findById(pet.getId()).orElseThrow();
        Stage stage = stageRepository.findById(updatedPet.getStageId()).orElseThrow();

        // then
        assertThat(stage.getLevel()).isEqualTo(secondStage.getLevel());
    }

    @Test
    void 가이드메일_점수_반영은_점수를_증가시키고_스테이지를_갱신한다() {
        // given
        Pet pet = TestFixture.createPetWithScore(member, firstStage.getId(), 49);
        petRepository.saveAndFlush(pet);
        entityManager.clear();

        // when
        petService.increaseCurrentScoreForGuideMail(member.getId(), 1);

        // then
        Pet updatedPet = petRepository.findById(pet.getId()).orElseThrow();
        assertSoftly(softly -> {
            softly.assertThat(updatedPet.getCurrentScore()).isEqualTo(50);
            softly.assertThat(updatedPet.getStageId()).isEqualTo(secondStage.getId());
        });
    }

    @Test
    void 가이드메일_점수_반영_시_펫이_없으면_에러() {
        // when & then
        assertThatThrownBy(() -> petService.increaseCurrentScoreForGuideMail(member.getId(), 1))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.ENTITY_NOT_FOUND);
    }

    @Test
    void 점수_증가_시_펫이_없으면_에러() {
        // when & then
        assertThatThrownBy(() -> petService.increaseCurrentScore(member.getId(), 1))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.ENTITY_NOT_FOUND);
    }

    @Test
    void 점수_부족_시_펫_스테이지_업데이트_실패() {
        // given
        Pet pet = TestFixture.createPetWithScore(member, firstStage.getId(), 48);
        petRepository.saveAndFlush(pet);
        entityManager.clear();

        // when
        petService.increaseCurrentScore(member.getId(), 1);
        Pet updatedPet = petRepository.findById(pet.getId()).orElseThrow();
        Stage stage = stageRepository.findById(updatedPet.getStageId()).orElseThrow();

        // then
        assertThat(stage.getLevel()).isEqualTo(firstStage.getLevel());
    }

    @Test
    void 신규_펫을_생성한다() {
        // when
        petService.createPet(member.getId());

        // then
        Pet pet = petRepository.findByMemberId(member.getId()).orElseThrow();
        assertSoftly(softly -> {
            softly.assertThat(pet.getMemberId()).isEqualTo(member.getId());
            softly.assertThat(pet.getStageId()).isEqualTo(1L);
            softly.assertThat(pet.getCurrentScore()).isZero();
            softly.assertThat(pet.isAttended()).isFalse();
        });
    }

    @Test
    void 회원의_펫을_삭제한다() {
        // given
        Pet pet = TestFixture.createPet(member, firstStage.getId());
        petRepository.save(pet);

        // when
        petService.deleteByMemberId(member.getId());

        // then
        assertThat(petRepository.findByMemberId(member.getId())).isEmpty();
    }
}
