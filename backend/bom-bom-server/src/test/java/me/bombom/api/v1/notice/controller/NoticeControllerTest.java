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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@AutoConfigureMockMvc
class NoticeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NoticeRepository noticeRepository;

    @BeforeEach
    void setUp() {
        noticeRepository.deleteAllInBatch();

        noticeRepository.save(TestFixture.createNotice("공지1", NoticeCategory.UPDATE));
        noticeRepository.save(TestFixture.createNotice("공지2", NoticeCategory.EVENT));
        noticeRepository.save(TestFixture.createNotice("공지3", NoticeCategory.NOTICE));
    }

    @Test
    @DisplayName("공지 목록은 createdAt DESC, id ASC 정렬로 페이지네이션되어 반환된다")
    void getNotices() throws Exception {
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
