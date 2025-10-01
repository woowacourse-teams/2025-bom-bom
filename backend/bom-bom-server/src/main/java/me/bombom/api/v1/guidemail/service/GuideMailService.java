package me.bombom.api.v1.guidemail.service;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.member.domain.Member;
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
    public void updateReadScore(Member member) {
        Long memberId = member.getId();
        LocalDate registerDate = member.getCreatedAt().toLocalDate();
        boolean isRegisterDay = isRegisterDay(registerDate);
        if (isRegisterDay) {
            petService.increaseCurrentScoreForGuideMail(memberId, ScorePolicyConstants.ARTICLE_READING_SCORE);
        }
        readingService.updateReadingCountForGuideMail(memberId, isRegisterDay);
    }

    private boolean isRegisterDay(LocalDate registerDate) {
        return registerDate.isEqual(LocalDate.now());
    }
}
