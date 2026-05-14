package me.bombom.api.v1.challenge.service;

import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.challenge.domain.ChallengeCommentReply;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import me.bombom.api.v1.challenge.dto.request.CreateCommentReplyRequest;
import me.bombom.api.v1.challenge.dto.response.CommentReplyResponse;
import me.bombom.api.v1.challenge.repository.ChallengeCommentReplyRepository;
import me.bombom.api.v1.challenge.repository.ChallengeCommentRepository;
import me.bombom.api.v1.challenge.repository.ChallengeParticipantRepository;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengeCommentReplyService {

    private final ChallengeCommentReplyRepository challengeCommentReplyRepository;
    private final ChallengeCommentRepository challengeCommentRepository;
    private final ChallengeParticipantRepository challengeParticipantRepository;

    @Transactional
    public void createCommentReply(Long challengeId, Long commentId, Long memberId, CreateCommentReplyRequest request) {
        validateComment(commentId);
        ChallengeParticipant replyAuthor = getParticipantWithChallengeId(memberId, challengeId);

        challengeCommentReplyRepository.save(
                ChallengeCommentReply.builder()
                        .commentId(commentId)
                        .participantId(replyAuthor.getId())
                        .reply(request.reply())
                        .isPrivate(request.isPrivate())
                        .build()
        );
        challengeCommentRepository.updateReplyCount(commentId);
    }

    public Page<CommentReplyResponse> getCommentReplies(Long memberId, Long challengeId, Long commentId, Pageable pageable) {
        validateComment(commentId);
        validateMemberInSameChallenge(challengeId, memberId);
        return challengeCommentReplyRepository.findAllByCommentId(commentId, memberId, pageable);
    }

    private void validateComment(Long commentId) {
        if (!challengeCommentRepository.existsById(commentId)) {
            throw new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext(ErrorContextKeys.COMMENT_ID, commentId)
                    .addContext(ErrorContextKeys.OPERATION, "existsById");
        }
    }

    private void validateMemberInSameChallenge(Long challengeId, Long memberId) {
        if (!challengeParticipantRepository.existsByChallengeIdAndMemberId(challengeId, memberId)) {
            throw new CIllegalArgumentException(ErrorDetail.FORBIDDEN_RESOURCE)
                    .addContext(ErrorContextKeys.CHALLENGE_ID, challengeId)
                    .addContext(ErrorContextKeys.MEMBER_ID, memberId)
                    .addContext(ErrorContextKeys.OPERATION, "existsByChallengeIdAndMemberId");
        }
    }

    private ChallengeParticipant getParticipantWithChallengeId(Long memberId, Long challengeId) {
        return challengeParticipantRepository.findByChallengeIdAndMemberId(challengeId, memberId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.FORBIDDEN_RESOURCE)
                        .addContext(ErrorContextKeys.MEMBER_ID, memberId)
                        .addContext(ErrorContextKeys.CHALLENGE_ID, challengeId)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "challengeParticipant")
                        .addContext(ErrorContextKeys.OPERATION, "findByChallengeIdAndMemberId"));
    }
}
