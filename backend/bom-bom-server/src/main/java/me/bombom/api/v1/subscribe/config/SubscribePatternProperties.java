package me.bombom.api.v1.subscribe.config;

import java.util.List;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "subscribe.patterns")
public class SubscribePatternProperties {

    private final Pattern unsubscribePattern;
    private final Pattern successPattern;
    private final Pattern alreadyUnsubscribedPattern;
    private final Pattern errorPattern;
    private final List<String> adDomains;
}
