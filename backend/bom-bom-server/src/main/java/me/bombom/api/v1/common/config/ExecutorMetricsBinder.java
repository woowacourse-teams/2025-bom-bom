package me.bombom.api.v1.common.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics;
import java.util.concurrent.Executor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@Component
public class ExecutorMetricsBinder implements MeterBinder {

    private final ThreadPoolTaskExecutor taskExecutor;
    private final ThreadPoolTaskExecutor markAsReadExecutor;

    public ExecutorMetricsBinder(
            @Qualifier("taskExecutor") Executor taskExecutor,
            @Qualifier("markAsReadExecutor") Executor markAsReadExecutor
    ) {
        this.taskExecutor = (ThreadPoolTaskExecutor) taskExecutor;
        this.markAsReadExecutor = (ThreadPoolTaskExecutor) markAsReadExecutor;
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        ExecutorServiceMetrics.monitor(registry, taskExecutor.getThreadPoolExecutor(), "defaultAsync", Tags.empty());
        ExecutorServiceMetrics.monitor(registry, markAsReadExecutor.getThreadPoolExecutor(), "markAsReadAsync", Tags.empty());
    }
}
