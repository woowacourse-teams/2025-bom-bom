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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class UnsubscribePatternReloader {

    private static final String URL_KEY = "parse.url-keyword";
    private static final String TEXT_KEY = "parse.text-keywords";

    private static final String DEFAULT_URL_KEYWORD = "unsubscribe";
    private static final List<String> DEFAULT_TEXT_KEYWORDS = List.of(
            "unsubscribe", "unsubscription", "수신\\s*거부", "구독\\s*취소", "구독\\s*해지"
    );

    private final UnsubscribePatternRepository repository;
    private final UnsubscribeUrlExtractor extractor;

    @PostConstruct
    public void init() {
        reload();
    }

    @Scheduled(fixedDelayString = "PT1H")
    public void reload() {
        Map<String, String> patterns = repository.findAll().stream()
                .collect(Collectors.toMap(UnsubscribePattern::getPatternKey, UnsubscribePattern::getPatternValue));

        String urlKeyword = patterns.get(URL_KEY);
        List<String> textKeywords = parseTextKeywords(patterns.get(TEXT_KEY));

        if (!StringUtils.hasText(urlKeyword) || textKeywords.isEmpty()) {
            log.warn("[UnsubscribePattern] DB에 패턴이 없어 기본값을 사용합니다.");
            extractor.reload(DEFAULT_URL_KEYWORD, DEFAULT_TEXT_KEYWORDS);
            return;
        }

        extractor.reload(urlKeyword, textKeywords);
        log.info("[UnsubscribePattern] 패턴 리로드 완료 - url: {}, text: {}", urlKeyword, textKeywords);
    }

    private List<String> parseTextKeywords(String value) {
        if (!StringUtils.hasText(value)) {
            return List.of();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
