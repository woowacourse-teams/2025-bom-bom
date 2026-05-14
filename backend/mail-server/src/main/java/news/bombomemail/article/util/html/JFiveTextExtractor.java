package news.bombomemail.article.util.html;

import ch.digitalfondue.jfiveparse.Document;
import ch.digitalfondue.jfiveparse.Element;
import ch.digitalfondue.jfiveparse.JFiveParse;
import ch.digitalfondue.jfiveparse.Node;
import java.io.StringReader;
import java.util.List;

public class JFiveTextExtractor implements HtmlTagCleaner {

    @Override
    public String clean(String html) {
        Document doc = JFiveParse.parse(new StringReader(html));
        removeTag(doc, "script");
        removeTag(doc, "style");
        return doc.getDocumentElement().getTextContent().trim();
    }

    // style과 script 태그 안의 내용은 텍스트로 취급 할 필요가 없음 / JFive만 제대로 되지 않음
    private void removeTag(Document doc, String tag) {
        List<Element> nodes = doc.getElementsByTagName(tag);
        for (int i = nodes.size() - 1; i >= 0; i--) {
            Node node = nodes.get(i);
            Node parent = node.getParentNode();
            if (parent != null) {
                parent.removeChild(node);
            }
        }
    }
}
