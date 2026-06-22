package me.bombom.api.v1.auth.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.bombom.api.v1.auth.enums.SignupValidateField;
import me.bombom.api.v1.auth.enums.SignupValidateStatus;
import me.bombom.api.v1.member.dto.request.MemberSignupRequest;
import me.bombom.api.v1.member.enums.Gender;
import me.bombom.support.acceptance.AcceptanceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@AcceptanceTest("acceptance/common/member.json")
class AuthControllerTest {

    private static final String EXISTING_NICKNAME = "인수테스트회원";
    private static final String EXISTING_EMAIL = "acceptance@bombom.news";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void 닉네임_중복_체크_DUPLICATE() throws Exception {
        mockMvc.perform(get("/api/v1/auth/signup/check")
                        .param("field", SignupValidateField.NICKNAME.name())
                        .param("userInput", EXISTING_NICKNAME)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(SignupValidateStatus.DUPLICATE.name()));
    }

    @Test
    void 닉네임_형식_체크_INVALID_FORMAT() throws Exception {
        mockMvc.perform(get("/api/v1/auth/signup/check")
                        .param("field", SignupValidateField.NICKNAME.name())
                        .param("userInput", "bombom..bombom")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(SignupValidateStatus.INVALID_FORMAT.name()));
    }

    @Test
    void 회원가입_닉네임_체크_OK() throws Exception {
        mockMvc.perform(get("/api/v1/auth/signup/check")
                        .param("field", SignupValidateField.NICKNAME.name())
                        .param("userInput", "anotherNickname"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(SignupValidateStatus.OK.name()));
    }

    @Test
    void 이메일_중복_체크_DUPLICATE() throws Exception {
        mockMvc.perform(get("/api/v1/auth/signup/check")
                        .param("field", SignupValidateField.EMAIL.name())
                        .param("userInput", EXISTING_EMAIL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(SignupValidateStatus.DUPLICATE.name()));
    }

    @Test
    void 유효하지_않은_필드_입력_시_예외() throws Exception {
        mockMvc.perform(get("/api/v1/auth/signup/check")
                        .param("field", "INVALID")
                        .param("userInput", EXISTING_EMAIL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void gender_none일_시_NONE으로_자동세팅() throws Exception {
        String json = """
                {
                    "nickname": "newUser",
                    "email": "newuser123@bombom.news",
                    "birthDate": "2000-01-01"
                }
                """;

        MemberSignupRequest request = objectMapper.readValue(json, MemberSignupRequest.class);
        assertThat(Gender.NONE).isEqualTo(request.gender());
    }
}
