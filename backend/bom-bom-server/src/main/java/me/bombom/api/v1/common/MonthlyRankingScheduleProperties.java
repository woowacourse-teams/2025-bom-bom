package me.bombom.api.v1.common;

import java.time.ZoneId;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.scheduling.support.CronExpression;

@Getter
@Setter
@ConfigurationProperties("ranking.reading.monthly")
public class MonthlyRankingScheduleProperties {

    private String cron;
    private String zone;

    public ZoneId zoneId() {
        return ZoneId.of(zone);
    }

    public CronExpression cronExpression() {
        return CronExpression.parse(cron);
    }
}
