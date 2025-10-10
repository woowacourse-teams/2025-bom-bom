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
        
        int birthYear = member.getBirthDate().getYear();
        int decade = NewsletterSubscriptionCount.toDecadeBucket(LocalDate.now().getYear(), birthYear);
        
        // MySQL INSERT ON DUPLICATE KEY UPDATE를 사용한 원자적 UPSERT 연산
        incrementCountByDecade(newsletterId, decade);
    }
    
    private void incrementCountByDecade(Long newsletterId, int decade) {
        String ageGroup = switch (decade) {
            case 0 -> "age0s";
            case 1 -> "age10s";
            case 2 -> "age20s";
            case 3 -> "age30s";
            case 4 -> "age40s";
            case 5 -> "age50s";
            default -> "age60plus";
        };
        newsletterSubscriptionCountRepository.incrementAge(newsletterId, ageGroup);
    }
}
