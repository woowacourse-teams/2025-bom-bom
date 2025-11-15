package me.bombom.api.v1.pet.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.pet.service.PetService;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PetScheduler {

    private static final String TIME_ZONE = "Asia/Seoul";
    private static final String DAILY_CRON = "0 0 0 * * *";

    private final PetService petService;

    @Scheduled(cron = DAILY_CRON, zone = TIME_ZONE)
    @SchedulerLock(name = "reset_attendance", lockAtLeastFor = "PT3S", lockAtMostFor = "PT15S")
    public void resetAttendance() {
        log.info("키우기 출석 상태 초기화 실행");
        petService.resetAttendance();
    }
}
