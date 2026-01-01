package me.bombom.api.v1.challenge.service;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.article.domain.Article;
import me.bombom.api.v1.article.repository.ArticleRepository;
import me.bombom.api.v1.challenge.domain.ChallengeComment;
import me.bombom.api.v1.challenge.domain.ChallengeComment.ChallengeCommentBuilder;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import me.bombom.api.v1.challenge.dto.request.ChallengeCommentOptionsRequest;
import me.bombom.api.v1.challenge.dto.request.ChallengeCommentRequest;
import me.bombom.api.v1.challenge.dto.response.ChallengeCommentCandidateArticleResponse;
import me.bombom.api.v1.challenge.dto.response.ChallengeCommentResponse;
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
public class ChallengeCommentService {

    private final ChallengeCommentRepository challengeCommentRepository;
    private final ChallengeParticipantRepository challengeParticipantRepository;
    private final ArticleRepository articleRepository;

    public Page<ChallengeCommentResponse> getChallengeComments(
            Long challengeId,
            Long memberId,
            ChallengeCommentOptionsRequest request,
            Pageable pageable
    ){
        ChallengeParticipant participant = challengeParticipantRepository.findByChallengeIdAndMemberId(challengeId, memberId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.FORBIDDEN_RESOURCE)
                    .addContext(ErrorContextKeys.MEMBER_ID, memberId)
                    .addContext(ErrorContextKeys.OPERATION, "findByChallengeIdAndMemberId"));
        return challengeCommentRepository.findAllByTeamInDuration(
                participant.getChallengeTeamId(),
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
        ChallengeParticipant participant = challengeParticipantRepository.findByChallengeIdAndMemberId(challengeId,
                        memberId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.MEMBER_ID, memberId)
                        .addContext(ErrorContextKeys.CHALLENGE_ID, challengeId)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "challengeParticipant")
                        .addContext(ErrorContextKeys.OPERATION, "findByChallengeIdAndMemberId"));

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
        // TODO : 이벤트 발행 후 완료 처리 필요
    }
}
