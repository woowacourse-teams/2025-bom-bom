package me.bombom.api.v1.notice.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.notice.domain.NoticeCategory;
import me.bombom.api.v1.notice.repository.NoticeRepository;
import me.bombom.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
class NoticeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NoticeRepository noticeRepository;

    @BeforeEach
    void setUp() {
        noticeRepository.save(TestFixture.createNotice("공지1", NoticeCategory.UPDATE));
        noticeRepository.save(TestFixture.createNotice("공지2", NoticeCategory.EVENT));
        noticeRepository.save(TestFixture.createNotice("공지3", NoticeCategory.NOTICE));
    }

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
