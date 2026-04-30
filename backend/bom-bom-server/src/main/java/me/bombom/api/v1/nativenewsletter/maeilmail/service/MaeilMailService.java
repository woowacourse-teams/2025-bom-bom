package me.bombom.api.v1.nativenewsletter.maeilmail.service;

import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailContent;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailContentAnswer;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailIssueHistory;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailUserAnswer;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.MaeilMailIdealAnswerResponse;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.MaeilMailInformationResponse;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.MaeilMailSubmitAnswerRequest;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.MaeilMailSubmittedAnswerResponse;
import me.bombom.api.v1.nativenewsletter.maeilmail.repository.MaeilMailContentAnswerRepository;
import me.bombom.api.v1.nativenewsletter.maeilmail.repository.MaeilMailContentRepository;
import me.bombom.api.v1.nativenewsletter.maeilmail.repository.MaeilMailIssueHistoryRepository;
import me.bombom.api.v1.nativenewsletter.maeilmail.repository.MaeilMailUserAnswerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MaeilMailService {

    private final MaeilMailContentRepository contentRepository;
    private final MaeilMailContentAnswerRepository contentAnswerRepository;
    private final MaeilMailUserAnswerRepository userAnswerRepository;
    private final MaeilMailIssueHistoryRepository issueHistoryRepository;

    public MaeilMailIdealAnswerResponse getIdealAnswer(Long contentId) {
        MaeilMailContent content = getContent(contentId);
        MaeilMailContentAnswer answer = getContentAnswer(contentId);
        return new MaeilMailIdealAnswerResponse(content.getTitle(), answer.getAnswer());
    }

    @Transactional
    public void submitAnswer(
            Member member,
            Long contentId,
            MaeilMailSubmitAnswerRequest request
    ) {
        MaeilMailIssueHistory issueHistory = getIssueHistoryByContentId(contentId);

        MaeilMailUserAnswer userAnswer = MaeilMailUserAnswer.builder()
                .issueHistoryId(issueHistory.getId())
                .memberId(member.getId())
                .answer(request.answer())
                .build();
        userAnswerRepository.save(userAnswer);
    }

    public MaeilMailSubmittedAnswerResponse getSubmittedAnswer(Member member, Long contentId) {
        // TODO: List<MaeilMailIssueHistory> | List<Integer> 로 반환
        MaeilMailIssueHistory issueHistory = getIssueHistoryByContentId(contentId);
        // TODO: List<MaeilMailUserAnswer> 로 반환
        MaeilMailUserAnswer userAnswer = getUserAnswer(member, contentId, issueHistory);
        return new MaeilMailSubmittedAnswerResponse(userAnswer.getAnswer());
    }

    public MaeilMailInformationResponse getContentInformationByArticle(Long articleId) {
        MaeilMailIssueHistory issueHistory = getIssueHistoryByArticleId(articleId);
        return MaeilMailInformationResponse.from(issueHistory);
    }

    private MaeilMailContent getContent(Long contentId) {
        return contentRepository.findById(contentId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "MaeilMailContent")
                        .addContext(ErrorContextKeys.OPERATION, "findById")
                        .addContext(ErrorContextKeys.CONTENT_ID, contentId));
    }

    private MaeilMailContentAnswer getContentAnswer(Long contentId) {
        return contentAnswerRepository.findByContentId(contentId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "MaeilMailContentAnswer")
                        .addContext(ErrorContextKeys.OPERATION, "findByContentId")
                        .addContext(ErrorContextKeys.CONTENT_ID, contentId));
    }

    private MaeilMailIssueHistory getIssueHistoryByArticleId(Long articleId) {
        return issueHistoryRepository.findByArticleId(articleId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "MaeilMailIssueHistory")
                        .addContext(ErrorContextKeys.OPERATION, "findByArticleId")
                        .addContext(ErrorContextKeys.ARTICLE_ID, articleId));
    }

    private MaeilMailIssueHistory getIssueHistoryByContentId(Long contentId) {
        return issueHistoryRepository.findByContentId(contentId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "MaeilMailIssueHistory")
                        .addContext(ErrorContextKeys.OPERATION, "findByContentId")
                        .addContext(ErrorContextKeys.CONTENT_ID, contentId));
    }

    private MaeilMailUserAnswer getUserAnswer(Member member, Long contentId, MaeilMailIssueHistory issueHistory) {
        return userAnswerRepository.findByMemberIdAndIssueHistoryId(member.getId(), issueHistory.getId())
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "MaeilMailUserAnswer")
                        .addContext(ErrorContextKeys.OPERATION, "findByMemberIdAndIssueHistoryId")
                        .addContext(ErrorContextKeys.MEMBER_ID, member.getId())
                        .addContext(ErrorContextKeys.ARTICLE_ID, contentId));
    }
}
