package news.bombomemail.email.util;

import jakarta.mail.BodyPart;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EmailContentExtractor {

    public static String extractContents(MimeMessage msg) throws MessagingException, IOException {
        try {
            String text = extractTextFromPart(msg);
            return text == null ? "" : text.trim();
        } catch (MessagingException | IOException e) {
            log.warn("메일 본문 추출 중 예외 발생", e);
            throw e;
        }
    }

    private static String extractTextFromPart(Part part) throws MessagingException, IOException {
        if (part.isMimeType("text/html")) {
            return (String) part.getContent();
        }

        if (part.isMimeType("text/plain")) {
            return (String) part.getContent();
        }

        if (part.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) part.getContent();
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bp = multipart.getBodyPart(i);
                String result = extractTextFromPart(bp);
                if (result != null && !result.isBlank()) {
                    return result;
                }
            }
        }
        return "";
    }
}
