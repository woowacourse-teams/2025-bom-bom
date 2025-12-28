package me.bombom.api.v1.challenge.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import me.bombom.api.v1.challenge.dto.GetChallengeInfoResponse;
import me.bombom.api.v1.challenge.service.ChallengeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WithMockUser
@WebMvcTest(ChallengeController.class)
class ChallengeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChallengeService challengeService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void 챌린지_상세_정보를_요청할_수_있다() throws Exception {
        //given
        GetChallengeInfoResponse response = new GetChallengeInfoResponse(
                "챌린지1",
                LocalDate.of(2026, 1, 5),
                LocalDate.of(2026, 2, 4),
                1,
                24,
                19
        );

        given(challengeService.getChallengeInfo(1L))
                .willReturn(response);

        //when & then
        mockMvc.perform(get("/api/v1/challenge/{id}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("챌린지1"));
    }

    @Test
    void 챌린지_상세_정보를_요청시_음수_id면_400() throws Exception {
        //when & then
        mockMvc.perform(get("/api/v1/challenge/{id}", -1L))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}
