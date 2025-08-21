package me.bombom.api.v1.member.scheduler;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.member.repository.MemberRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberCleanupScheduler {

    private static final String DAILY_CRON = "0 0 0 * * *";
    private static final String TIME_ZONE = "Asia/Seoul";

    private final MemberRepository memberRepository;

    @Transactional
    @Scheduled(cron = DAILY_CRON,  zone = TIME_ZONE)
    public void cleanupDeletedMembers() {
        log.info("탈퇴 후 1년 지난 회원 삭제");
        LocalDateTime oneYearAgo = LocalDateTime.now().minusYears(1);
        memberRepository.deleteByDeletedAtBefore(oneYearAgo);
    }
}
