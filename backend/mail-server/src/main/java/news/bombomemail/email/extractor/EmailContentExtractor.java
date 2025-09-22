package news.bombomemail.email.extractor;

import jakarta.mail.MessagingException;
import jakarta.mail.Part;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EmailContentExtractor {

    private static final List<ContentExtractor> EXTRACTORS = List.of(
            new HtmlTextExtractor(),
            new PlainTextExtractor(),
            new AlternativeMultipartExtractor(),
            new MultipartExtractor(),
            new Rfc822Extractor()
    );

    public static String extractContents(MimeMessage msg) throws MessagingException, IOException {
        try {
            String text = extractTextFromPart(msg);
            return text == null ? "" : text.strip();
        } catch (MessagingException | IOException e) {
            log.warn("메일 본문 추출 중 예외 발생", e);
            throw e;
        }
    }

    public static String extractTextFromPart(Part part) throws MessagingException, IOException {
        for (ContentExtractor extractor : EXTRACTORS) {
            if (extractor.supports(part)) {
                return extractor.extract(part);
            }
        }
        return "";
    }
}
