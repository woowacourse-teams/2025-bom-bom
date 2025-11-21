package me.bombom.api.v1.subscribe.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.auth.dto.CustomOAuth2User;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.newsletter.domain.Category;
import me.bombom.api.v1.newsletter.domain.Newsletter;
import me.bombom.api.v1.newsletter.domain.NewsletterDetail;
import me.bombom.api.v1.newsletter.repository.CategoryRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterDetailRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterRepository;
import me.bombom.api.v1.subscribe.domain.Subscribe;
import me.bombom.api.v1.subscribe.repository.SubscribeRepository;
import me.bombom.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@AutoConfigureMockMvc
class SubscribeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private SubscribeRepository subscribeRepository;

    @Autowired
    private NewsletterRepository newsletterRepository;

    @Autowired
    private NewsletterDetailRepository newsletterDetailRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Member member;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        subscribeRepository.deleteAllInBatch();
        newsletterRepository.deleteAllInBatch();
        newsletterDetailRepository.deleteAllInBatch();
        categoryRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();


        member = TestFixture.normalMemberFixture();
        memberRepository.save(member);

        Map<String, Object> attributes = Map.of(
                "id", member.getId().toString(),
                "email", member.getEmail(),
                "name", member.getNickname()
        );
        CustomOAuth2User customOAuth2User = new CustomOAuth2User(attributes, member, null, null);

        authentication = new OAuth2AuthenticationToken(
                customOAuth2User,
                customOAuth2User.getAuthorities(),
                "registrationId"
        );
    }

    @Test
    void 내가_구독한_뉴스레터_목록을_조회한다() throws Exception {
        // given
        Category category = categoryRepository.save(TestFixture.createCategory());
        NewsletterDetail newsletterDetail = newsletterDetailRepository.save(TestFixture.createNewsletterDetail(true));
        Newsletter newsletter = newsletterRepository.save(TestFixture.createNewsletter("Test Newsletter", "test@test.com", category.getId(), newsletterDetail.getId()));
        subscribeRepository.save(TestFixture.createSubscribe(newsletter, member));

        // when & then
        mockMvc.perform(get("/api/v1/members/me/subscriptions")
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    void 구독을_취소한다() throws Exception {
        // given
        Category category = categoryRepository.save(TestFixture.createCategory());
        NewsletterDetail newsletterDetail = newsletterDetailRepository.save(TestFixture.createNewsletterDetail(true));
        Newsletter newsletter = newsletterRepository.save(TestFixture.createNewsletter("Test Newsletter", "test@test.com", category.getId(), newsletterDetail.getId()));
        Subscribe subscribe = subscribeRepository.save(TestFixture.createSubscribe(newsletter, member));

        // when & then
        mockMvc.perform(post("/api/v1/members/me/subscriptions/{subscriptionId}/unsubscribe", subscribe.getId())
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    void 존재하지_않는_구독을_취소_시도_시_404_예외가_발생한다() throws Exception {
        // given
        Long nonExistentSubscriptionId = 999L;

        // when & then
        mockMvc.perform(post("/api/v1/members/me/subscriptions/{subscriptionId}/unsubscribe", nonExistentSubscriptionId)
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    void 다른_회원의_구독을_취소_시도_시_403_예외가_발생한다() throws Exception {
        // given
        Member otherMember = TestFixture.createMemberFixture("other@email.com", "other-nickname");
        memberRepository.save(otherMember);

        Category category = categoryRepository.save(TestFixture.createCategory());
        NewsletterDetail newsletterDetail = newsletterDetailRepository.save(TestFixture.createNewsletterDetail(true));
        Newsletter newsletter = newsletterRepository.save(TestFixture.createNewsletter("Test Newsletter", "test@test.com", category.getId(), newsletterDetail.getId()));
        Subscribe otherMemberSubscribe = subscribeRepository.save(TestFixture.createSubscribe(newsletter, otherMember));

        // when & then
        mockMvc.perform(post("/api/v1/members/me/subscriptions/{subscriptionId}/unsubscribe", otherMemberSubscribe.getId())
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andDo(print());
    }
}
