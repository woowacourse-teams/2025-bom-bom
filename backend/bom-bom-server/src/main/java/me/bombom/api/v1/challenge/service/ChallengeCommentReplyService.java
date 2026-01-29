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
    public void createCommentReply(Long commentId, Long memberId, CreateCommentReplyRequest request) {
        validateComment(commentId);
        ChallengeParticipant commentAuthor = getCommentAuthorByCommentId(commentId);
        ChallengeParticipant replyAuthor = getParticipantWithChallengeId(memberId, commentAuthor.getChallengeId());

        challengeCommentReplyRepository.save(
                ChallengeCommentReply.builder()
                        .commentId(commentId)
                        .participantId(replyAuthor.getId())
                        .reply(request.reply())
                        .build()
        );
    }

    public Page<CommentReplyResponse> getCommentReplies(Long memberId, Long commentId, Pageable pageable) {
        validateComment(commentId);
        validateReplyVisible(commentId, memberId);
        return challengeCommentReplyRepository.findAllByCommentId(commentId, memberId, pageable);
    }

    private void validateComment(Long commentId) {
        if (!challengeCommentRepository.existsById(commentId)) {
            throw new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext(ErrorContextKeys.COMMENT_ID, commentId)
                    .addContext(ErrorContextKeys.OPERATION, "existsById");
        }
    }

    private ChallengeParticipant getCommentAuthorByCommentId(Long commentId) {
        return challengeParticipantRepository.findAuthorByCommentId(commentId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.COMMENT_ID, commentId)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "challengeParticipant")
                        .addContext(ErrorContextKeys.OPERATION, "findAuthorByCommentId"));
    }

    private ChallengeParticipant getParticipantWithChallengeId(Long memberId, Long challengeId) {
        return challengeParticipantRepository.findByChallengeIdAndMemberId(challengeId, memberId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.FORBIDDEN_RESOURCE)
                        .addContext(ErrorContextKeys.MEMBER_ID, memberId)
                        .addContext(ErrorContextKeys.CHALLENGE_ID, challengeId)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "challengeParticipant")
                        .addContext(ErrorContextKeys.OPERATION, "findByChallengeIdAndMemberId"));
    }

    private void validateReplyVisible(Long commentId, Long memberId) {
        if (!challengeCommentRepository.existsVisibleToMember(commentId, memberId)) {
            throw new CIllegalArgumentException(ErrorDetail.FORBIDDEN_RESOURCE)
                    .addContext(ErrorContextKeys.COMMENT_ID, commentId)
                    .addContext(ErrorContextKeys.MEMBER_ID, memberId)
                    .addContext(ErrorContextKeys.OPERATION, "existsVisibleToMember");

        }
    }
}
