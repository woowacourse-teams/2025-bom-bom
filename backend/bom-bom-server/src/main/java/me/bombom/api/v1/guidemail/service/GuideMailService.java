package me.bombom.api.v1.guidemail.service;

import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.pet.ScorePolicyConstants;
import me.bombom.api.v1.pet.service.PetService;
import me.bombom.api.v1.reading.service.ReadingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GuideMailService {

    private final PetService petService;
    private final ReadingService readingService;

    @Transactional
    public void updateReadScore(Long memberId) {
        petService.increaseCurrentScoreForGuideMail(memberId, ScorePolicyConstants.ARTICLE_READING_SCORE);
        readingService.updateReadingCountForGuideMail(memberId);
    }
}
