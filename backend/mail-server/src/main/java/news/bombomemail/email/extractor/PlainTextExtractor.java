package news.bombomemail.email.extractor;

import jakarta.mail.MessagingException;
import jakarta.mail.Part;
import java.io.IOException;

public class PlainTextExtractor implements ContentExtractor{
    @Override
    public boolean supports(Part part) throws MessagingException {
        return part.isMimeType("text/plain");
    }

    @Override
    public String extract(Part part) throws MessagingException, IOException {
        return (String) part.getContent();
    }
}
