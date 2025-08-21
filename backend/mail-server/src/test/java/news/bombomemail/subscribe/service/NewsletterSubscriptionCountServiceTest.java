package news.bombomemail.subscribe.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDate;
import java.util.Optional;
import news.bombomemail.member.domain.Gender;
import news.bombomemail.member.domain.Member;
import news.bombomemail.member.repository.MemberRepository;
import news.bombomemail.newsletter.domain.Newsletter;
import news.bombomemail.newsletter.repository.NewsletterRepository;
import news.bombomemail.subscribe.domain.NewsletterSubscriptionCount;
import news.bombomemail.subscribe.repository.NewsletterSubscriptionCountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.transaction.annotation.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class NewsletterSubscriptionCountServiceTest {

    @Autowired
    private NewsletterSubscriptionCountService service;

    @Autowired
    private NewsletterSubscriptionCountRepository repository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private NewsletterRepository newsletterRepository;

    private Member testMember;
    private Newsletter testNewsletter;

    @BeforeEach
    @Transactional
    void setup() {
        // 각 테스트마다 고유한 값을 생성
        String uniqueId = String.valueOf(System.currentTimeMillis());
        
        testMember = memberRepository.save(Member.builder()
                .email("test" + uniqueId + "@example.com")
                .nickname("테스트멤버" + uniqueId)
                .providerId("test" + uniqueId)
                .provider("google")
                .gender(Gender.MALE)
                .roleId(1L)
                .birthDate(LocalDate.of(1990, 1, 1)) // 30대
                .build());

        testNewsletter = newsletterRepository.save(Newsletter.builder()
                .name("테스트뉴스레터" + uniqueId)
                .description("설명")
                .imageUrl("이미지")
                .email("newsletter" + uniqueId + "@example.com")
                .categoryId(1L)
                .detailId(1L)
                .build());
    }

    @Test
    void 뉴스레터_구독_수_정상_증가() {
        // given & when
        service.updateNewsletterSubscriptionCount(testNewsletter.getId(), testMember.getId());

        // then
        Optional<NewsletterSubscriptionCount> result = repository.findByNewsletterId(testNewsletter.getId());
        assertSoftly(softly -> {
            softly.assertThat(result).isPresent();
            NewsletterSubscriptionCount count = result.get();
            softly.assertThat(count.getTotal()).isEqualTo(1);
            softly.assertThat(count.getAge30s()).isEqualTo(1); // 1990년생 = 30대
            softly.assertThat(count.getAge0s()).isEqualTo(0);
            softly.assertThat(count.getAge10s()).isEqualTo(0);
            softly.assertThat(count.getAge20s()).isEqualTo(0);
            softly.assertThat(count.getAge40s()).isEqualTo(0);
            softly.assertThat(count.getAge50s()).isEqualTo(0);
            softly.assertThat(count.getAge60Plus()).isEqualTo(0);
        });
    }

    @Test
    void 기존_뉴스레터_구독_수에_추가_증가() {
        // given
        NewsletterSubscriptionCount existing = repository.save(NewsletterSubscriptionCount.builder()
                .newsletterId(testNewsletter.getId())
                .total(5)
                .age20s(3)
                .age30s(2)
                .build());

        // when
        service.updateNewsletterSubscriptionCount(testNewsletter.getId(), testMember.getId());

        // then
        NewsletterSubscriptionCount result = repository.findById(existing.getId()).orElseThrow();
        assertSoftly(softly -> {
            softly.assertThat(result.getTotal()).isEqualTo(6); // 5 + 1
            softly.assertThat(result.getAge30s()).isEqualTo(3); // 2 + 1
            softly.assertThat(result.getAge20s()).isEqualTo(3); // 기존 값 유지
        });
    }

    @Test
    void 존재하지_않는_멤버_ID로_호출시_예외_발생() {
        // given
        Long nonExistentMemberId = 999L;

        // when & then
        assertThatThrownBy(() -> service.updateNewsletterSubscriptionCount(testNewsletter.getId(), nonExistentMemberId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("멤버가 존재하지 않습니다.");
    }

    @Test
    void 서비스_메서드_호출시_예외_발생하지_않음() {
        // given & when & then
        assertThatCode(() -> service.updateNewsletterSubscriptionCount(testNewsletter.getId(), testMember.getId()))
                .doesNotThrowAnyException();
    }
}
