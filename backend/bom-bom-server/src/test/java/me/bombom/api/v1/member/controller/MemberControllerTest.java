package me.bombom.api.v1.member.controller;

import static me.bombom.support.acceptance.AcceptanceTestHeaders.MEMBER_ID;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.bombom.api.v1.member.dto.request.MemberInfoUpdateRequest;
import me.bombom.support.acceptance.AcceptanceTest;
import me.bombom.support.acceptance.ResetsAcceptanceData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@AcceptanceTest({
        "acceptance/common/member.json",
        "acceptance/member/member.json"
})
class MemberControllerTest {

    private static final long MEMBER_ID_VALUE = 1L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void 형식에_맞지_않는_닉네임으로_변경_시도_시_400_예외가_발생한다() throws Exception {
        // given
        MemberInfoUpdateRequest request = new MemberInfoUpdateRequest("invalid..nickname", null, null, null);
        String requestBody = objectMapper.writeValueAsString(request);

        // when & then
        mockMvc.perform(patch("/api/v1/members/me")
                        .header(MEMBER_ID, MEMBER_ID_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void 너무_짧은_닉네임으로_변경_시도_시_400_예외가_발생한다() throws Exception {
        // given
        MemberInfoUpdateRequest request = new MemberInfoUpdateRequest("a", null, null, null);
        String requestBody = objectMapper.writeValueAsString(request);

        // when & then
        mockMvc.perform(patch("/api/v1/members/me")
                        .header(MEMBER_ID, MEMBER_ID_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @ResetsAcceptanceData
    void 형식에_맞는_닉네임이면_정상_동작한다() throws Exception {
        // given
        MemberInfoUpdateRequest request = new MemberInfoUpdateRequest("new.nickname", null, null, null);
        String requestBody = objectMapper.writeValueAsString(request);

        // when & then
        mockMvc.perform(patch("/api/v1/members/me")
                        .header(MEMBER_ID, MEMBER_ID_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("new.nickname"))
                .andDo(print());
    }

    @Test
    void 회원_정보를_조회한다() throws Exception {
        mockMvc.perform(get("/api/v1/members/me")
                        .header(MEMBER_ID, MEMBER_ID_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("인수테스트회원"))
                .andExpect(jsonPath("$.email").value("acceptance@bombom.news"));
    }

    @Test
    void 회원_프로필을_조회한다() throws Exception {
        mockMvc.perform(get("/api/v1/members/me/profile")
                        .header(MEMBER_ID, MEMBER_ID_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("인수테스트회원"));
    }

    @Test
    void 이미_사용중인_닉네임으로_변경하면_예외가_발생한다() throws Exception {
        MemberInfoUpdateRequest request = new MemberInfoUpdateRequest("duplicated", null, null, null);

        mockMvc.perform(patch("/api/v1/members/me")
                        .header(MEMBER_ID, MEMBER_ID_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
