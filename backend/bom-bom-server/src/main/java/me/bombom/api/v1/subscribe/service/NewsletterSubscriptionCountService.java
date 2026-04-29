package me.bombom.api.v1.subscribe.service;

import java.time.Clock;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.subscribe.domain.AgeGroup;
import me.bombom.api.v1.subscribe.repository.NewsletterSubscriptionCountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsletterSubscriptionCountService {

    private final Clock clock;
    private final NewsletterSubscriptionCountRepository newsletterSubscriptionCountRepository;

    @Transactional
    public void updateNewsletterSubscriptionCount(Long newsletterId, LocalDate birthDate) {
        if (birthDate == null) {
            log.debug("멤버의 생년월일이 없어 구독자 수가 업데이트 되지 않습니다.");
            return;
        }

        AgeGroup group = AgeGroup.fromBirthYear(LocalDate.now(clock).getYear(), birthDate.getYear());
        newsletterSubscriptionCountRepository.increaseSubscriptionCountByNewsletterIdAndAgeGroup(
                newsletterId,
                group.getDbKey()
        );
    }
}
