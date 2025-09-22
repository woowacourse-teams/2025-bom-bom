package news.bombomemail.email.extractor;

import jakarta.mail.MessagingException;
import jakarta.mail.Part;
import java.io.IOException;

public interface ContentExtractor {

    boolean supports(Part part) throws MessagingException;

    String extract(Part part) throws MessagingException, IOException;
}
