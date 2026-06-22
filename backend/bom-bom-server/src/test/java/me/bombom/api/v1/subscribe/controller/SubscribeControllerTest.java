package me.bombom.api.v1.subscribe.controller;

import static me.bombom.support.acceptance.AcceptanceTestHeaders.MEMBER_ID;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import me.bombom.support.acceptance.AcceptanceTest;
import me.bombom.support.acceptance.ResetsAcceptanceData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

@AcceptanceTest({
        "acceptance/common/member.json",
        "acceptance/subscribe/subscription.json"
})
class SubscribeControllerTest {

    private static final long MEMBER_ID_VALUE = 1L;
    private static final long SUBSCRIPTION_ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void 인증된_사용자는_구독_목록을_조회할_수_있다() throws Exception {
        mockMvc.perform(get("/api/v1/members/me/subscriptions").header(MEMBER_ID, MEMBER_ID_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].subscriptionId").value(SUBSCRIPTION_ID))
                .andExpect(jsonPath("$[0].name").value("테스트 뉴스레터"));
    }

    @Test
    void 인증되지_않은_사용자는_403을_반환한다() throws Exception {
        mockMvc.perform(get("/api/v1/members/me/subscriptions"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @ResetsAcceptanceData
    void 인증된_사용자는_구독_취소를_요청할_수_있다() throws Exception {
        mockMvc.perform(post("/api/v1/members/me/subscriptions/{id}/unsubscribe", SUBSCRIPTION_ID)
                        .header(MEMBER_ID, MEMBER_ID_VALUE))
                .andExpect(status().isOk());
    }
}
