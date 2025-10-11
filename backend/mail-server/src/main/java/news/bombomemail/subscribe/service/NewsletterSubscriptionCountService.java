package news.bombomemail.subscribe.service;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import lombok.extern.log4j.Log4j2;
import news.bombomemail.member.domain.Member;
import news.bombomemail.member.repository.MemberRepository;
import news.bombomemail.subscribe.domain.AgeGroup;
import news.bombomemail.subscribe.domain.NewsletterSubscriptionCount;
import news.bombomemail.subscribe.repository.NewsletterSubscriptionCountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NewsletterSubscriptionCountService {

    private final NewsletterSubscriptionCountRepository newsletterSubscriptionCountRepository;
    private final MemberRepository memberRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateNewsletterSubscriptionCount(Long newsletterId, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("멤버가 존재하지 않습니다."));
        if (!member.hasBirthDate()) {
            log.debug("멤버의 생년월일이 없어 구독자 수가 업데이트 되지 않습니다.");
            return;
        }
        
        int birthYear = member.getBirthDate().getYear();
        AgeGroup group = AgeGroup.fromBirthYear(LocalDate.now().getYear(), birthYear);

        // MySQL INSERT ON DUPLICATE KEY UPDATE를 사용한 원자적 UPSERT 연산
        newsletterSubscriptionCountRepository.incrementSubscriptionCountByNewsletterIdAndAgeGroup(newsletterId, group.getDbKey());
    }
}
