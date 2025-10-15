package me.bombom.api.v1.subscribe.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.newsletter.domain.Category;
import me.bombom.api.v1.newsletter.domain.Newsletter;
import me.bombom.api.v1.newsletter.repository.CategoryRepository;
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

    @Test
    void 구독중인_뉴스레터를_조회한다() {
        // given
        Member member = TestFixture.normalMemberFixture();
        memberRepository.save(member);

        Category category = TestFixture.createCategory();
        categoryRepository.save(category);

        Newsletter newsletter1 = TestFixture.createNewsletter("newsletter1", "email1@test.com", category.getId());
        Newsletter newsletter2 = TestFixture.createNewsletter("newsletter2", "email2@test.com", category.getId());
        Newsletter newsletter3 = TestFixture.createNewsletter("newsletter3", "email3@test.com", category.getId());
        newsletterRepository.saveAll(List.of(newsletter1, newsletter2, newsletter3));

        subscribeRepository.saveAll(List.of(
                Subscribe.builder().memberId(member.getId()).newsletterId(newsletter1.getId()).build(),
                Subscribe.builder().memberId(member.getId()).newsletterId(newsletter3.getId()).build()
        ));

        // when
        List<SubscribedNewsletterResponse> result = subscribeService.getSubscribedNewsletters(member);

        // then
        assertThat(result).hasSize(2)
            .extracting("name")
            .containsExactlyInAnyOrder("newsletter1", "newsletter3");
    }
}
