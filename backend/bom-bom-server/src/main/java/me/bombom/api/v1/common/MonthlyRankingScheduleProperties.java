package me.bombom.api.v1.common;

import jakarta.validation.constraints.NotBlank;
import java.time.ZoneId;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties("ranking.reading.monthly")
public class MonthlyRankingScheduleProperties {

    @NotBlank
    private String cron;

    @NotBlank
    private String zone;

    public ZoneId zoneId() {
        return ZoneId.of(zone);
    }

    public CronExpression cronExpression() {
        return CronExpression.parse(cron);
    }
}
