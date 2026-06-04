package me.bombom.api.v1.reading.service;

import java.time.Clock;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.reading.domain.ContinueReadingShield;
import me.bombom.api.v1.reading.domain.ContinueReadingShieldHistory;
import me.bombom.api.v1.reading.domain.ContinueReadingShieldHistoryType;
import me.bombom.api.v1.reading.repository.ContinueReadingShieldHistoryRepository;
import me.bombom.api.v1.reading.repository.ContinueReadingShieldRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContinueReadingShieldService {

    private static final int SHIELD_QUANTITY = 1;

    private final ContinueReadingShieldRepository continueReadingShieldRepository;
    private final ContinueReadingShieldHistoryRepository continueReadingShieldHistoryRepository;
    private final Clock clock;

    @Transactional
    public void initializeShield(Long memberId) {
        ContinueReadingShield shield = ContinueReadingShield.create(memberId);
        continueReadingShieldRepository.save(shield);
        continueReadingShieldHistoryRepository.save(
                ContinueReadingShieldHistory.grant(memberId, currentMonthStartDate(), SHIELD_QUANTITY)
        );
    }

    @Transactional
    public boolean useShield(Long memberId, LocalDate targetDate) {
        ContinueReadingShield shield = continueReadingShieldRepository.findByMemberIdForUpdate(memberId)
                .orElse(null);
        if (shield == null) {
            log.warn("연속 읽기 보호막 정보가 없어 보호막 사용을 건너뜁니다. memberId={}", memberId);
            return false;
        }
        if (continueReadingShieldHistoryRepository.existsByMemberIdAndTypeAndEventDate(
                memberId,
                ContinueReadingShieldHistoryType.USE,
                targetDate
        )) {
            return false;
        }
        if (!shield.useIfAvailable()) {
            return false;
        }
        continueReadingShieldHistoryRepository.save(
                ContinueReadingShieldHistory.use(memberId, targetDate, SHIELD_QUANTITY)
        );
        return true;
    }

    @Transactional
    public void resetMonthlyShieldsIfFirstDay() {
        LocalDate today = LocalDate.now(clock);
        if (today.getDayOfMonth() != 1) {
            return;
        }
        LocalDate monthStartDate = today.withDayOfMonth(1);
        continueReadingShieldRepository.resetMonthlyIfNotGranted(monthStartDate, SHIELD_QUANTITY);
        continueReadingShieldHistoryRepository.insertMonthlyGrantHistories(monthStartDate, SHIELD_QUANTITY);
    }

    @Transactional
    public void deleteByMemberId(Long memberId) {
        continueReadingShieldHistoryRepository.deleteByMemberId(memberId);
        continueReadingShieldRepository.deleteByMemberId(memberId);
    }

    private LocalDate currentMonthStartDate() {
        return LocalDate.now(clock).withDayOfMonth(1);
    }
}
