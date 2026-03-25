package me.bombom.api.v1.challenge.service;

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
                List<ChallengeParticipant> incompleteParticipants = challengeParticipantRepository
                        .findSurvivedParticipantsWithoutCompleteDailyResultByChallengeId(challenge.getId(), reminderDate);

                if (incompleteParticipants.isEmpty()) {
                    log.info("미완료 참여자가 없는 챌린지입니다. challengeId={}, reminderDate={}",
                            challenge.getId(), reminderDate);
                    continue;
                }

                Set<Long> existingMemberIds = new HashSet<>(
                        challengeTodoReminderNotificationRepository
                                .findMemberIdsByChallengeIdAndPhaseAndCreatedAtBetween(
                                        challenge.getId(),
                                        phase,
                                        reminderDate.atStartOfDay(),
                                        endOfDay(reminderDate)
                                )
                );

                boolean isLastDay = reminderDate.equals(challenge.getEndDate());
                List<ChallengeTodoReminderNotification> notifications = incompleteParticipants.stream()
                        .filter(participant -> !existingMemberIds.contains(participant.getMemberId()))
                        .map(participant -> ChallengeTodoReminderNotification.createPending(
                                participant.getMemberId(),
                                challenge.getId(),
                                challenge.getName(),
                                phase,
                                participant.getStreak(),
                                isLastDay
                        ))
                        .toList();
                if (notifications.isEmpty()) {
                    log.info("이미 TODO 리마인더 알림이 모두 적재된 챌린지입니다. challengeName={}, reminderDate={}, phase={}",
                            challenge.getName(), reminderDate, phase);
                    continue;
                }

                challengeTodoReminderNotificationRepository.saveAll(notifications);
                log.info("챌린지 TODO 리마인더 알림 적재 완료. challengeId={}, challengeName={}, reminderDate={}, phase={}, createdCount={}",
                        challenge.getId(), challenge.getName(), reminderDate, phase, notifications.size());
            } catch (Exception e) {
                log.error("챌린지 TODO 리마인더 알림 적재 실패. challengeId={}, challengeName={}, reminderDate={}, phase={}",
                        challenge.getId(), challenge.getName(), reminderDate, phase, e);
            }
        }
    }

    private LocalDateTime endOfDay(LocalDate date) {
        return date.plusDays(1).atStartOfDay().minusNanos(1);
    }
}
