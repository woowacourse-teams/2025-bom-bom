package me.bombom.api.v1.newsletter.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.newsletter.domain.Category;
import me.bombom.api.v1.newsletter.domain.Newsletter;
import me.bombom.api.v1.newsletter.domain.NewsletterDetail;
import me.bombom.api.v1.newsletter.domain.NewsletterPublicationStatus;
import me.bombom.api.v1.newsletter.repository.CategoryRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterDetailRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterRepository;
import me.bombom.api.v1.subscribe.domain.NewsletterSubscriptionCount;
import me.bombom.api.v1.subscribe.repository.NewsletterSubscriptionCountRepository;
import me.bombom.api.v1.subscribe.repository.SubscribeRepository;
import me.bombom.support.IntegrationTest;
import me.bombom.support.MutableClock;
import me.bombom.support.acceptance.AcceptanceTestHeaders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
class NewsletterControllerTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 6, 21);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NewsletterRepository newsletterRepository;

    @Autowired
    private NewsletterDetailRepository newsletterDetailRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private NewsletterSubscriptionCountRepository newsletterSubscriptionCountRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private SubscribeRepository subscribeRepository;

    @Autowired
    private MutableClock clock;

    private List<Newsletter> newsletters;
    private List<Category> categories;
    private Member member;

    @BeforeEach
    void setUp() {
        clock.setDate(TODAY);
        List<NewsletterDetail> details = newsletterDetailRepository.saveAll(TestFixture.createNewsletterDetails());
        categories = categoryRepository.saveAll(TestFixture.createCategories());
        newsletters = newsletterRepository.saveAll(TestFixture.createNewslettersWithDetails(categories, details));
        List<NewsletterSubscriptionCount> counts = TestFixture.createNewsletterSubscriptionCounts(newsletters);
        newsletterSubscriptionCountRepository.saveAll(counts);

        member = memberRepository.save(TestFixture.createUniqueMember("newsletter-user", "newsletter-controller"));
        subscribeRepository.save(TestFixture.createSubscribe(newsletters.getFirst(), member));
    }

    @Test
    void 비로그인_사용자는_전체_뉴스레터를_구독하지_않은_상태로_조회한다() throws Exception {
        mockMvc.perform(get("/api/v1/newsletters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newsletters.length()").value(4))
                .andExpect(jsonPath("$.newsletters[*].isSubscribed")
                        .value(org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.is(false))))
                .andExpect(jsonPath("$.newsletters[*].source")
                        .value(org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.is("EXTERNAL"))));
    }

    @Test
    void 로그인_사용자는_구독_여부와_함께_뉴스레터를_조회한다() throws Exception {
        mockMvc.perform(get("/api/v1/newsletters")
                .header(AcceptanceTestHeaders.MEMBER_ID, member.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newsletters[*].isSubscribed")
                        .value(org.hamcrest.Matchers.hasItem(true)));
    }

    @Test
    void 뉴스레터_상세와_구독_여부를_조회한다() throws Exception {
        Newsletter newsletter = newsletters.getFirst();

        mockMvc.perform(get("/api/v1/newsletters/{id}", newsletter.getId())
                        .header(AcceptanceTestHeaders.MEMBER_ID, member.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(newsletter.getName()))
                .andExpect(jsonPath("$.description").value(newsletter.getDescription()))
                .andExpect(jsonPath("$.isSubscribed").value(true));
    }

    @Test
    void 존재하지_않는_뉴스레터_상세는_404를_반환한다() throws Exception {
        mockMvc.perform(get("/api/v1/newsletters/{id}", 99999))
                .andExpect(status().isNotFound());
    }

    @Test
    void includeSuspended가_false이면_휴재와_폐간을_제외한다() throws Exception {
        Newsletter suspended = saveSuspended("휴재 뉴스레터", "suspended@newsletter.test", TODAY.minusMonths(2));
        Newsletter discontinued = saveNewsletter(
                "폐간 뉴스레터",
                "discontinued@newsletter.test",
                NewsletterPublicationStatus.DISCONTINUED
        );

        mockMvc.perform(get("/api/v1/newsletters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newsletters[*].newsletterId")
                        .value(org.hamcrest.Matchers.not(org.hamcrest.Matchers.hasItems(
                                suspended.getId().intValue(),
                                discontinued.getId().intValue()
                        ))));
    }

    @Test
    void includeSuspended가_true이면_최근_휴재만_포함한다() throws Exception {
        Newsletter recent = saveSuspended("최근 휴재", "recent@newsletter.test", TODAY.minusMonths(5));
        Newsletter old = saveSuspended("장기 휴재", "old@newsletter.test", TODAY.minusMonths(7));

        mockMvc.perform(get("/api/v1/newsletters").queryParam("includeSuspended", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newsletters[*].newsletterId")
                        .value(org.hamcrest.Matchers.hasItem(recent.getId().intValue())))
                .andExpect(jsonPath("$.newsletters[*].newsletterId")
                        .value(org.hamcrest.Matchers.not(org.hamcrest.Matchers.hasItem(old.getId().intValue()))));
    }

    @Test
    void categoryId로_뉴스레터를_필터링한다() throws Exception {
        Long categoryId = categories.get(2).getId();

        mockMvc.perform(get("/api/v1/newsletters").queryParam("categoryId", categoryId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newsletters.length()").value(2))
                .andExpect(jsonPath("$.newsletters[*].categoryId")
                        .value(org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.is(categoryId.intValue()))));
    }

    @Test
    void 구독자_수_집계가_없는_뉴스레터도_조회한다() throws Exception {
        Newsletter newsletter = saveNewsletter(
                "집계 없는 뉴스레터",
                "without-count@newsletter.test",
                NewsletterPublicationStatus.ACTIVE
        );

        mockMvc.perform(get("/api/v1/newsletters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newsletters[*].newsletterId")
                        .value(org.hamcrest.Matchers.hasItem(newsletter.getId().intValue())));
    }

    @Test
    void 유효하지_않은_인증_객체여도_공개_목록을_반환한다() throws Exception {
        TestingAuthenticationToken invalidAuthentication = new TestingAuthenticationToken("invalid", null);
        invalidAuthentication.setAuthenticated(true);

        mockMvc.perform(get("/api/v1/newsletters").with(authentication(invalidAuthentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newsletters.length()").value(4));
    }

    private Newsletter saveSuspended(String name, String email, LocalDate suspendedAt) {
        NewsletterDetail detail = newsletterDetailRepository.save(TestFixture.createNewsletterDetail(false));
        return newsletterRepository.save(TestFixture.createSuspendedNewsletter(
                name,
                email,
                categories.getFirst().getId(),
                detail.getId(),
                suspendedAt
        ));
    }

    private Newsletter saveNewsletter(String name, String email, NewsletterPublicationStatus status) {
        NewsletterDetail detail = newsletterDetailRepository.save(TestFixture.createNewsletterDetail(false));
        return newsletterRepository.save(Newsletter.builder()
                .name(name)
                .description("설명")
                .imageUrl("https://cdn.bombom.me/img.png")
                .email(email)
                .categoryId(categories.getFirst().getId())
                .detailId(detail.getId())
                .status(status)
                .build());
    }
}
