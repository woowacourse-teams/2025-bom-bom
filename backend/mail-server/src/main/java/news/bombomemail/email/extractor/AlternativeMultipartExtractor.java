package news.bombomemail.email.extractor;

import jakarta.mail.BodyPart;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import java.io.IOException;

public class AlternativeMultipartExtractor implements ContentExtractor {

    @Override
    public boolean supports(Part part) throws MessagingException {
        return part.isMimeType("multipart/alternative");
    }

    @Override
    public String extract(Part part) throws MessagingException, IOException {
        Multipart mp = (Multipart) part.getContent();
        String plain = null;

        for (int i = mp.getCount() - 1; i >= 0; i--) {
            BodyPart bp = mp.getBodyPart(i);
            if (bp.isMimeType("text/html")) {
                String html = EmailContentExtractor.extractTextFromPart(bp);
                if (html != null && !html.isBlank()) {
                    return html;
                }
            } else if (bp.isMimeType("text/plain")) {
                if (plain == null) {
                    plain = EmailContentExtractor.extractTextFromPart(bp);
                }
            } else {
                String other = EmailContentExtractor.extractTextFromPart(bp);
                if (other != null && !other.isBlank()) {
                    return other;
                }
            }
        }
        return plain == null ? "" : plain;
    }
}
