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
    public void createPendingNotificationsForIncompleteTodos(LocalDate reminderDate) {
        List<Challenge> ongoingChallenges = challengeRepository.findOngoingChallenges(reminderDate);

        if (ongoingChallenges.isEmpty()) {
            log.info("진행 중 챌린지가 없습니다. reminderDate={}", reminderDate);
            return;
        }

        for (Challenge challenge : ongoingChallenges) {
            try {
                List<ChallengeParticipant> incompleteParticipants = challengeParticipantRepository
                        .findSurvivedParticipantsWithIncompleteTodosByChallengeId(challenge.getId(), reminderDate);

                if (incompleteParticipants.isEmpty()) {
                    log.info("미완료 참여자가 없는 챌린지입니다. challengeId={}, reminderDate={}",
                            challenge.getId(), reminderDate);
                    continue;
                }

                Set<Long> existingMemberIds = new HashSet<>(
                        challengeTodoReminderNotificationRepository
                                .findMemberIdsByChallengeIdAndCreatedAtBetween(
                                        challenge.getId(),
                                        reminderDate.atStartOfDay(),
                                        endOfDay(reminderDate)
                                )
                );

                List<ChallengeTodoReminderNotification> notifications = incompleteParticipants.stream()
                        .map(ChallengeParticipant::getMemberId)
                        .filter(memberId -> !existingMemberIds.contains(memberId))
                        .map(memberId -> ChallengeTodoReminderNotification.createPending(
                                memberId,
                                challenge.getId(),
                                challenge.getName()
                        ))
                        .toList();

                if (notifications.isEmpty()) {
                    log.info("이미 TODO 리마인더 알림이 모두 적재된 챌린지입니다. challengeName={}, reminderDate={}",
                            challenge.getName(), reminderDate);
                    continue;
                }

                challengeTodoReminderNotificationRepository.saveAll(notifications);
                log.info("챌린지 TODO 리마인더 알림 적재 완료. challengeId={}, challengeName={}, reminderDate={}, createdCount={}",
                        challenge.getId(), challenge.getName(), reminderDate, notifications.size());
            } catch (Exception e) {
                log.error("챌린지 TODO 리마인더 알림 적재 실패. challengeId={}, challengeName={}, reminderDate={}",
                        challenge.getId(), challenge.getName(), reminderDate, e);
            }
        }
    }

    private LocalDateTime endOfDay(LocalDate date) {
        return date.plusDays(1).atStartOfDay().minusNanos(1);
    }
}
