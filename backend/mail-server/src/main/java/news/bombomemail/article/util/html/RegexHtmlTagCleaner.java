package news.bombomemail.article.util.html;

public class RegexHtmlTagCleaner implements HtmlTagCleaner {

    @Override
    public String clean(String html) {
        return html.replaceAll("<[^>]*>", "");
    }
}
