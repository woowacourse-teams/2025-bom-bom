package me.bombom.api.v1.notice.service;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.List;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.notice.domain.NoticeCategory;
import me.bombom.api.v1.notice.dto.NoticeResponse;
import me.bombom.api.v1.notice.repository.NoticeCategoryRepository;
import me.bombom.api.v1.notice.repository.NoticeRepository;
import me.bombom.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
class NoticeServiceTest {

    @Autowired
    private NoticeService noticeService;

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

        noticeRepository.save(TestFixture.createNotice("공지1", category1.getId()));
        noticeRepository.save(TestFixture.createNotice("공지2", category2.getId()));
    }

    @Test
    @DisplayName("공지 목록 조회 시 카테고리 정보와 함께 반환한다.")
    void getNoticesReturnsListWithCategory() {
        // when
        List<NoticeResponse> responses = noticeService.getNotices();

        // then
        assertSoftly(softly -> {
                    softly.assertThat(responses).hasSize(2);
                    softly.assertThat(responses.get(0).title()).isEqualTo("공지2");
                    softly.assertThat(responses.get(1).title()).isEqualTo("공지1");
                }
        );
    }
}
