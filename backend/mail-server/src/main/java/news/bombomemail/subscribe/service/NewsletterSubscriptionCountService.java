package news.bombomemail.subscribe.service;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import news.bombomemail.member.domain.Member;
import news.bombomemail.member.repository.MemberRepository;
import news.bombomemail.subscribe.domain.NewsletterSubscriptionCount;
import news.bombomemail.subscribe.repository.NewsletterSubscriptionCountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
        if (member.getBirthDate() == null) {
            throw new IllegalArgumentException("멤버의 생년월일이 없습니다.");
        }
        int birthYear = member.getBirthDate()
                .getYear();
        NewsletterSubscriptionCount newsletterSubscriptionCount = newsletterSubscriptionCountRepository.findByNewsletterId(newsletterId)
                .orElseGet(() -> newsletterSubscriptionCountRepository.save(NewsletterSubscriptionCount.from(newsletterId)));
        newsletterSubscriptionCount.incrementByDecade(NewsletterSubscriptionCount.toDecadeBucket(LocalDate.now().getYear(), birthYear));
    }
}
