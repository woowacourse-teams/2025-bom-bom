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

    public ExecutorMetricsBinder(@Qualifier("taskExecutor") Executor taskExecutor) {
        this.taskExecutor = (ThreadPoolTaskExecutor) taskExecutor;
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        ExecutorServiceMetrics.monitor(registry, taskExecutor.getThreadPoolExecutor(), "defaultAsync", Tags.empty());
    }
}
