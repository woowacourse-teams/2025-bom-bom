package me.bombom.api.v1.subscribe.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.newsletter.domain.Category;
import me.bombom.api.v1.newsletter.domain.Newsletter;
import me.bombom.api.v1.newsletter.domain.NewsletterDetail;
import me.bombom.api.v1.newsletter.repository.CategoryRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterDetailRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterRepository;
import me.bombom.api.v1.subscribe.domain.Subscribe;
import me.bombom.api.v1.subscribe.dto.SubscribedNewsletterResponse;
import me.bombom.api.v1.subscribe.repository.SubscribeRepository;
import me.bombom.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@IntegrationTest
class SubscribeServiceTest {

    @Autowired
    private SubscribeService subscribeService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private NewsletterRepository newsletterRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SubscribeRepository subscribeRepository;
    @Autowired
    private NewsletterDetailRepository newsletterDetailRepository;

    @Test
    void 구독중인_뉴스레터를_조회한다() {
        // given
        Member member = TestFixture.normalMemberFixture();
        memberRepository.save(member);

        List<Category> categories = TestFixture.createCategories();
        categoryRepository.saveAll(categories);

        List<NewsletterDetail> newsletterDetails = TestFixture.createNewsletterDetails();
        newsletterDetailRepository.saveAll(newsletterDetails);
        List<Newsletter> newsletters = TestFixture.createNewslettersWithDetails(categories, newsletterDetails);
        newsletterRepository.saveAll(newsletters);

        subscribeRepository.saveAll(List.of(
                Subscribe.builder().memberId(member.getId()).newsletterId(newsletters.getFirst().getId()).build(),
                Subscribe.builder().memberId(member.getId()).newsletterId(newsletters.getLast().getId()).build()
        ));

        // when
        List<SubscribedNewsletterResponse> result = subscribeService.getSubscribedNewsletters(member);

        // then
        assertThat(result).hasSize(2)
            .extracting("name")
            .containsExactlyInAnyOrder(newsletters.getFirst().getName(), newsletters.getLast().getName());
    }
}
