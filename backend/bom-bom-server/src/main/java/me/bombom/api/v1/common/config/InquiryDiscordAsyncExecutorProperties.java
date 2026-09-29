package me.bombom.api.v1.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties("async.inquiry-discord-executor")
public class InquiryDiscordAsyncExecutorProperties {

    private int corePoolSize = 1;
    private int maxPoolSize = 2;
    private int queueCapacity = 50;
}
