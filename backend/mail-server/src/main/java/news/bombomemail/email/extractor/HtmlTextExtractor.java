package news.bombomemail.email.extractor;

import jakarta.mail.MessagingException;
import jakarta.mail.Part;
import jakarta.mail.internet.ContentType;
import jakarta.mail.internet.ParseException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HtmlTextExtractor implements ContentExtractor{

    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

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
        Object content = part.getContent();
        if (content instanceof String text) {
            return text; // 이미 문자열이면 그대로 반환 (CSS 포함)
        }
        return readBody(part);
    }

    /**
     *  대부분 content는 text로 나오지만 가끔 InputStream으로만 전달되는 본문을 파싱하기 위해서 필요
     *  문자셋을 직접 판별해 문자열로 복원한다.
     * @param part
     * @return
     * @throws MessagingException
     * @throws IOException
     */
    private String readBody(Part part) throws MessagingException, IOException {

        Charset charset = resolveCharset(part.getContentType());
        try (InputStream input = part.getInputStream()) {
            byte[] bytes = input.readAllBytes();
            return new String(bytes, charset);
        }
    }

    private Charset resolveCharset(String rawContentType) {
        if (rawContentType == null) {
            return DEFAULT_CHARSET;
        }
        try {
            ContentType contentType = new ContentType(rawContentType);
            String charsetName = contentType.getParameter("charset");
            if (charsetName == null || charsetName.isBlank()) {
                return DEFAULT_CHARSET;
            }
            return Charset.forName(charsetName);
        } catch (IllegalArgumentException | ParseException ex) {
            log.debug("알 수 없는 charset [{}], 기본값({}) 사용", rawContentType, DEFAULT_CHARSET);
            return DEFAULT_CHARSET;
        }
    }
}
