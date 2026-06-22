package me.bombom.api.v1.notice.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import me.bombom.support.acceptance.AcceptanceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@AcceptanceTest("acceptance/notice/get-notices.json")
class NoticeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void 공지_목록을_생성일_내림차순과_ID_오름차순으로_페이지네이션하여_조회한다() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/notices")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].title").value("공지3"))
                .andExpect(jsonPath("$.content[1].title").value("공지2"))
                .andExpect(jsonPath("$.content[2].title").value("공지1"))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.sort.sorted").value(true));
    }
}
