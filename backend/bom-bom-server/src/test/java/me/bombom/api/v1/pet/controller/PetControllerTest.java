package me.bombom.api.v1.pet.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import java.util.UUID;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.auth.dto.CustomOAuth2User;
import me.bombom.api.v1.auth.handler.OAuth2LoginSuccessHandler;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.pet.domain.Pet;
import me.bombom.api.v1.pet.domain.Stage;
import me.bombom.api.v1.pet.repository.PetRepository;
import me.bombom.api.v1.pet.repository.StageRepository;
import me.bombom.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@AutoConfigureMockMvc
class PetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private StageRepository stageRepository;

    @MockitoBean
    private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    private Member member;
    private Pet pet;
    private OAuth2AuthenticationToken authToken;

    @BeforeEach
    void setUp() {
        petRepository.deleteAllInBatch();
        stageRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();

        member = memberRepository.save(TestFixture.createUniqueMember(uniqueValue(), uniqueValue()));
        Stage firstStage = stageRepository.save(TestFixture.createStage(1, 0));
        stageRepository.save(TestFixture.createStage(2, 50));
        pet = petRepository.save(TestFixture.createPet(member, firstStage.getId()));

        Map<String, Object> attributes = Map.of(
                "id", member.getId().toString(),
                "email", member.getEmail(),
                "name", member.getNickname()
        );
        CustomOAuth2User customOAuth2User = new CustomOAuth2User(attributes, member, null, null);
        authToken = new OAuth2AuthenticationToken(
                customOAuth2User,
                customOAuth2User.getAuthorities(),
                "registrationId"
        );
    }

    @Test
    @DisplayName("펫 정보 조회 시 생성 인터페이스 경로를 통해 응답 DTO가 반환된다")
    void 펫_정보_조회() throws Exception {
        mockMvc.perform(get("/api/v1/members/me/pet")
                        .with(authentication(authToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.level").value(1))
                .andExpect(jsonPath("$.currentStageScore").value(0))
                .andExpect(jsonPath("$.requiredStageScore").value(50))
                .andExpect(jsonPath("$.isAttended").value(false));
    }

    @Test
    @DisplayName("출석 점수 부여 시 DB 상태가 함께 갱신된다")
    void 출석_점수_부여() throws Exception {
        mockMvc.perform(post("/api/v1/members/me/pet/attendance")
                        .with(authentication(authToken)))
                .andExpect(status().isOk());

        Pet updatedPet = petRepository.findById(pet.getId()).orElseThrow();
        assertThat(updatedPet.getCurrentScore()).isEqualTo(5);
        assertThat(updatedPet.isAttended()).isTrue();
    }

    private String uniqueValue() {
        return UUID.randomUUID().toString().substring(0, 20);
    }
}
