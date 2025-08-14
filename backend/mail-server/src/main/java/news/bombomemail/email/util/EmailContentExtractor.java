package news.bombomemail.email.util;

import jakarta.mail.BodyPart;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EmailContentExtractor {

    private static final String BLOCKS = String.join(",",
            "address","article","aside","blockquote","div","dl","fieldset","figcaption","figure","footer","form",
            "h1","h2","h3","h4","h5","h6","header","hr","li","main","nav","ol","p","pre","section","table",
            "thead","tbody","tfoot","tr","td","th","ul"
    );

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


        if (part.isMimeType("message/rfc822")) {
            Object inner = part.getContent();
            if (inner instanceof MimeMessage) {
                return extractTextFromPart((MimeMessage) inner);
            }
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

    private static boolean looksLikeHtml(String text) {
        if (text == null) {
            return false;
        }
        String s = text.toLowerCase(Locale.ROOT);
        return s.contains("<html") || s.contains("<div") || s.contains("<p") || s.contains("<br")
                || s.contains("<span") || s.contains("<table") || s.contains("<img") || s.contains("<a ");
    }

    private static String cleanHtmlBrs(String html) {
        try {
            log.debug("HTML 정리 시작 - 원본 길이: {} chars", html.length());

            Document doc = Jsoup.parse(html);
            doc.outputSettings().prettyPrint(false);

            // ---- 0) 보호 영역: pre/code/blockquote 내부는 손대지 않도록 기준 셀렉터 준비
            String PROTECTED_ANCESTORS = "pre,code,blockquote";

            // 1) 블록 경계에 낀 br 제거
            Set<Element> toRemove = new LinkedHashSet<>();

            // (a) BLOCKS + br  => 이 셀렉터는 <br> 자신을 가리킵니다. 그대로 제거 대상에 추가
            toRemove.addAll(doc.select(selBlocksFollowedByBr()));

            // (b) br + BLOCKS  => 이 셀렉터는 '블록'을 가리킵니다. 바로 앞 형제가 <br>이면 그 <br>만 제거 대상에 추가
            for (Element block : doc.select(selBrFollowedByBlocks())) {
                Element prev = block.previousElementSibling();
                if (prev != null && prev.tagName().equals("br")) {
                    toRemove.add(prev);
                }
            }

            int removedBoundary = 0;
            for (Element br : toRemove) {
                // 보호영역/중요속성 체크
                if (!br.parents().select(PROTECTED_ANCESTORS).isEmpty()) continue;
                if (hasImportantAttrs(br)) continue;
                br.remove();
                removedBoundary++;
            }
            if (removedBoundary > 0) {
                log.debug("블록 경계 br {} 개 제거", removedBoundary);
            }

            // ---- 2) 빈 단락 제거: <div><br></div>, <p><br></p>
            int removedEmptyParas = 0;
            for (Element box : doc.select("div:has(> br:only-child), p:has(> br:only-child)")) {
                if (!box.parents().select(PROTECTED_ANCESTORS).isEmpty()) continue;
                Element br = box.selectFirst("> br");
                if (br != null && hasImportantAttrs(br)) continue; // 의도적 br 보존
                box.remove();
                removedEmptyParas++;
            }
            if (removedEmptyParas > 0) {
                log.debug("빈 단락 {} 개 제거", removedEmptyParas);
            }

            // 3) 연속 br 축약 (한 번에 전체 제거, 안정화될 때까지)
            int cycles = 0;
            while (true) {
                Elements doubles = doc.select("br + br");
                if (doubles.isEmpty()) break;

                for (Element br : doubles) {
                    if (!br.parents().select(PROTECTED_ANCESTORS).isEmpty()) continue;
                    if (hasImportantAttrs(br)) continue;
                    br.remove();
                }
                cycles++;
            }
            if (cycles > 0) {
                log.debug("연속 br 정리 {} 사이클 수행", cycles);
            }

            // 4) 블록의 맨 앞/뒤 br 제거(중요 속성 br 보존)
            int removedEdges = 0;
            int preservedImportant = 0;
            for (Element br : doc.select("br")) {
                if (!br.parents().select(PROTECTED_ANCESTORS).isEmpty()) continue;

                if (hasImportantAttrs(br)) { preservedImportant++; continue; }

                Element p = br.parent();
                if (p != null && p.isBlock()) {
                    boolean hasPrevContent = hasMeaningfulPrevSibling(br);
                    boolean hasNextContent = hasMeaningfulNextSibling(br);
                    boolean isLeading = !hasPrevContent;
                    boolean isTrailing = !hasNextContent;
                    if (isLeading || isTrailing) {
                        br.remove();
                        removedEdges++;
                    }
                }
            }
            if (removedEdges > 0) {
                log.debug("블록 앞/뒤 br {} 개 제거", removedEdges);
            }
            if (preservedImportant > 0) {
                log.debug("중요 속성 br {} 개 보존", preservedImportant);
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

    private static boolean hasMeaningfulPrevSibling(Element el) {
        Node n = el.previousSibling();
        while (n != null) {
            if (n instanceof TextNode) {
                if (!((TextNode) n).isBlank()) return true;
            } else if (n instanceof Element) {
                return true;
            }
            n = n.previousSibling();
        }
        return false;
    }

    private static boolean hasMeaningfulNextSibling(Element el) {
        Node n = el.nextSibling();
        while (n != null) {
            if (n instanceof TextNode) {
                if (!((TextNode) n).isBlank()) return true;
            } else if (n instanceof Element) {
                return true;
            }
            n = n.nextSibling();
        }
        return false;
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

    private static String selBlocksFollowedByBr() {
        return BLOCKS + " + br";
    }

    private static String selBrFollowedByBlocks() {
        return "br + " + BLOCKS;
    }
}
