package me.bombom.api.v1.reading.service;

import java.time.Clock;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.reading.domain.ContinueReadingShield;
import me.bombom.api.v1.reading.domain.ContinueReadingShieldHistory;
import me.bombom.api.v1.reading.domain.ContinueReadingShieldHistoryReason;
import me.bombom.api.v1.reading.repository.ContinueReadingShieldHistoryRepository;
import me.bombom.api.v1.reading.repository.ContinueReadingShieldRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContinueReadingShieldService {

    private static final int INITIAL_GRANT_COUNT = 1;
    private static final int DEDUCT_COUNT = 1;
    private static final int MONTHLY_GRANT_COUNT = 1;
    private static final int MONTHLY_SHIELD_GRANT_EVENT_DAY = 1;

    private final ContinueReadingShieldRepository continueReadingShieldRepository;
    private final ContinueReadingShieldHistoryRepository continueReadingShieldHistoryRepository;
    private final Clock clock;

    @Transactional
    public void initializeShield(Long memberId) {
        ContinueReadingShield shield = ContinueReadingShield.create(memberId);
        continueReadingShieldRepository.save(shield);
        continueReadingShieldHistoryRepository.save(
                ContinueReadingShieldHistory.grant(
                        memberId,
                        ContinueReadingShieldHistoryReason.SIGNUP,
                        currentDate(),
                        INITIAL_GRANT_COUNT
                )
        );
    }

    @Transactional
    public boolean useShield(Long memberId, LocalDate targetDate) {
        int updatedRows = continueReadingShieldRepository.bulkDecreaseRemainingCountIfUsable(
                memberId,
                ContinueReadingShieldHistoryReason.DAILY_RESET_PROTECTION_USE.name(),
                targetDate,
                DEDUCT_COUNT
        );
        if (updatedRows == 0) {
            return false;
        }
        continueReadingShieldHistoryRepository.save(
                ContinueReadingShieldHistory.use(
                        memberId,
                        ContinueReadingShieldHistoryReason.DAILY_RESET_PROTECTION_USE,
                        targetDate,
                        DEDUCT_COUNT
                )
        );
        return true;
    }

    @Transactional
    public void resetMonthlyShieldsIfFirstDay() {
        LocalDate today = currentDate();
        if (today.getDayOfMonth() != MONTHLY_SHIELD_GRANT_EVENT_DAY) {
            return;
        }
        LocalDate monthlyShieldGrantEventDate = today.withDayOfMonth(MONTHLY_SHIELD_GRANT_EVENT_DAY);
        continueReadingShieldRepository.bulkResetMonthlyIfNotGranted(
                ContinueReadingShieldHistoryReason.MONTHLY_RESET.name(),
                monthlyShieldGrantEventDate,
                MONTHLY_GRANT_COUNT
        );
        continueReadingShieldHistoryRepository.bulkInsertMonthlyGrantHistories(
                ContinueReadingShieldHistoryReason.MONTHLY_RESET.name(),
                monthlyShieldGrantEventDate,
                MONTHLY_GRANT_COUNT
        );
    }

    @Transactional
    public void deleteByMemberId(Long memberId) {
        continueReadingShieldHistoryRepository.deleteByMemberId(memberId);
        continueReadingShieldRepository.deleteByMemberId(memberId);
    }

    private LocalDate currentMonthlyShieldGrantEventDate() {
        return currentDate().withDayOfMonth(MONTHLY_SHIELD_GRANT_EVENT_DAY);
    }

    private LocalDate currentDate() {
        return LocalDate.now(clock);
    }
}
