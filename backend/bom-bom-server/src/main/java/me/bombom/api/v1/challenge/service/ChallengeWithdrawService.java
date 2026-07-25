package me.bombom.api.v1.challenge.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.challenge.repository.ChallengeCommentLikeRepository;
import me.bombom.api.v1.challenge.repository.ChallengeCommentReplyRepository;
import me.bombom.api.v1.challenge.repository.ChallengeCommentRepository;
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
    private final ChallengeCommentRepository challengeCommentRepository;
    private final ChallengeCommentReplyRepository challengeCommentReplyRepository;
    private final ChallengeCommentLikeRepository challengeCommentLikeRepository;
    private final ChallengeDailyResultRepository challengeDailyResultRepository;
    private final ChallengeDailyTodoRepository challengeDailyTodoRepository;
    private final ChallengeDailyGuideCommentRepository challengeDailyGuideCommentRepository;
    private final ChallengeStartNotificationRepository challengeStartNotificationRepository;
    private final ChallengeTodoReminderNotificationRepository challengeTodoReminderNotificationRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteAllByMemberId(Long memberId) {
        List<Long> participantIds = challengeParticipantRepository.findIdsByMemberId(memberId);

        if (!participantIds.isEmpty()) {
            // 탈퇴 회원이 작성한 댓글에 달린 타인의 좋아요/답글을 먼저 정리한다.
            List<Long> commentIds = challengeCommentRepository.findIdsByParticipantIdIn(participantIds);
            if (!commentIds.isEmpty()) {
                challengeCommentLikeRepository.bulkDeleteAllByCommentIdIn(commentIds);
                challengeCommentReplyRepository.bulkDeleteAllByCommentIdIn(commentIds);
            }

            // 탈퇴 회원 본인이 남긴 좋아요/답글/댓글/일일기록을 정리한다.
            challengeCommentLikeRepository.bulkDeleteAllByParticipantIdIn(participantIds);
            challengeCommentReplyRepository.bulkDeleteAllByParticipantIdIn(participantIds);
            challengeCommentRepository.bulkDeleteAllByParticipantIdIn(participantIds);
            challengeDailyResultRepository.bulkDeleteAllByParticipantIdIn(participantIds);
            challengeDailyTodoRepository.bulkDeleteAllByParticipantIdIn(participantIds);
            challengeDailyGuideCommentRepository.bulkDeleteAllByParticipantIdIn(participantIds);
        }

        challengeReviewRepository.bulkDeleteAllByMemberId(memberId);
        challengeStartNotificationRepository.bulkDeleteAllByMemberId(memberId);
        challengeTodoReminderNotificationRepository.bulkDeleteAllByMemberId(memberId);
        challengeParticipantRepository.bulkDeleteAllByMemberId(memberId);
    }
}
