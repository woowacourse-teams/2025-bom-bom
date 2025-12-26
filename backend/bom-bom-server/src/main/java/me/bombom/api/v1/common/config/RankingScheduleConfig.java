package me.bombom.api.v1.common.config;

import me.bombom.api.v1.common.MonthlyRankingScheduleProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MonthlyRankingScheduleProperties.class)
public class RankingScheduleConfig {
}
