package news.bombomemail.email.extractor;

import jakarta.mail.MessagingException;
import jakarta.mail.Part;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;

public class Rfc822Extractor implements ContentExtractor{

    @Override
    public boolean supports(Part part) throws MessagingException {
        return part.isMimeType("message/rfc822");
    }

    @Override
    public int getOrder() {
        return 5;
    }

    @Override
    public String extract(Part part) throws MessagingException, IOException {
        Object inner = part.getContent();
        if (inner instanceof MimeMessage) {
            return EmailContentExtractor.extractTextFromPart((MimeMessage) inner);
        }
        return "";
    }
}
