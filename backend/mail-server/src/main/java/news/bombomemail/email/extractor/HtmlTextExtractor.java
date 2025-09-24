package news.bombomemail.email.extractor;

import jakarta.mail.MessagingException;
import jakarta.mail.Part;
import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

public class HtmlTextExtractor implements ContentExtractor{

    @Override
    public boolean supports(Part part) throws MessagingException {
        return part.isMimeType("text/html");
    }

    @Override
    public int getOrder() {
        return 1;
    }

    @Override
    public String extract(Part part) throws MessagingException, IOException {
        return Jsoup.clean((String) part.getContent(), Safelist.basic());
    }
}
