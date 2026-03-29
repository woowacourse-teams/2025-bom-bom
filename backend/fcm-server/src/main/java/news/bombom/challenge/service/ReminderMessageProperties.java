package news.bombom.challenge.service;

import jakarta.annotation.PostConstruct;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import news.bombom.support.YamlPropertySourceFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Slf4j
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

    private static final String DEFAULT_TITLE = "[%1$s] 오늘 할 일을 완료해주세요!";
    private static final String DEFAULT_BODY = "5분만 읽으면 오늘 출석이에요. 같이 해볼까요? 😄";

    private Streak streak;
    private LastDay lastDay;
    private EliminationRisk eliminationRisk;
    private Absent absent;

    @PostConstruct
    public void applyDefaults() {
        if (streak == null) {
            log.error("reminder-messages.yml이 없거나 streak 설정이 없어 기본 메시지를 사용합니다.");
            streak = defaultStreak();
        }
        if (lastDay == null) {
            log.error("reminder-messages.yml이 없거나 lastDay 설정이 없어 기본 메시지를 사용합니다.");
            lastDay = defaultLastDay();
        }
        if (eliminationRisk == null) {
            log.error("reminder-messages.yml이 없거나 eliminationRisk 설정이 없어 기본 메시지를 사용합니다.");
            eliminationRisk = defaultEliminationRisk();
        }
        if (absent == null) {
            log.error("reminder-messages.yml이 없거나 absent 설정이 없어 기본 메시지를 사용합니다.");
            absent = defaultAbsent();
        }
    }

    private Streak defaultStreak() {
        Streak streak = new Streak();
        Streak.StreakTitle title = new Streak.StreakTitle();
        PhaseMessage defaultPhaseMessage = defaultPhaseMessage(DEFAULT_TITLE);
        title.setStreak0(defaultPhaseMessage); title.setStreak2(defaultPhaseMessage); title.setStreak3(defaultPhaseMessage);
        title.setStreak7(defaultPhaseMessage); title.setStreak14(defaultPhaseMessage); title.setStreak21(defaultPhaseMessage);
        streak.setTitle(title);
        streak.setBodyPool(List.of(DEFAULT_BODY));
        return streak;
    }

    private LastDay defaultLastDay() {
        LastDay lastDay = new LastDay();
        lastDay.setTitle(defaultPhaseMessage(DEFAULT_TITLE));
        lastDay.setBodyPool(List.of(DEFAULT_BODY));
        return lastDay;
    }

    private EliminationRisk defaultEliminationRisk() {
        EliminationRisk eliminationRisk = new EliminationRisk();
        eliminationRisk.setTitle(defaultPhaseMessage(DEFAULT_TITLE));
        eliminationRisk.setBody(defaultPhaseMessage(DEFAULT_BODY));
        return eliminationRisk;
    }

    private Absent defaultAbsent() {
        Absent absent = new Absent();
        absent.setTitle(defaultPhaseMessage(DEFAULT_TITLE));
        absent.setBody(defaultPhaseMessage(DEFAULT_BODY));
        return absent;
    }

    private PhaseMessage defaultPhaseMessage(String message) {
        PhaseMessage phaseMessage = new PhaseMessage();
        phaseMessage.setFirst(message);
        phaseMessage.setSecond(message);
        return phaseMessage;
    }

    @Getter
    @Setter
    public static class Streak {
        private StreakTitle title;
        private List<String> bodyPool;

        @Getter
        @Setter
        public static class StreakTitle {
            private PhaseMessage streak0;
            private PhaseMessage streak2;
            private PhaseMessage streak3;
            private PhaseMessage streak7;
            private PhaseMessage streak14;
            private PhaseMessage streak21;
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
