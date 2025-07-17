package me.bombom.api.v1.article.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class ArticleTest {

    private static final LocalDateTime baseTime = LocalDateTime.of(2025, 7, 15, 10, 0);

    @Test
    void 아티클의_읽기_상태를_true로_바꿀_수_있다() {
        //given
        Article article = Article.builder()
                .title("테스트")
                .contents("<h2>제목</h2>")
                .thumbnailUrl("http://thumbnail.com")
                .expectedReadTime(5)
                .contentsSummary("요약")
                .isRead(false)
                .memberId(1L)
                .newsletterId(1L)
                .arrivedDateTime(baseTime)
                .build();

        //when
        article.markAsRead();

        //then
        assertThat(article.isRead()).isTrue();
    }
}
