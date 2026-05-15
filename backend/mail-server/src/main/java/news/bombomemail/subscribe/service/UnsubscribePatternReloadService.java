package news.bombomemail.subscribe.service;

import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import news.bombomemail.article.util.UnsubscribeUrlExtractor;
import news.bombomemail.subscribe.domain.UnsubscribePattern;
import news.bombomemail.subscribe.repository.UnsubscribePatternRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class UnsubscribePatternReloadService {

    private static final String URL_KEYS = "parse.url-keywords";
    private static final String TEXT_KEY = "parse.text-keywords";

    private static final String PATTERN_REGEX = ",";
    private static final List<String> DEFAULT_URL_KEYWORDS = List.of("unsubscribe");
    private static final List<String> DEFAULT_TEXT_KEYWORDS = List.of(
            "unsubscribe", "unsubscription", "수신\\s*거부", "구독\\s*취소", "구독\\s*해지"
    );

    private final UnsubscribePatternRepository unsubscribePatternRepository;
    private final UnsubscribeUrlExtractor unsubscribeUrlExtractor;

    @PostConstruct
    public void init() {
        reload();
    }

    public void reload() {
        Map<String, String> patterns = unsubscribePatternRepository.findAll().stream()
                .collect(Collectors.toMap(UnsubscribePattern::getPatternKey, UnsubscribePattern::getPatternValue));

        List<String> urlKeywords = parseKeywords(patterns.get(URL_KEYS));
        List<String> textKeywords = parseKeywords(patterns.get(TEXT_KEY));

        if (urlKeywords.isEmpty() || textKeywords.isEmpty()) {
            log.warn("[UnsubscribePattern] DB에 패턴이 없어 기본값을 사용합니다.");
            unsubscribeUrlExtractor.reload(DEFAULT_URL_KEYWORDS, DEFAULT_TEXT_KEYWORDS);
            return;
        }

        unsubscribeUrlExtractor.reload(urlKeywords, textKeywords);
        log.info("[UnsubscribePattern] 패턴 리로드 완료 - url: {}, text: {}", urlKeywords, textKeywords);
    }

    private List<String> parseKeywords(String value) {
        if (!StringUtils.hasText(value)) {
            return List.of();
        }
        return Arrays.stream(value.split(PATTERN_REGEX))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
