package news.bombomemail.article.util.html;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

@Slf4j
public class FailoverHtmlTagCleaner implements HtmlTagCleaner {

    private final List<HtmlTagCleaner> cleaners;

    public FailoverHtmlTagCleaner(HtmlTagCleaner... cleaners) {
        if (cleaners == null || cleaners.length == 0) {
            throw new IllegalArgumentException("적어도 하나 이상의 HtmlTagCleaner가 필요합니다.");
        }
        this.cleaners = List.of(cleaners);
    }

    @Override
    public String clean(String html) {
        if (!StringUtils.hasText(html)) {
            return "";
        }

        for (HtmlTagCleaner cleaner : cleaners) {
            try {
                String cleaned = cleaner.clean(html);
                if (cleaned != null) {
                    return cleaned;
                }
                log.warn("{} 가 null을 반환해서 다음 후보로 넘어갑니다.", cleaner.getClass().getSimpleName());
            } catch (Exception e) {
                log.warn("Cleaner {} 실패", cleaner.getClass().getSimpleName(), e);
            }
        }

        log.warn("모든 cleaners 실패");
        return html;
    }
}
