package me.bombom.api.v1.challenge.service;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.article.domain.Article;
import me.bombom.api.v1.article.repository.ArticleRepository;
import me.bombom.api.v1.challenge.domain.ChallengeComment;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import me.bombom.api.v1.challenge.dto.request.ChallengeCommentOptionsRequest;
import me.bombom.api.v1.challenge.dto.request.ChallengeCommentRequest;
import me.bombom.api.v1.challenge.dto.request.UpdateChallengeCommentRequest;
import me.bombom.api.v1.challenge.dto.response.ChallengeCommentCandidateArticleResponse;
import me.bombom.api.v1.challenge.dto.response.ChallengeCommentHighlightResponse;
import me.bombom.api.v1.challenge.dto.response.ChallengeCommentLikeResponse;
import me.bombom.api.v1.challenge.dto.response.ChallengeCommentResponse;
import me.bombom.api.v1.challenge.event.CreateChallengeCommentEvent;
import me.bombom.api.v1.challenge.repository.ChallengeCommentLikeRepository;
import me.bombom.api.v1.challenge.repository.ChallengeCommentRepository;
import me.bombom.api.v1.challenge.repository.ChallengeParticipantRepository;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.highlight.repository.HighlightRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengeCommentService {

    private final ChallengeCommentRepository challengeCommentRepository;
    private final ChallengeParticipantRepository challengeParticipantRepository;
    private final ChallengeCommentLikeRepository challengeCommentLikeRepository;
    private final ArticleRepository articleRepository;
    private final HighlightRepository highlightRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final Clock clock;

    @Value("${challenge.highlight.truncate-ratio}")
    private double highlightTruncateRatio;

    public Page<ChallengeCommentResponse> getChallengeComments(
            Long challengeId,
            Long memberId,
            ChallengeCommentOptionsRequest request,
            Pageable pageable
    ) {
        validateParticipant(challengeId, memberId);

        return challengeCommentRepository.findAllInDuration(
                challengeId,
                memberId,
                request.start(),
                request.end(),
                pageable
        );
    }

    public List<ChallengeCommentCandidateArticleResponse> getChallengeCommentCandidateArticles(Long memberId, LocalDate date) {
        return articleRepository.findChallengeCommentCandidateArticles(
                memberId,
                date.atStartOfDay(),
                date.plusDays(1).atStartOfDay()
        );
    }

    @Transactional
    public void createChallengeComment(
            Long memberId,
            Long challengeId,
            ChallengeCommentRequest request
    ) {
        validateCommentAvailableDay(memberId, challengeId);
        ChallengeParticipant participant = getChallengeParticipant(memberId, challengeId);

        Article article = articleRepository.findById(request.articleId())
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.MEMBER_ID, memberId)
                        .addContext(ErrorContextKeys.ARTICLE_ID, request.articleId())
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "article"));

        ChallengeComment comment = ChallengeComment.builder()
                .newsletterId(article.getNewsletterId())
                .participantId(participant.getId())
                .articleTitle(article.getTitle())
                .quotation(request.quotation())
                .comment(request.comment())
                .build();

        challengeCommentRepository.save(comment);
        applicationEventPublisher.publishEvent(new CreateChallengeCommentEvent(participant.getId()));
    }

    public Page<ChallengeCommentHighlightResponse> getChallengeArticleHighlights(
            Long memberId,
            Long articleId,
            Pageable pageable
    ) {
        return highlightRepository.findChallengeArticleHighlights(
                memberId,
                articleId,
                highlightTruncateRatio,
                pageable
        );
    }

    @Transactional
    public void updateChallengeComment(
            Long memberId,
            Long challengeId,
            Long commentId,
            UpdateChallengeCommentRequest request
    ) {
        ChallengeParticipant participant = getChallengeParticipant(memberId, challengeId);

        ChallengeComment comment = challengeCommentRepository.findById(commentId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.MEMBER_ID, memberId)
                        .addContext(ErrorContextKeys.CHALLENGE_ID, challengeId)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "challengeComment")
                        .addContext(ErrorContextKeys.OPERATION, "findById"));

        if (!comment.getParticipantId().equals(participant.getId())) {
            throw new CIllegalArgumentException(ErrorDetail.FORBIDDEN_RESOURCE)
                    .addContext(ErrorContextKeys.MEMBER_ID, memberId)
                    .addContext(ErrorContextKeys.CHALLENGE_ID, challengeId)
                    .addContext(ErrorContextKeys.ACTUAL_OWNER_ID, comment.getParticipantId())
                    .addContext(ErrorContextKeys.OPERATION, "updateChallengeComment");
        }

        comment.updateComment(request.comment());
    }

    @Transactional
    public ChallengeCommentLikeResponse addChallengeCommentLike(Long memberId, Long challengeId, Long commentId) {
        ChallengeParticipant participant = getChallengeParticipant(memberId, challengeId);
        validateComment(commentId);

        int insertCount = challengeCommentLikeRepository.insertIgnoreByParticipantIdAndCommentId(
                participant.getId(),
                commentId
        );

        if (insertCount == 1) {
            challengeCommentRepository.incrementLikeCountNotBelowZero(commentId, 1);
        }

        return ChallengeCommentLikeResponse.from(challengeCommentRepository.findById(commentId).get());
    }

    @Transactional
    public ChallengeCommentLikeResponse deleteChallengeCommentLike(Long memberId, Long challengeId, Long commentId) {
        ChallengeParticipant participant = getChallengeParticipant(memberId, challengeId);
        validateComment(commentId);

        int deleteCount = challengeCommentLikeRepository.deleteByParticipantIdAndCommentId(
                participant.getId(),
                commentId
        );

        if (deleteCount == 1) {
            challengeCommentRepository.incrementLikeCountNotBelowZero(commentId, -1);
        }

        return ChallengeCommentLikeResponse.from(challengeCommentRepository.findById(commentId).get());
    }

    private void validateParticipant(Long challengeId, Long memberId) {
        if (!challengeParticipantRepository.existsByChallengeIdAndMemberId(challengeId, memberId)) {
            throw new CIllegalArgumentException(ErrorDetail.FORBIDDEN_RESOURCE)
                    .addContext(ErrorContextKeys.MEMBER_ID, memberId)
                    .addContext(ErrorContextKeys.CHALLENGE_ID, challengeId)
                    .addContext(ErrorContextKeys.OPERATION, "existsByChallengeIdAndMemberId");
        }
    }

    private ChallengeParticipant getChallengeParticipant(Long memberId, Long challengeId) {
        return challengeParticipantRepository.findByChallengeIdAndMemberId(challengeId, memberId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.FORBIDDEN_RESOURCE)
                        .addContext(ErrorContextKeys.MEMBER_ID, memberId)
                        .addContext(ErrorContextKeys.CHALLENGE_ID, challengeId)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "challengeParticipant")
                        .addContext(ErrorContextKeys.OPERATION, "findByChallengeIdAndMemberId"));
    }

    private void validateCommentAvailableDay(Long memberId, Long challengeId) {
        if (isWeekend(LocalDate.now(clock))) {
            throw new CIllegalArgumentException(ErrorDetail.PRECONDITION_FAILED)
                    .addContext(ErrorContextKeys.MEMBER_ID, memberId)
                    .addContext(ErrorContextKeys.CHALLENGE_ID, challengeId)
                    .addContext(ErrorContextKeys.OPERATION, "createChallengeComment")
                    .addContext(ErrorContextKeys.DETAIL, "주말에는 챌린지 코멘트를 작성할 수 없습니다.");
        }
    }

    private void validateComment(Long commentId) {
        if (!challengeCommentRepository.existsById(commentId)) {
            throw new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext(ErrorContextKeys.COMMENT_ID, commentId)
                    .addContext(ErrorContextKeys.OPERATION, "existsById");
        }
    }

    private boolean isWeekend(LocalDate today) {
        DayOfWeek dayOfWeek = today.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }
}
