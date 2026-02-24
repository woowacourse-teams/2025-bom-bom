package me.bombom.api.v1.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties("async.executor")
public class AsyncExecutorProperties {

    private int corePoolSize = 1; // 유지할 기본 스레드 수
    private int maxPoolSize = 3;  // 큐 초과 시 늘어날 최대 스레드 수
    private int queueCapacity = 50; // 대기 큐 용량 (작업이 쌓일 수 있는 개수)
}
