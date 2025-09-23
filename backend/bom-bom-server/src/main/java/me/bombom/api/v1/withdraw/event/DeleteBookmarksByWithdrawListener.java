package me.bombom.api.v1.withdraw.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.bookmark.service.BookmarkService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteBookmarksByWithdrawListener {

    private final BookmarkService bookmarkService;

    @TransactionalEventListener
    public void on(WithdrawEvent event) {
        log.info("회원 탈퇴 따른 북마크 삭제 처리 시작 - memberId={}", event.memberId());
        try {
            bookmarkService.deleteAllByMemberId(event.memberId());
        } catch (Exception e) {
            log.error("회원 탈퇴 따른 북마크 삭제 처리 실패 - memberId={}", event.memberId(), e);
        }
    }
}
