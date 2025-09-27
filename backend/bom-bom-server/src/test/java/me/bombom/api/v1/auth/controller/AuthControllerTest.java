package me.bombom.api.v1.auth.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.dto.request.MemberSignupRequest;
import me.bombom.api.v1.member.enums.Gender;
import me.bombom.api.v1.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Member member;

    @BeforeEach
    void setUp() {
        memberRepository.deleteAllInBatch();
        member = TestFixture.normalMemberFixture();
        memberRepository.save(member);
    }

    @Test
    void 닉네임_중복_체크_TRUE() throws Exception {
        mockMvc.perform(get("/api/v1/auth/signup/check")
                        .param("field", "NICKNAME")
                        .param("userInput", member.getNickname())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void 닉네임_중복_체크_FALSE() throws Exception {
        mockMvc.perform(get("/api/v1/auth/signup/check")
                        .param("field", "nickname")
                        .param("userInput", "anotherNickname")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void 이메일_중복_체크_TRUE() throws Exception {
        mockMvc.perform(get("/api/v1/auth/signup/check")
                        .param("field", "EMAIL")
                        .param("userInput", member.getEmail())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void 유효하지_않은_필드_입력_시_예외() throws Exception {
        mockMvc.perform(get("/api/v1/auth/signup/check")
                        .param("field", "INVALID")
                        .param("userInput", member.getEmail())
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

