package me.bombom.api.v1.pet.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.article.service.ArticleService;
import me.bombom.api.v1.pet.service.PetService;
import me.bombom.api.v1.reading.service.ReadingService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class AddArticleScoreListener {

    private final ArticleService articleService;
    private final ReadingService readingService;
    private final PetService petService;

    @TransactionalEventListener
    public void on(AddArticleScoreEvent event){
        try {
            if(articleService.canAddArticleScore(event.getMemberId())) {
                int score = readingService.calculateArticleScore(event.getMemberId());
                petService.increaseCurrentScore(event.getMemberId(), score);
            }
        } catch (Exception e){
            log.error("아티클 점수 추가 실패, member: {}", event.getMemberId(), e);
        }
    }
}
