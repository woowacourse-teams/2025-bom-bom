package me.bombom.api.v1.notice.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.notice.domain.NoticeCategory;
import me.bombom.api.v1.notice.repository.NoticeCategoryRepository;
import me.bombom.api.v1.notice.repository.NoticeRepository;
import me.bombom.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@AutoConfigureMockMvc
class NoticeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NoticeRepository noticeRepository;

    @Autowired
    private NoticeCategoryRepository noticeCategoryRepository;

    @BeforeEach
    void setUp() {
        noticeRepository.deleteAllInBatch();
        noticeCategoryRepository.deleteAllInBatch();

        NoticeCategory category1 = noticeCategoryRepository.save(TestFixture.createNoticeCategory("점검"));
        NoticeCategory category2 = noticeCategoryRepository.save(TestFixture.createNoticeCategory("이벤트"));

        var notice1 = TestFixture.createNotice("공지1", category1.getId());
        var notice2 = TestFixture.createNotice("공지2", category2.getId());

        ReflectionTestUtils.setField(notice1, "createdAt", LocalDateTime.now().minusDays(2));
        ReflectionTestUtils.setField(notice2, "createdAt", LocalDateTime.now().minusDays(1));

        noticeRepository.save(notice1);
        noticeRepository.save(notice2);
    }

    @Test
    @DisplayName("공지 목록을 조회하면 카테고리, 제목, 내용, 날짜가 반환된다")
    void getNotices() throws Exception {
        mockMvc.perform(get("/api/v1/notices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("공지2"))
                .andExpect(jsonPath("$[1].title").value("공지1"))
                .andExpect(jsonPath("$[0].categoryName").value("이벤트"))
                .andExpect(jsonPath("$[1].categoryName").value("점검"));
    }
}
