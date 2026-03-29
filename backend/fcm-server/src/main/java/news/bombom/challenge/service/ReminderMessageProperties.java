package news.bombom.challenge.service;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import news.bombom.support.YamlPropertySourceFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "reminder")
@PropertySource(
        value = "classpath:reminder-messages.yml",
        factory = YamlPropertySourceFactory.class,
        ignoreResourceNotFound = true
)
public class ReminderMessageProperties {

    private Streak streak;
    private LastDay lastDay;
    private EliminationRisk eliminationRisk;
    private Absent absent;

    @Getter
    @Setter
    public static class Streak {
        private StreakTitle title;
        private List<String> bodyPool;

        @Getter
        @Setter
        public static class StreakTitle {
            private PhaseMessage s0;
            private PhaseMessage s2;
            private PhaseMessage s3;
            private PhaseMessage s7;
            private PhaseMessage s14;
            private PhaseMessage s21;
        }
    }

    @Getter
    @Setter
    public static class LastDay {
        private PhaseMessage title;
        private List<String> bodyPool;
    }

    @Getter
    @Setter
    public static class EliminationRisk {
        private PhaseMessage title;
        private PhaseMessage body;
    }

    @Getter
    @Setter
    public static class Absent {
        private PhaseMessage title;
        private PhaseMessage body;
    }

    @Getter
    @Setter
    public static class PhaseMessage {
        private String first;
        private String second;

        public String byPhase(boolean isFirst) {
            return isFirst ? first : second;
        }
    }
}
