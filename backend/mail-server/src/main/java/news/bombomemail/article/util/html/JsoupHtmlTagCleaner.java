package news.bombomemail.article.util.html;

import org.jsoup.Jsoup;

public class JsoupHtmlTagCleaner implements HtmlTagCleaner {

    @Override
    public String clean(String html) {
        return Jsoup.parse(html).text();
    }
}
