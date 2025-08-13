package news.bombomemail.email.util;

import jakarta.mail.BodyPart;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EmailContentExtractor {

    private static final String[] BLOCK_TAGS = {
            "address","article","aside","blockquote","div","dl","fieldset","figcaption","figure","footer","form",
            "h1","h2","h3","h4","h5","h6","header","hr","li","main","nav","ol","p","pre","section",
            "table","thead","tbody","tfoot","tr","td","th","ul"
    };

    private static String selBlocksFollowedByBr() {
        return Arrays.stream(BLOCK_TAGS).map(t -> t + " + br").collect(Collectors.joining(","));
    }

    private static String selBrFollowedByBlocks() {
        return Arrays.stream(BLOCK_TAGS).map(t -> "br + " + t).collect(Collectors.joining(","));
    }

    public static String extractContents(MimeMessage msg) throws MessagingException, IOException {
        try {
            log.debug("메일 본문 추출 시작 - Subject: {}", msg.getSubject());
            
            String text = extractTextFromPart(msg);
            if (text == null) {
                log.debug("추출된 텍스트가 null임");
                return "";
            }
            
            log.debug("추출된 원본 텍스트 길이: {} chars", text.length());
            
            boolean isHtml = looksLikeHtml(text);
            log.debug("HTML 판별 결과: {}", isHtml);
            
            String result = isHtml ? cleanHtmlBrs(text) : text.strip();
            log.debug("최종 처리된 텍스트 길이: {} chars", result.length());
            
            return result;
        } catch (MessagingException | IOException e) {
            log.warn("메일 본문 추출 중 예외 발생", e);
            throw e;
        }
    }

    private static boolean looksLikeHtml(String text) {
        if (text == null) {
            return false;
        }
        String lowerText = text.toLowerCase();
        return lowerText.contains("<html")
                || lowerText.contains("<div")
                || lowerText.contains("<p")
                || lowerText.contains("<br");
    }

    private static String cleanHtmlBrs(String html) {
        try {
            log.debug("HTML 정리 시작 - 원본 길이: {} chars", html.length());
            
            Document doc = Jsoup.parse(html);
            doc.outputSettings().prettyPrint(false);

            // 1) 블록 경계에 낀 br 제거
            int removedBlockBrs = doc.select(selBlocksFollowedByBr()).size() + 
                                 doc.select(selBrFollowedByBlocks()).size();
            doc.select(selBlocksFollowedByBr()).remove();
            doc.select(selBrFollowedByBlocks()).remove();
            if (removedBlockBrs > 0) {
                log.debug("블록 경계 br {} 개 제거", removedBlockBrs);
            }

            // 2) 빈 단락 제거
            int removedEmptyParagraphs = doc.select("div:has(> br:only-child), p:has(> br:only-child)").size();
            doc.select("div:has(> br:only-child), p:has(> br:only-child)").remove();
            if (removedEmptyParagraphs > 0) {
                log.debug("빈 단락 {} 개 제거", removedEmptyParagraphs);
            }

            // 3) 연속 br 축약 (한 번에 전체 제거, 안정화될 때까지)
            int brRemovalCycles = 0;
            while (!doc.select("br + br").isEmpty()) {
                doc.select("br + br").remove();
                brRemovalCycles++;
            }
            if (brRemovalCycles > 0) {
                log.debug("연속 br 정리 {} 사이클 수행", brRemovalCycles);
            }

            // 4) 블록의 맨 앞/뒤 br 제거(중요 속성 br 보존)
            int removedLeadingTrailing = 0;
            int preservedImportantBrs = 0;
            for (Element br : doc.select("br")) {
                if (hasImportantAttrs(br)) {
                    preservedImportantBrs++;
                    continue;
                }
                Element parent = br.parent();
                if (parent != null && parent.isBlock()) {
                    boolean isLeading = br.previousElementSibling() == null;
                    boolean isTrailing = br.nextElementSibling() == null;
                    if (isLeading || isTrailing) {
                        br.remove();
                        removedLeadingTrailing++;
                    }
                }
            }
            if (removedLeadingTrailing > 0) {
                log.debug("블록 앞/뒤 br {} 개 제거", removedLeadingTrailing);
            }
            if (preservedImportantBrs > 0) {
                log.debug("중요 속성 br {} 개 보존", preservedImportantBrs);
            }

            String result = doc.body().html();
            log.debug("HTML 정리 완료 - 결과 길이: {} chars", result.length());
            
            return result;
        } catch (Exception ex) {
            log.warn("HTML 정리 중 예외 발생, 원본 반환: {}", ex.getMessage());
            return html;
        }
    }

    private static boolean hasImportantAttrs(Element br) {
        if (br.hasAttr("class") || br.hasAttr("id") || br.hasAttr("style")) {
            return true;
        }
        for (Attribute a : br.attributes()) {
            if (a.getKey().startsWith("data-")) {
                return true;
            }
        }
        return false;
    }


    private static String extractTextFromPart(Part part) throws MessagingException, IOException {
        String contentType = part.getContentType();
        log.debug("파트 처리 중 - Content-Type: {}", contentType);
        
        if (part.isMimeType("text/html")) {
            log.debug("HTML 파트 발견");
            return (String) part.getContent();
        }

        if (part.isMimeType("text/plain")) {
            log.debug("Plain text 파트 발견");
            return (String) part.getContent();
        }

        if (part.isMimeType("multipart/alternative")) {
            log.debug("Multipart/alternative 파트 발견");
            Multipart mp = (Multipart) part.getContent();
            String plain = null;
            for (int i = mp.getCount() - 1; i >= 0; i--) {
                BodyPart bp = mp.getBodyPart(i);
                log.debug("Alternative 파트 {}: {}", i, bp.getContentType());
                if (bp.isMimeType("text/html")) {
                    String s = extractTextFromPart(bp);
                    if (s != null && !s.isBlank()) {
                        log.debug("HTML 파트에서 텍스트 추출 성공");
                        return s;
                    }
                } else if (bp.isMimeType("text/plain")) {
                    if (plain == null) {
                        plain = extractTextFromPart(bp);
                        if (plain != null && !plain.isBlank()) {
                            log.debug("Plain text 파트에서 텍스트 추출 성공");
                        }
                    }
                } else {
                    String s = extractTextFromPart(bp);
                    if (s != null && !s.isBlank()) {
                        log.debug("기타 파트에서 텍스트 추출 성공");
                        return s;
                    }
                }
            }
            return plain == null ? "" : plain;
        }

        if (part.isMimeType("multipart/*")) {
            log.debug("Multipart 파트 발견 - 파트 개수: {}", ((Multipart) part.getContent()).getCount());
            Multipart multipart = (Multipart) part.getContent();
            List<Integer> order = sort(multipart);
            for (int idx : order) {
                BodyPart bp = multipart.getBodyPart(idx);
                log.debug("Multipart 파트 {}: {}", idx, bp.getContentType());
                String result = extractTextFromPart(bp);
                if (result != null && !result.isBlank()) {
                    log.debug("Multipart 파트 {}에서 텍스트 추출 성공", idx);
                    return result;
                }
            }
        }
        
        log.debug("파트에서 텍스트 추출 실패 - Content-Type: {}", contentType);
        return "";
    }

    private static List<Integer> sort(Multipart multipart) throws MessagingException {
        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < multipart.getCount(); i++) order.add(i);
        order.sort((a,b) -> {
            try {
                String contentTypeA = multipart.getBodyPart(a).getContentType().toLowerCase();
                String contentTypeB = multipart.getBodyPart(b).getContentType().toLowerCase();
                return score(contentTypeB) - score(contentTypeA);
            } catch (MessagingException e) { return 0; }
        });
        return order;
    }

    private static int score(String ct) {
        if (ct.startsWith("multipart/alternative")) {
            return 100;
        }
        if (ct.startsWith("multipart/related")) {
            return 90;
        }
        if (ct.startsWith("text/html")) {
            return 80;
        }
        if (ct.startsWith("text/plain")) {
            return 70;
        }
        return 0;
    }
}
