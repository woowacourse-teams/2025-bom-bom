package me.bombom.api.v1.badge.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.badge.service.BadgeService;
import me.bombom.api.v1.reading.event.ContinueReadingIncreasedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContinueReadingIncreasedEventListener {

    private final BadgeService badgeService;

    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(ContinueReadingIncreasedEvent event) {
        log.info("ContinueReadingIncreasedEvent received - memberId={}, streakDayCount={}",
                event.memberId(), event.streakDayCount());
        badgeService.issueStreakBadge(event.memberId(), event.streakDayCount());
    }
}
