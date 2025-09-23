package me.bombom.api.v1.withdraw.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.article.service.ArticleService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteArticlesByWithdrawListener {

    private final ArticleService articleService;

    @TransactionalEventListener
    public void on(WithdrawEvent event) {
        log.info("회원 탈퇴 따른 아티클 삭제 시작 - memberId={}", event.memberId());
        try {
            articleService.deleteAllByMemberId(event.memberId());
        } catch (Exception e) {
            log.error("회원 탈퇴 따른 아티클 삭제 처리 실패 - memberId={}", event.memberId(), e);
        }
    }
}
