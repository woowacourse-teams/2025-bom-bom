package me.bombom.api.v1.challenge.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.challenge.repository.ChallengeCommentLikeRepository;
import me.bombom.api.v1.challenge.repository.ChallengeDailyGuideCommentRepository;
import me.bombom.api.v1.challenge.repository.ChallengeDailyResultRepository;
import me.bombom.api.v1.challenge.repository.ChallengeDailyTodoRepository;
import me.bombom.api.v1.challenge.repository.ChallengeParticipantRepository;
import me.bombom.api.v1.challenge.repository.ChallengeReviewRepository;
import me.bombom.api.v1.challenge.repository.ChallengeStartNotificationRepository;
import me.bombom.api.v1.challenge.repository.ChallengeTodoReminderNotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengeWithdrawService {

    private final ChallengeParticipantRepository challengeParticipantRepository;
    private final ChallengeReviewRepository challengeReviewRepository;
    private final ChallengeCommentLikeRepository challengeCommentLikeRepository;
    private final ChallengeDailyResultRepository challengeDailyResultRepository;
    private final ChallengeDailyTodoRepository challengeDailyTodoRepository;
    private final ChallengeDailyGuideCommentRepository challengeDailyGuideCommentRepository;
    private final ChallengeStartNotificationRepository challengeStartNotificationRepository;
    private final ChallengeTodoReminderNotificationRepository challengeTodoReminderNotificationRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteAllByMemberId(Long memberId) {
        var participantIds = challengeParticipantRepository.findIdsByMemberId(memberId);
        if (!participantIds.isEmpty()) {
            // 댓글/답글과 작성자 participant는 보존해 조회 시 탈퇴 회원으로 표시되도록 한다.
            challengeCommentLikeRepository.bulkDeleteAllByParticipantIdIn(participantIds);
            challengeDailyResultRepository.bulkDeleteAllByParticipantIdIn(participantIds);
            challengeDailyTodoRepository.bulkDeleteAllByParticipantIdIn(participantIds);
            challengeDailyGuideCommentRepository.bulkDeleteAllByParticipantIdIn(participantIds);
        }

        challengeReviewRepository.bulkDeleteAllByMemberId(memberId);
        challengeStartNotificationRepository.bulkDeleteAllByMemberId(memberId);
        challengeTodoReminderNotificationRepository.bulkDeleteAllByMemberId(memberId);
    }
}
