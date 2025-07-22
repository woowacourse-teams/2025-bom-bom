package me.bombom.api.v1.article.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import me.bombom.api.v1.TestFixture;
import org.junit.jupiter.api.Test;

class ArticleTest {

    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2025, 7, 15, 10, 0);

    @Test
    void 아티클의_읽기_상태를_true로_바꿀_수_있다() {
        //given
        Article article = TestFixture.createArticle("제목", 1L, 1L, baseTime);

        //when
        article.markAsRead();

        //then
        assertThat(article.isRead()).isTrue();
    }

    @Test
    void 아티클이_오늘_도착했으면_true를_반환한다() {
        // given
        Article article = TestFixture.createArticle(1L, 1L, LocalDateTime.now());

        // when
        boolean result = article.isArrivedToday();

        // then
        assertThat(result).isTrue();
    }

    @Test
    void 아티클이_오늘_도착하지_않았으면_false를_반환한다() {
        // given
        Article article = TestFixture.createArticle(1L, 1L, BASE_TIME);

        // when
        boolean result = article.isArrivedToday();

        // then
        assertThat(result).isFalse();
    }
}
