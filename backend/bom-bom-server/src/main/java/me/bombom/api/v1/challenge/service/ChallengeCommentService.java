package me.bombom.api.v1.challenge.service;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.article.repository.ArticleRepository;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import me.bombom.api.v1.challenge.dto.request.ChallengeCommentOptionsRequest;
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
}
