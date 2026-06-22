package me.bombom.api.v1.guidemail.controller;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static me.bombom.support.acceptance.AcceptanceTestHeaders.MEMBER_ID;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import me.bombom.support.acceptance.AcceptanceTest;
import me.bombom.support.acceptance.ResetsAcceptanceData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

@AcceptanceTest({
        "acceptance/common/member.json",
        "acceptance/guidemail/read-guide-mail.json"
})
class GuideMailControllerTest {

    private static final long MEMBER_ID_VALUE = 1L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @ResetsAcceptanceData
    void 가이드_메일을_읽으면_키우기_점수와_읽기_횟수가_증가한다() throws Exception {
        int currentScore = value("select current_score from pet where member_id = ?");
        int currentTodayCount = value("select current_count from today_reading where member_id = ?");
        int currentReadCount = value("select read_count from today_reading where member_id = ?");
        int currentWeeklyCount = value("select current_count from weekly_reading where member_id = ?");
        int currentContinueDayCount = value("select day_count from continue_reading_realtime where member_id = ?");

        mockMvc.perform(patch("/api/v1/guide/read")
                        .header(MEMBER_ID, MEMBER_ID_VALUE))
                .andExpect(status().isNoContent());

        assertSoftly(softly -> {
            softly.assertThat(value("select current_score from pet where member_id = ?")).isGreaterThan(currentScore);
            softly.assertThat(value("select current_count from today_reading where member_id = ?"))
                    .isGreaterThan(currentTodayCount);
            softly.assertThat(value("select read_count from today_reading where member_id = ?"))
                    .isGreaterThan(currentReadCount);
            softly.assertThat(value("select current_count from weekly_reading where member_id = ?"))
                    .isGreaterThan(currentWeeklyCount);
            softly.assertThat(value("select day_count from continue_reading_realtime where member_id = ?"))
                    .isGreaterThan(currentContinueDayCount);
        });
    }

    private int value(String sql) {
        return jdbcTemplate.queryForObject(sql, Integer.class, MEMBER_ID_VALUE);
    }
}
