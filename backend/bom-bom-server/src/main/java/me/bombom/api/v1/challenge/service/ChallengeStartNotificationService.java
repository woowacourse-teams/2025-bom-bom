package me.bombom.api.v1.challenge.service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.challenge.domain.Challenge;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import me.bombom.api.v1.challenge.domain.notification.ChallengeStartNotification;
import me.bombom.api.v1.challenge.repository.ChallengeParticipantRepository;
import me.bombom.api.v1.challenge.repository.ChallengeRepository;
import me.bombom.api.v1.challenge.repository.ChallengeStartNotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengeStartNotificationService {

    private final ChallengeRepository challengeRepository;
    private final ChallengeParticipantRepository challengeParticipantRepository;
    private final ChallengeStartNotificationRepository challengeStartNotificationRepository;

    @Transactional
    public void createPendingNotificationsForStartingChallenges(LocalDate date) {
        List<Challenge> startingChallenges = challengeRepository.findChallengesStartingOn(date);

        if (startingChallenges.isEmpty()) {
            log.info("시작일인 챌린지가 없습니다. date={}", date);
            return;
        }

        for (Challenge challenge : startingChallenges) {
            List<ChallengeParticipant> participants = challengeParticipantRepository.findAllByChallengeId(challenge.getId());

            if (participants.isEmpty()) {
                log.info("참여자가 없는 시작 챌린지입니다. challengeId={}", challenge.getId());
                continue;
            }

            Set<Long> existingMemberIds = new HashSet<>(
                    challengeStartNotificationRepository.findMemberIdsByChallengeId(challenge.getId()));

            List<ChallengeStartNotification> notifications = participants.stream()
                    .map(ChallengeParticipant::getMemberId)
                    .filter(memberId -> !existingMemberIds.contains(memberId))
                    .map(memberId -> ChallengeStartNotification.createPending(
                            memberId,
                            challenge.getId(),
                            challenge.getName()
                    ))
                    .toList();

            if (notifications.isEmpty()) {
                log.info("이미 시작 알림이 모두 적재된 챌린지입니다. challengeId={}", challenge.getId());
                continue;
            }

            challengeStartNotificationRepository.saveAll(notifications);
            log.info(
                    "챌린지 시작 알림 적재 완료. challengeId={}, challengeName={}, createdCount={}",
                    challenge.getId(),
                    challenge.getName(),
                    notifications.size()
            );
        }
    }
}
