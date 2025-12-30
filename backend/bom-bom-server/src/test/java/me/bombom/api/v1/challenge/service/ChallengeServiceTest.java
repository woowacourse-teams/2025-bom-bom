package me.bombom.api.v1.challenge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDate;
import me.bombom.api.v1.challenge.domain.Challenge;
import me.bombom.api.v1.challenge.dto.response.ChallengeInfoResponse;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.support.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
class ChallengeServiceTest {

    @Autowired
    private ChallengeService challengeService;

    @Autowired
    private ChallengeRepository challengeRepository;

    @Test
    @DisplayName("챌린지 상세 정보를 조회할 수 있다.")
    void getChallengeInfo() {
        // given
        Challenge challenge = Challenge.builder()
                .name("챌린지1")
                .generation(1)
                .startDate(LocalDate.of(2026, 1, 5))
                .endDate(LocalDate.of(2026, 2, 4))
                .totalDays(31)
                .build();
        challengeRepository.save(challenge);

        // when
        ChallengeInfoResponse response = challengeService.getChallengeInfo(challenge.getId());

        // then
        assertSoftly(softly -> {
            assertThat(response.name()).isEqualTo("챌린지1");
            assertThat(response.startDate()).isEqualTo(LocalDate.of(2026, 1, 5));
            assertThat(response.endDate()).isEqualTo(LocalDate.of(2026, 2, 4));
            assertThat(response.generation()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("존재하지 않는 챌린지 ID로 조회 시 예외가 발생한다.")
    void getChallengeInfoWithInvalidId() {
        // when & then
        assertThatThrownBy(() -> challengeService.getChallengeInfo(0L))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasMessage(ErrorDetail.ENTITY_NOT_FOUND.getMessage());
    }
}
