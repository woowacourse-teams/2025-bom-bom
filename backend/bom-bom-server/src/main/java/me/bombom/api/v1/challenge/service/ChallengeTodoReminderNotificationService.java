package me.bombom.api.v1.challenge.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.challenge.domain.Challenge;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import me.bombom.api.v1.challenge.domain.notification.ChallengeTodoReminderNotification;
import me.bombom.api.v1.challenge.domain.notification.ChallengeTodoReminderPhase;
import me.bombom.api.v1.challenge.repository.ChallengeParticipantRepository;
import me.bombom.api.v1.challenge.repository.ChallengeRepository;
import me.bombom.api.v1.challenge.repository.ChallengeTodoReminderNotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengeTodoReminderNotificationService {

    private final ChallengeRepository challengeRepository;
    private final ChallengeParticipantRepository challengeParticipantRepository;
    private final ChallengeTodoReminderNotificationRepository challengeTodoReminderNotificationRepository;

    @Transactional
    public void createPendingNotificationsForIncompleteTodos(LocalDate reminderDate, ChallengeTodoReminderPhase phase) {
        List<Challenge> ongoingChallenges = challengeRepository.findOngoingChallenges(reminderDate);

        if (ongoingChallenges.isEmpty()) {
            log.info("진행 중 챌린지가 없습니다. reminderDate={}, phase={}", reminderDate, phase);
            return;
        }

        for (Challenge challenge : ongoingChallenges) {
            try {
                createPendingNotificationsForChallenge(challenge, reminderDate, phase);
            } catch (Exception e) {
                log.error("챌린지 TODO 리마인더 알림 적재 실패. challengeId={}, challengeName={}, reminderDate={}, phase={}",
                        challenge.getId(), challenge.getName(), reminderDate, phase, e);
            }
        }
    }

    private void createPendingNotificationsForChallenge(Challenge challenge, LocalDate reminderDate, ChallengeTodoReminderPhase phase) {
        List<ChallengeParticipant> incompleteParticipants = challengeParticipantRepository
                .findSurvivedParticipantsWithoutCompleteDailyResultByChallengeId(challenge.getId(), reminderDate);
        if (incompleteParticipants.isEmpty()) {
            log.info("미완료 참여자가 없는 챌린지입니다. challengeId={}, reminderDate={}", challenge.getId(), reminderDate);
            return;
        }

        Set<Long> alreadyNotifiedMemberIds = findAlreadyNotifiedMemberIds(challenge.getId(), phase, reminderDate);
        boolean isLastDay = challenge.isLastDay(reminderDate);
        List<ChallengeTodoReminderNotification> notifications = buildNotifications(
                incompleteParticipants,
                alreadyNotifiedMemberIds,
                reminderDate,
                challenge,
                phase,
                isLastDay
        );

        if (notifications.isEmpty()) {
            log.info("이미 TODO 리마인더 알림이 모두 적재된 챌린지입니다. challengeName={}, reminderDate={}, phase={}",
                    challenge.getName(), reminderDate, phase);
            return;
        }

        challengeTodoReminderNotificationRepository.saveAll(notifications);
        log.info("챌린지 TODO 리마인더 알림 적재 완료. challengeId={}, challengeName={}, reminderDate={}, phase={}, createdCount={}",
                challenge.getId(), challenge.getName(), reminderDate, phase, notifications.size());
    }

    private Set<Long> findAlreadyNotifiedMemberIds(Long challengeId, ChallengeTodoReminderPhase phase, LocalDate date) {
        return new HashSet<>(challengeTodoReminderNotificationRepository.findMemberIdsByChallengeIdAndPhaseAndCreatedAtBetween(
                challengeId,
                phase,
                date.atStartOfDay(),
                endOfDay(date)
            )
        );
    }

    private List<ChallengeTodoReminderNotification> buildNotifications(
            List<ChallengeParticipant> incompleteParticipants,
            Set<Long> alreadyNotifiedMemberIds,
            LocalDate reminderDate,
            Challenge challenge,
            ChallengeTodoReminderPhase phase,
            boolean isLastDay
    ) {
        return incompleteParticipants.stream()
                .filter(participant -> !alreadyNotifiedMemberIds.contains(participant.getMemberId()))
                .map(participant -> ChallengeTodoReminderNotification.createPending(
                        participant.getMemberId(),
                        challenge.getId(),
                        challenge.getName(),
                        phase,
                        participant.getStreak(),
                        calculateDaysAbsent(participant.getLastParticipatedDate(), reminderDate),
                        isLastDay
                ))
                .toList();
    }

    private Integer calculateDaysAbsent(LocalDate lastParticipatedDate, LocalDate reminderDate) {
        if (lastParticipatedDate == null) {
            return null;
        }

        int count = 0;
        LocalDate day = lastParticipatedDate.plusDays(1); // 참여한 날 제외
        while (day.isBefore(reminderDate)) { // 오늘(reminderDate)은 아직 참여 가능하므로 결석 카운트에서 제외
            if (day.getDayOfWeek() != DayOfWeek.SATURDAY && day.getDayOfWeek() != DayOfWeek.SUNDAY) {
                count++;
            }
            day = day.plusDays(1);
        }
        return count;
    }

    private LocalDateTime endOfDay(LocalDate date) {
        return date.plusDays(1).atStartOfDay().minusNanos(1);
    }
}
