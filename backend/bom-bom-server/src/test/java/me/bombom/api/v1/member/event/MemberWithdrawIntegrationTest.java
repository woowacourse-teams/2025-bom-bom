package me.bombom.api.v1.member.event;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import me.bombom.api.v1.article.service.ArticleService;
import me.bombom.api.v1.bookmark.service.BookmarkService;
import me.bombom.api.v1.highlight.service.HighlightService;
import me.bombom.api.v1.pet.service.PetService;
import me.bombom.api.v1.reading.service.ReadingService;
import me.bombom.api.v1.subscribe.service.SubscribeService;
import me.bombom.api.v1.withdraw.event.DeleteArticlesByWithdrawListener;
import me.bombom.api.v1.withdraw.event.DeleteBookmarksByWithdrawListener;
import me.bombom.api.v1.withdraw.event.DeleteHighlightsByWithdrawListener;
import me.bombom.api.v1.withdraw.event.DeletePetByWithdrawListener;
import me.bombom.api.v1.withdraw.event.DeleteReadingsByWithdrawListener;
import me.bombom.api.v1.withdraw.event.DeleteSubscribeByWithdrawListener;
import me.bombom.api.v1.withdraw.event.WithdrawEvent;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Disabled("테스트 가이드 3단계에서 실제 DB 상태 검증 방식으로 재작성한다.")
@ExtendWith(MockitoExtension.class)
class MemberWithdrawIntegrationTest {

    @Mock
    private ReadingService readingService;

    @Mock
    private PetService petService;

    @Mock
    private ArticleService articleService;

    @Mock
    private BookmarkService bookmarkService;

    @Mock
    private HighlightService highlightService;

    @Mock
    private SubscribeService subscribeService;

    @Test
    void 회원_탈퇴_이벤트를_각_삭제_리스너에_전달한다() {
        Long memberId = 1L;
        WithdrawEvent event = new WithdrawEvent(memberId);

        new DeleteReadingsByWithdrawListener(readingService).on(event);
        new DeletePetByWithdrawListener(petService).on(event);
        new DeleteArticlesByWithdrawListener(articleService).on(event);
        new DeleteBookmarksByWithdrawListener(bookmarkService).on(event);
        new DeleteHighlightsByWithdrawListener(highlightService).on(event);
        new DeleteSubscribeByWithdrawListener(subscribeService).on(event);

        verify(readingService, times(1)).deleteAllByMemberId(memberId);
        verify(petService, times(1)).deleteByMemberId(memberId);
        verify(articleService, times(1)).deleteAllByMemberId(memberId);
        verify(bookmarkService, times(1)).deleteAllByMemberId(memberId);
        verify(highlightService, times(1)).deleteAllByMemberId(memberId);
        verify(subscribeService, times(1)).deleteAllByMemberId(memberId);
    }
}
