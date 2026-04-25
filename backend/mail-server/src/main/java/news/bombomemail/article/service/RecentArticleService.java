package news.bombomemail.article.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import news.bombomemail.article.domain.RecentArticle;
import news.bombomemail.article.repository.RecentArticleRepository;
import news.bombomemail.article.util.ReadingTimeCalculator;
import news.bombomemail.article.util.SummaryGenerator;
import news.bombomemail.article.util.html.HtmlTagCleaner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecentArticleService {

    private final HtmlTagCleaner htmlTagCleaner;
    private final RecentArticleRepository recentArticleRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(
            Long articleId,
            String articleTitle,
            String contents,
            Long memberId,
            Long newsletterId
    ) {
        recentArticleRepository.save(buildRecentArticle(articleId, articleTitle, contents, memberId, newsletterId));
    }

    private RecentArticle buildRecentArticle(
            Long articleId,
            String articleTitle,
            String contents,
            Long memberId,
            Long newsletterId
    ) {
        return RecentArticle.builder()
                .articleId(articleId)
                .title(articleTitle)
                .contents(contents)
                .contentsText(htmlTagCleaner.clean(contents))
                .expectedReadTime(ReadingTimeCalculator.calculate(contents))
                .contentsSummary(SummaryGenerator.summarize(contents))
                .memberId(memberId)
                .newsletterId(newsletterId)
                .arrivedDateTime(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .build();
    }
}
