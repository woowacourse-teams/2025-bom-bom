package news.bombom.captcha.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import news.bombom.captcha.dto.request.CaptchaVerifyRequest;
import news.bombom.captcha.dto.response.CaptchaVerifyResponse;
import news.bombom.captcha.service.CaptchaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.auditing.AuditingHandler;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import news.bombom.NotificationServerApplication;

@WebMvcTest(CaptchaController.class)
@ContextConfiguration(classes = NotificationServerApplication.class)
class CaptchaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CaptchaService captchaService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMappingContext;

    @MockitoBean
    private AuditingHandler jpaAuditingHandler;

    @Test
    @DisplayName("캡차 검증 성공")
    void verify_success() throws Exception {
        // given
        CaptchaVerifyRequest request = new CaptchaVerifyRequest("valid-token");
        when(captchaService.verify(anyString(), anyString()))
                .thenReturn(CaptchaVerifyResponse.success());

        // when & then
        mockMvc.perform(post("/api/v1/notifications/capcha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.message").value("캡차 검증 성공"));
    }

    @Test
    @DisplayName("캡차 검증 실패")
    void verify_fail() throws Exception {
        // given
        CaptchaVerifyRequest request = new CaptchaVerifyRequest("invalid-token");
        when(captchaService.verify(anyString(), anyString()))
                .thenReturn(CaptchaVerifyResponse.fail("캡차 검증에 실패했습니다."));

        // when & then
        mockMvc.perform(post("/api/v1/notifications/capcha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.message").value("캡차 검증에 실패했습니다."));
    }

    @Test
    @DisplayName("캡차 토큰 없음 - validation 실패")
    void verify_blank_token() throws Exception {
        // given
        CaptchaVerifyRequest request = new CaptchaVerifyRequest("");

        // when & then
        mockMvc.perform(post("/api/v1/notifications/capcha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("캡차 토큰 null - validation 실패")
    void verify_null_token() throws Exception {
        // when & then
        mockMvc.perform(post("/api/v1/notifications/capcha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"gRecaptchaResponse\": null}"))
                .andExpect(status().isBadRequest());
    }
}
