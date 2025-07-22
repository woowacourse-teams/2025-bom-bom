package me.bombom.api.v1.article.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import me.bombom.api.v1.TestFixture;
import org.junit.jupiter.api.Test;

class ArticleTest {

    private static final LocalDateTime baseTime = LocalDateTime.of(2025, 7, 15, 10, 0);

    @Test
    void 아티클의_읽기_상태를_true로_바꿀_수_있다() {
        //given
        Article article = TestFixture.createArticle("제목", 1L, 1L, baseTime);

        //when
        article.markAsRead();

        //then
        assertThat(article.isRead()).isTrue();
    }
}
