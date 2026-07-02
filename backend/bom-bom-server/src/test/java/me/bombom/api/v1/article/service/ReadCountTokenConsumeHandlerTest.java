package me.bombom.api.v1.article.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import me.bombom.api.v1.reading.service.ReadRateLimitService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.TransientDataAccessResourceException;

@ExtendWith(MockitoExtension.class)
class ReadCountTokenConsumeHandlerTest {

    private static final Long MEMBER_ID = 1L;
    private static final Long ARTICLE_ID = 10L;
    private static final LocalDateTime READ_AT = LocalDateTime.of(2026, 1, 1, 10, 0);

    @Mock
    private ReadRateLimitService readRateLimitService;

    @Test
    void 읽기_토큰_소비_결과를_그대로_반환한다() {
        // given
        ReadCountTokenConsumeHandler handler = new ReadCountTokenConsumeHandler(readRateLimitService);
        when(readRateLimitService.tryConsumeReadCountToken(MEMBER_ID, READ_AT)).thenReturn(false);

        // when
        boolean result = handler.consume(MEMBER_ID, ARTICLE_ID, READ_AT);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void 읽기_토큰_소비_중_일시적_DB_예외가_발생하면_카운트_가능으로_처리한다() {
        // given
        ReadCountTokenConsumeHandler handler = new ReadCountTokenConsumeHandler(readRateLimitService);
        when(readRateLimitService.tryConsumeReadCountToken(MEMBER_ID, READ_AT))
                .thenThrow(new TransientDataAccessResourceException("DB 일시 장애"));

        // when
        boolean result = handler.consume(MEMBER_ID, ARTICLE_ID, READ_AT);

        // then
        assertThat(result).isTrue();
    }
}
