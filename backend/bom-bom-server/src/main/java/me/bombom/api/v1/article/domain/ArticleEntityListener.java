package me.bombom.api.v1.article.domain;

import jakarta.persistence.PostPersist;
import jakarta.persistence.PostUpdate;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import me.bombom.api.v1.article.repository.SearchRecentRepository;

@Slf4j
@Component
public class ArticleEntityListener implements ApplicationContextAware {

    private static ApplicationContext applicationContext;
    private static final int RECENT_DAYS = 3;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        ArticleEntityListener.applicationContext = applicationContext;
    }

    @PostPersist
    @PostUpdate
    public void syncSearchRecent(Article article) {
        try {
            // 최근 3일 이내의 Article만 search_recent에 저장
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(RECENT_DAYS);
            if (article.getArrivedDateTime().isBefore(cutoffDate)) {
                log.debug("Article이 3일 이전이므로 search_recent에 저장하지 않음 - articleId={}", article.getId());
                return;
            }
            SearchRecentRepository searchRecentRepository = applicationContext.getBean(SearchRecentRepository.class);
            
            // 기존에 있으면 삭제 (중복 방지)
            searchRecentRepository.deleteByArticleId(article.getId());
            
            SearchRecent searchRecent = SearchRecent.from(article);
            searchRecentRepository.save(searchRecent);
            
            log.debug("search_recent 동기화 완료 - articleId={}", article.getId());
        } catch (Exception e) {
            log.error("search_recent 동기화 실패 - articleId={}", article.getId(), e);
            // 동기화 실패해도 Article 저장은 성공해야 하므로 예외를 던지지 않음
        }
    }
}
