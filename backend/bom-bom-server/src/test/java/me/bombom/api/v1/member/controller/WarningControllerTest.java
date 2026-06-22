package me.bombom.api.v1.member.controller;

import static me.bombom.support.acceptance.AcceptanceTestHeaders.MEMBER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import me.bombom.support.acceptance.AcceptanceTest;
import me.bombom.support.acceptance.ResetsAcceptanceData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

@AcceptanceTest({
        "acceptance/common/member.json",
        "acceptance/member/warning-setting.json"
})
class WarningControllerTest {

    private static final long MEMBER_ID_VALUE = 1L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void 임박_경고_설정을_조회한다() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/members/me/warning/near-capacity")
                        .header(MEMBER_ID, MEMBER_ID_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isVisible").value(true));
    }

    @Test
    @ResetsAcceptanceData
    void 임박_경고_설정을_수정한다() throws Exception {
        // when
        mockMvc.perform(post("/api/v1/members/me/warning/near-capacity")
                        .header(MEMBER_ID, MEMBER_ID_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "isVisible": false
                                }
                                """))
                .andExpect(status().isNoContent());

        // then
        Boolean visible = jdbcTemplate.queryForObject(
                "select is_visible from warning_setting where member_id = ?",
                Boolean.class,
                MEMBER_ID_VALUE
        );
        assertThat(visible).isFalse();
    }
}
