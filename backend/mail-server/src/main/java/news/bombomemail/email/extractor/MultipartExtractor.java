package news.bombomemail.email.extractor;

import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import java.io.IOException;

public class MultipartExtractor implements ContentExtractor{

    @Override
    public boolean supports(Part part) throws MessagingException {
        return part.isMimeType("multipart/*");
    }

    @Override
    public String extract(Part part) throws MessagingException, IOException {
        Multipart multipart = (Multipart) part.getContent();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < multipart.getCount(); i++) {
            String text = EmailContentExtractor.extractTextFromPart(multipart.getBodyPart(i));
            if (text != null && !text.isBlank()) {
                sb.append(text).append("\n");
            }
        }
        return sb.toString().trim();
    }
}
