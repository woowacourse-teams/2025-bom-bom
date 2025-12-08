package news.bombomemail.article.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import news.bombomemail.article.domain.RecentArticle;
import news.bombomemail.article.repository.RecentArticleRepository;
import news.bombomemail.article.util.ReadingTimeCalculator;
import news.bombomemail.article.util.SummaryGenerator;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecentArticleService {

    private final RecentArticleRepository recentArticleRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(MimeMessage message, String contents, Long memberId, Long newsletterId)
            throws MessagingException {
        recentArticleRepository.save(buildRecentArticle(message, contents, memberId, newsletterId));
    }

    private RecentArticle buildRecentArticle(
            MimeMessage message,
            String contents,
            Long memberId,
            Long newsletterId
    ) throws MessagingException {
        String contentsText = extractTextFromHtml(contents);
        return RecentArticle.builder()
                .title(message.getSubject())
                .contents(contents)
                .contentsText(contentsText)
                .expectedReadTime(ReadingTimeCalculator.calculate(contents))
                .contentsSummary(SummaryGenerator.summarize(contents))
                .memberId(memberId)
                .newsletterId(newsletterId)
                .arrivedDateTime(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .build();
    }

    private String extractTextFromHtml(String htmlText) {
        if (!StringUtils.hasText(htmlText)) {
            return "";
        }
        return Jsoup.parse(htmlText).text().strip();
    }
}
