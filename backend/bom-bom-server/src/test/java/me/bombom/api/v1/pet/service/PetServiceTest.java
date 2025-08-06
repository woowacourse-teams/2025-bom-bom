package me.bombom.api.v1.pet.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.List;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.common.config.QuerydslConfig;
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
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({PetService.class, QuerydslConfig.class})
class PetServiceTest {

    @Autowired
    private PetService petService;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private StageRepository stageRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Member member;
    private Stage stage;

    @BeforeEach
    void setUp() {
        member = TestFixture.normalMemberFixture();
        memberRepository.save(member);
        stage = TestFixture.createStage(1, 50);
        stageRepository.save(stage);
    }

    @Test
    void 키우기_정보_조회() {
        // given
        Pet pet = TestFixture.createPet(member, stage.getId());
        petRepository.save(pet);

        // when
        PetResponse result = petService.getPet(member);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.level()).isEqualTo(1);
            softly.assertThat(result.totalScore()).isEqualTo(50);
            softly.assertThat(result.currentScore()).isEqualTo(0);
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

        // when
        petService.resetAttendance();

        // then
        long notAttendedCount = petRepository.findAll()
                .stream()
                .filter(pet -> !pet.isAttended())
                .count();
        Assertions.assertThat(notAttendedCount).isEqualTo(4);
    }

    private Pet createPet(boolean isAttend) {
        return Pet.builder()
                .memberId(1L)
                .stageId(1L)
                .isAttended(isAttend)
                .build();
    }
}
