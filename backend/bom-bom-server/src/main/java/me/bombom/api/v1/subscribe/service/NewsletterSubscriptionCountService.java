package me.bombom.api.v1.subscribe.service;

import java.time.Clock;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.subscribe.domain.AgeGroup;
import me.bombom.api.v1.subscribe.repository.NewsletterSubscriptionCountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsletterSubscriptionCountService {

    private final Clock clock;
    private final MemberRepository memberRepository;
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

    @Transactional
    public void decreaseNewsletterSubscriptionCount(Long newsletterId, LocalDate birthDate) {
        if (birthDate == null) {
            log.debug("멤버의 생년월일이 없어 구독자 수가 감소되지 않습니다.");
            return;
        }

        AgeGroup group = AgeGroup.fromBirthYear(LocalDate.now(clock).getYear(), birthDate.getYear());
        newsletterSubscriptionCountRepository.decreaseSubscriptionCountByNewsletterIdAndAgeGroup(
                newsletterId,
                group.getDbKey()
        );
    }

    @Transactional
    public void decreaseNewsletterSubscriptionCountByMemberId(Long newsletterId, Long memberId) {
        Member member = memberRepository.findById(memberId).orElse(null);
        if (member == null) {
            log.warn("멤버 정보가 존재하지 않아 구독자 수를 감소시키지 않습니다. memberId: {}, newsletterId: {}", memberId, newsletterId);
            return;
        }

        decreaseNewsletterSubscriptionCount(newsletterId, member.getBirthDate());
    }
}
