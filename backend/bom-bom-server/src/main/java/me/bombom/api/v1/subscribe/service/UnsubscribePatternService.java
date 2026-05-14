package me.bombom.api.v1.subscribe.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.subscribe.domain.UnsubscribePattern;
import me.bombom.api.v1.subscribe.dto.UnsubscribePatterns;
import me.bombom.api.v1.subscribe.repository.UnsubscribePatternRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UnsubscribePatternService {

    private final UnsubscribePatternRepository patternRepository;

    public UnsubscribePatterns getPatterns() {
        List<UnsubscribePattern> entities = patternRepository.findAll();
        Map<String, String> patternMap = new HashMap<>();
        for (UnsubscribePattern entity : entities) {
            patternMap.put(entity.getPatternKey(), entity.getPatternValue());
        }

        return new UnsubscribePatterns(
                patternMap.getOrDefault("unsubscribe-pattern", ""),
                patternMap.getOrDefault("success-pattern", ""),
                patternMap.getOrDefault("already-unsubscribed-pattern", ""),
                patternMap.getOrDefault("error-pattern", ""),
                parseAdDomains(patternMap.getOrDefault("ad-domains", ""))
        );
    }

    private List<String> parseAdDomains(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
