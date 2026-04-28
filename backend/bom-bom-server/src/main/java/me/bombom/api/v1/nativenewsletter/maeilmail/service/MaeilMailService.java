package me.bombom.api.v1.nativenewsletter.maeilmail.service;

import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailContent;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailContentAnswer;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.MaeilMailIdealAnswerResponse;
import me.bombom.api.v1.nativenewsletter.maeilmail.repository.MaeilMailContentAnswerRepository;
import me.bombom.api.v1.nativenewsletter.maeilmail.repository.MaeilMailContentRepository;
import me.bombom.api.v1.nativenewsletter.maeilmail.repository.MaeilMailIssueHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MaeilMailService {

    private final MaeilMailContentRepository contentRepository;
    private final MaeilMailContentAnswerRepository contentAnswerRepository;
    private final MaeilMailIssueHistoryRepository issueHistoryRepository;

    public MaeilMailIdealAnswerResponse getIdealAnswer(Long contentId) {
        MaeilMailContent content = contentRepository.findById(contentId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "maeilMailContent")
                        .addContext(ErrorContextKeys.OPERATION, "findById")
                        .addContext(ErrorContextKeys.CONTENT_ID, contentId));

        MaeilMailContentAnswer answer = contentAnswerRepository.findByContentId(contentId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "maeilMailContentAnswer")
                        .addContext(ErrorContextKeys.OPERATION, "findByContentId")
                        .addContext(ErrorContextKeys.CONTENT_ID, contentId));

        return new MaeilMailIdealAnswerResponse(content.getTitle(), answer.getAnswer());
    }
}
