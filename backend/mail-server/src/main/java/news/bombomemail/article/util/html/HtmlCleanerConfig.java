package news.bombomemail.article.util.html;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HtmlCleanerConfig {

    @Bean
    public HtmlTagCleaner htmlTagCleaner() {
        return new FailoverHtmlTagCleaner(
                new JsoupHtmlTagCleaner(),
                new JFiveTextExtractor(),
                new RegexHtmlTagCleaner()
        );
    }
}
