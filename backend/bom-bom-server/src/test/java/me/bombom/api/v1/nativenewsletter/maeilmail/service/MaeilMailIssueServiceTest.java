package me.bombom.api.v1.nativenewsletter.maeilmail.service;

import static org.mockito.BDDMockito.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.article.domain.Article;
import me.bombom.api.v1.article.domain.RecentArticle;
import me.bombom.api.v1.article.repository.ArticleRepository;
import me.bombom.api.v1.article.repository.RecentArticleRepository;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailContent;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailSentContent;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailSubscriptionTrack;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailTopic;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailTrack;
import me.bombom.api.v1.nativenewsletter.maeilmail.repository.MaeilMailContentRepository;
import me.bombom.api.v1.nativenewsletter.maeilmail.repository.MaeilMailSentContentRepository;
import me.bombom.api.v1.nativenewsletter.maeilmail.repository.MaeilMailSubscriptionTrackRepository;
import me.bombom.api.v1.nativenewsletter.maeilmail.repository.MaeilMailTopicRepository;
import me.bombom.api.v1.newsletter.domain.Category;
import me.bombom.api.v1.newsletter.domain.Newsletter;
import me.bombom.api.v1.newsletter.domain.NewsletterDetail;
import me.bombom.api.v1.newsletter.domain.NewsletterPublicationStatus;
import me.bombom.api.v1.newsletter.domain.NewsletterSource;
import me.bombom.api.v1.newsletter.repository.CategoryRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterDetailRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterRepository;
import me.bombom.api.v1.subscribe.domain.Subscribe;
import me.bombom.api.v1.subscribe.domain.SubscribeStatus;
import me.bombom.api.v1.subscribe.repository.SubscribeRepository;
import me.bombom.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.TestPropertySource;

@IntegrationTest
@TestPropertySource(properties = "maeil-mail.issue.chunk-size=1")
class MaeilMailIssueServiceTest {

    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");
    private static final LocalDate WEEKDAY = LocalDate.of(2026, 4, 24);

    @Autowired
    private MaeilMailIssueService maeilMailIssueService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private NewsletterRepository newsletterRepository;

    @Autowired
    private NewsletterDetailRepository newsletterDetailRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SubscribeRepository subscribeRepository;

    @Autowired
    private MaeilMailSubscriptionTrackRepository trackRepository;

    @Autowired
    private MaeilMailTopicRepository topicRepository;

    @Autowired
    private MaeilMailContentRepository contentRepository;

    @Autowired
    private MaeilMailSentContentRepository sentContentRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private RecentArticleRepository recentArticleRepository;

    @MockitoBean
    private Clock clock;

    @BeforeEach
    void setup() {
        recentArticleRepository.deleteAllInBatch();
        articleRepository.deleteAllInBatch();
        sentContentRepository.deleteAllInBatch();
        contentRepository.deleteAllInBatch();
        topicRepository.deleteAllInBatch();
        trackRepository.deleteAllInBatch();
        subscribeRepository.deleteAllInBatch();
        newsletterRepository.deleteAllInBatch();
        newsletterDetailRepository.deleteAllInBatch();
        categoryRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();

        setToday(WEEKDAY);
    }

    @Test
    void 활성_구독자에게_아티클이_발행된다() {
        Member member = memberRepository.save(TestFixture.normalMemberFixture());
        Newsletter newsletter = saveMaeilMailNewsletter();
        MaeilMailTopic topic = topicRepository.save(createTopic(MaeilMailTrack.BE, "JPA", 0));
        contentRepository.save(createContent(topic.getId(), "N+1 문제"));
        Subscribe subscribe = subscribeRepository.save(createSubscribe(newsletter, member, SubscribeStatus.SUBSCRIBED));
        trackRepository.save(createTrack(subscribe.getId(), member.getId(), MaeilMailTrack.BE));

        maeilMailIssueService.issue();

        List<Article> articles = articleRepository.findAll();
        List<RecentArticle> recentArticles = recentArticleRepository.findAll();

        assertSoftly(softly -> {
            softly.assertThat(articles).hasSize(1);
            softly.assertThat(articles.getFirst().getTitle()).isEqualTo("N+1 문제");
            softly.assertThat(articles.getFirst().getMemberId()).isEqualTo(member.getId());
            softly.assertThat(articles.getFirst().getNewsletterId()).isEqualTo(newsletter.getId());
            softly.assertThat(recentArticles).hasSize(1);
            softly.assertThat(recentArticles.getFirst().getArticleId()).isEqualTo(articles.getFirst().getId());
            softly.assertThat(trackRepository.findAll().getFirst().getLastIssuedDate()).isEqualTo(WEEKDAY);
        });
    }

    @Test
    void UNSUBSCRIBING_구독자는_발행_대상에서_제외된다() {
        Member member = memberRepository.save(TestFixture.normalMemberFixture());
        Newsletter newsletter = saveMaeilMailNewsletter();
        MaeilMailTopic topic = topicRepository.save(createTopic(MaeilMailTrack.BE, "JPA", 0));
        contentRepository.save(createContent(topic.getId(), "N+1 문제"));
        Subscribe subscribe = subscribeRepository.save(createSubscribe(newsletter, member, SubscribeStatus.UNSUBSCRIBING));
        trackRepository.save(createTrack(subscribe.getId(), member.getId(), MaeilMailTrack.BE));

        maeilMailIssueService.issue();

        assertThat(articleRepository.findAll()).isEmpty();
    }

    @Test
    void 매일메일이_아닌_구독_트랙은_발행_대상에서_제외된다() {
        Member member = memberRepository.save(TestFixture.normalMemberFixture());
        Newsletter newsletter = saveNewsletter("외부 뉴스레터", "external@bombom.news", NewsletterSource.EXTERNAL);
        MaeilMailTopic topic = topicRepository.save(createTopic(MaeilMailTrack.BE, "JPA", 0));
        contentRepository.save(createContent(topic.getId(), "N+1 문제"));
        Subscribe subscribe = subscribeRepository.save(createSubscribe(newsletter, member, SubscribeStatus.SUBSCRIBED));
        trackRepository.save(createTrack(subscribe.getId(), member.getId(), MaeilMailTrack.BE));

        maeilMailIssueService.issue();

        assertSoftly(softly -> {
            softly.assertThat(articleRepository.findAll()).isEmpty();
            softly.assertThat(trackRepository.findAll().getFirst().getLastIssuedDate()).isNull();
        });
    }

    @Test
    void 발행중이_아닌_매일메일_구독_트랙은_발행_대상에서_제외된다() {
        Member member = memberRepository.save(TestFixture.normalMemberFixture());
        Newsletter newsletter = saveNewsletter(
                "휴재 매일메일",
                "suspended-maeil@bombom.news",
                NewsletterSource.MAEIL_MAIL,
                NewsletterPublicationStatus.SUSPENDED
        );
        MaeilMailTopic topic = topicRepository.save(createTopic(MaeilMailTrack.BE, "JPA", 0));
        contentRepository.save(createContent(topic.getId(), "N+1 문제"));
        Subscribe subscribe = subscribeRepository.save(createSubscribe(newsletter, member, SubscribeStatus.SUBSCRIBED));
        trackRepository.save(createTrack(subscribe.getId(), member.getId(), MaeilMailTrack.BE));

        maeilMailIssueService.issue();

        assertSoftly(softly -> {
            softly.assertThat(articleRepository.findAll()).isEmpty();
            softly.assertThat(trackRepository.findAll().getFirst().getLastIssuedDate()).isNull();
        });
    }

    @Test
    void 단일_매일메일_뉴스레터_id로_아티클이_발행된다() {
        Member firstMember = memberRepository.save(TestFixture.createUniqueMember("member1", "provider1"));
        Member secondMember = memberRepository.save(TestFixture.createUniqueMember("member2", "provider2"));
        Newsletter newsletter = saveMaeilMailNewsletter();
        MaeilMailTopic topic = topicRepository.save(createTopic(MaeilMailTrack.BE, "JPA", 0));
        contentRepository.save(createContent(topic.getId(), "N+1 문제"));
        Subscribe firstSubscribe = subscribeRepository.save(
                createSubscribe(newsletter, firstMember, SubscribeStatus.SUBSCRIBED));
        Subscribe secondSubscribe = subscribeRepository.save(
                createSubscribe(newsletter, secondMember, SubscribeStatus.SUBSCRIBED));
        trackRepository.save(createTrack(firstSubscribe.getId(), firstMember.getId(), MaeilMailTrack.BE));
        trackRepository.save(createTrack(secondSubscribe.getId(), secondMember.getId(), MaeilMailTrack.BE));

        maeilMailIssueService.issue();

        Map<Long, Long> newsletterIdByMemberId = articleRepository.findAll().stream()
                .collect(Collectors.toMap(Article::getMemberId, Article::getNewsletterId));

        assertSoftly(softly -> {
            softly.assertThat(newsletterIdByMemberId).hasSize(2);
            softly.assertThat(newsletterIdByMemberId.get(firstMember.getId())).isEqualTo(newsletter.getId());
            softly.assertThat(newsletterIdByMemberId.get(secondMember.getId())).isEqualTo(newsletter.getId());
        });
    }

    @Test
    void 같은_멤버의_같은_토픽_중복_트랙은_한_번만_발행한다() {
        Member member = memberRepository.save(TestFixture.normalMemberFixture());
        Newsletter newsletter = saveMaeilMailNewsletter();
        MaeilMailTopic topic = topicRepository.save(createTopic(MaeilMailTrack.BE, "JPA", 0));
        contentRepository.save(createContent(topic.getId(), "N+1 문제"));
        Subscribe firstSubscribe = subscribeRepository.save(
                createSubscribe(newsletter, member, SubscribeStatus.SUBSCRIBED));
        Subscribe secondSubscribe = subscribeRepository.save(
                createSubscribe(newsletter, member, SubscribeStatus.SUBSCRIBED));
        trackRepository.save(createTrack(firstSubscribe.getId(), member.getId(), MaeilMailTrack.BE));
        trackRepository.save(createTrack(secondSubscribe.getId(), member.getId(), MaeilMailTrack.BE));

        maeilMailIssueService.issue();

        assertSoftly(softly -> {
            softly.assertThat(articleRepository.findAll()).hasSize(1);
            softly.assertThat(recentArticleRepository.findAll()).hasSize(1);
            softly.assertThat(sentContentRepository.findAll()).hasSize(1);
            softly.assertThat(trackRepository.findAll())
                    .allSatisfy(track -> {
                        softly.assertThat(track.getLastIssuedDate()).isEqualTo(WEEKDAY);
                        softly.assertThat(track.getCurriculumIndex()).isEqualTo(1);
                    });
        });
    }

    @Test
    void 토픽에_컨텐츠가_없으면_해당_구독자는_건너뛴다() {
        Member member = memberRepository.save(TestFixture.normalMemberFixture());
        Newsletter newsletter = saveMaeilMailNewsletter();
        topicRepository.save(createTopic(MaeilMailTrack.BE, "JPA", 0));
        Subscribe subscribe = subscribeRepository.save(createSubscribe(newsletter, member, SubscribeStatus.SUBSCRIBED));
        trackRepository.save(createTrack(subscribe.getId(), member.getId(), MaeilMailTrack.BE));

        maeilMailIssueService.issue();

        assertSoftly(softly -> {
            softly.assertThat(articleRepository.findAll()).isEmpty();
            softly.assertThat(trackRepository.findAll().getFirst().getCurriculumIndex()).isZero();
        });
    }

    @Test
    void curriculumIndex_나머지_연산으로_토픽이_순환된다() {
        Member member = memberRepository.save(TestFixture.normalMemberFixture());
        Newsletter newsletter = saveMaeilMailNewsletter();
        MaeilMailTopic firstTopic = topicRepository.save(createTopic(MaeilMailTrack.BE, "JPA", 0));
        MaeilMailTopic secondTopic = topicRepository.save(createTopic(MaeilMailTrack.BE, "Spring", 1));
        contentRepository.save(createContent(firstTopic.getId(), "JPA 컨텐츠"));
        MaeilMailContent springContent = contentRepository.save(createContent(secondTopic.getId(), "Spring 컨텐츠"));
        Subscribe subscribe = subscribeRepository.save(createSubscribe(newsletter, member, SubscribeStatus.SUBSCRIBED));
        MaeilMailSubscriptionTrack track = trackRepository.save(createTrack(subscribe.getId(), member.getId(), MaeilMailTrack.BE));
        trackRepository.incrementCurriculumIndexByIds(List.of(track.getId()));

        maeilMailIssueService.issue();

        List<Article> articles = articleRepository.findAll();

        assertSoftly(softly -> {
            softly.assertThat(articles).hasSize(1);
            softly.assertThat(articles.getFirst().getTitle()).isEqualTo(springContent.getTitle());
            softly.assertThat(trackRepository.findAll().getFirst().getCurriculumIndex()).isEqualTo(2);
        });
    }

    @Test
    void 모든_컨텐츠_소진_시_발송_기록이_초기화된다() {
        Member member = memberRepository.save(TestFixture.normalMemberFixture());
        Newsletter newsletter = saveMaeilMailNewsletter();
        MaeilMailTopic topic = topicRepository.save(createTopic(MaeilMailTrack.BE, "JPA", 0));
        MaeilMailContent content = contentRepository.save(createContent(topic.getId(), "유일한 컨텐츠"));
        Subscribe subscribe = subscribeRepository.save(createSubscribe(newsletter, member, SubscribeStatus.SUBSCRIBED));
        trackRepository.save(createTrack(subscribe.getId(), member.getId(), MaeilMailTrack.BE));
        sentContentRepository.save(MaeilMailSentContent.builder()
                .memberId(member.getId())
                .topicId(topic.getId())
                .contentId(content.getId())
                .build());

        maeilMailIssueService.issue();

        List<MaeilMailSentContent> sentContents = sentContentRepository.findAll();

        assertSoftly(softly -> {
            softly.assertThat(sentContents).hasSize(1);
            softly.assertThat(sentContents.getFirst().getContentId()).isEqualTo(content.getId());
            softly.assertThat(articleRepository.findAll()).hasSize(1);
        });
    }

    @Test
    void 이미_발송된_컨텐츠는_같은_사이클에서_다시_선택되지_않는다() {
        Member member = memberRepository.save(TestFixture.normalMemberFixture());
        Newsletter newsletter = saveMaeilMailNewsletter();
        MaeilMailTopic topic = topicRepository.save(createTopic(MaeilMailTrack.BE, "JPA", 0));
        MaeilMailContent alreadySent = contentRepository.save(createContent(topic.getId(), "이미 발송됨"));
        MaeilMailContent candidate = contentRepository.save(createContent(topic.getId(), "아직 안 보냄"));
        Subscribe subscribe = subscribeRepository.save(createSubscribe(newsletter, member, SubscribeStatus.SUBSCRIBED));
        trackRepository.save(createTrack(subscribe.getId(), member.getId(), MaeilMailTrack.BE));
        sentContentRepository.save(MaeilMailSentContent.builder()
                .memberId(member.getId())
                .topicId(topic.getId())
                .contentId(alreadySent.getId())
                .build());

        maeilMailIssueService.issue();

        List<Article> articles = articleRepository.findAll();

        assertThat(articles).hasSize(1);
        assertThat(articles.getFirst().getTitle()).isEqualTo(candidate.getTitle());
    }

    @Test
    void 이미_오늘_발행된_트랙은_다시_발행하지_않는다() {
        Member member = memberRepository.save(TestFixture.normalMemberFixture());
        Newsletter newsletter = saveMaeilMailNewsletter();
        MaeilMailTopic topic = topicRepository.save(createTopic(MaeilMailTrack.BE, "JPA", 0));
        contentRepository.save(createContent(topic.getId(), "N+1 문제"));
        Subscribe subscribe = subscribeRepository.save(createSubscribe(newsletter, member, SubscribeStatus.SUBSCRIBED));
        trackRepository.save(createTrack(subscribe.getId(), member.getId(), MaeilMailTrack.BE));

        maeilMailIssueService.issue();
        maeilMailIssueService.issue();

        assertSoftly(softly -> {
            softly.assertThat(articleRepository.findAll()).hasSize(1);
            softly.assertThat(recentArticleRepository.findAll()).hasSize(1);
            softly.assertThat(trackRepository.findAll().getFirst().getCurriculumIndex()).isEqualTo(1);
        });
    }

    @Test
    void 주말에는_발행하지_않는다() {
        setToday(LocalDate.of(2026, 4, 25));
        Member member = memberRepository.save(TestFixture.normalMemberFixture());
        Newsletter newsletter = saveMaeilMailNewsletter();
        MaeilMailTopic topic = topicRepository.save(createTopic(MaeilMailTrack.BE, "JPA", 0));
        contentRepository.save(createContent(topic.getId(), "N+1 문제"));
        Subscribe subscribe = subscribeRepository.save(createSubscribe(newsletter, member, SubscribeStatus.SUBSCRIBED));
        trackRepository.save(createTrack(subscribe.getId(), member.getId(), MaeilMailTrack.BE));

        maeilMailIssueService.issue();

        assertSoftly(softly -> {
            softly.assertThat(articleRepository.findAll()).isEmpty();
            softly.assertThat(trackRepository.findAll().getFirst().getLastIssuedDate()).isNull();
        });
    }

    private Newsletter saveMaeilMailNewsletter() {
        return saveNewsletter("매일메일", "maeil@bombom.news", NewsletterSource.MAEIL_MAIL);
    }

    private Newsletter saveNewsletter(
            String name,
            String email,
            NewsletterSource source
    ) {
        return saveNewsletter(name, email, source, NewsletterPublicationStatus.ACTIVE);
    }

    private Newsletter saveNewsletter(
            String name,
            String email,
            NewsletterSource source,
            NewsletterPublicationStatus status
    ) {
        Category category = categoryRepository.save(TestFixture.createCategory());
        NewsletterDetail detail = newsletterDetailRepository.save(TestFixture.createNewsletterDetail(true));
        return newsletterRepository.save(Newsletter.builder()
                .name(name)
                .description("설명")
                .imageUrl("https://cdn.bombom.me/img.png")
                .email(email)
                .categoryId(category.getId())
                .detailId(detail.getId())
                .source(source)
                .status(status)
                .build());
    }

    private Subscribe createSubscribe(
            Newsletter newsletter,
            Member member,
            SubscribeStatus status
    ) {
        return Subscribe.builder()
                .newsletterId(newsletter.getId())
                .memberId(member.getId())
                .status(status)
                .build();
    }

    private MaeilMailSubscriptionTrack createTrack(
            Long subscribeId,
            Long memberId,
            MaeilMailTrack field
    ) {
        return MaeilMailSubscriptionTrack.builder()
                .subscribeId(subscribeId)
                .memberId(memberId)
                .field(field)
                .build();
    }

    private MaeilMailTopic createTopic(
            MaeilMailTrack track,
            String name,
            int displayOrder
    ) {
        return MaeilMailTopic.builder()
                .track(track)
                .name(name)
                .displayOrder(displayOrder)
                .build();
    }

    private MaeilMailContent createContent(
            Long topicId,
            String title
    ) {
        return MaeilMailContent.builder()
                .topicId(topicId)
                .title(title)
                .content("<p>" + title + "</p>")
                .contentsText(title)
                .contentsSummary(title)
                .expectedReadTime(1)
                .build();
    }

    private void setToday(LocalDate date) {
        Instant instant = date.atStartOfDay(SEOUL_ZONE).toInstant();
        given(clock.instant()).willReturn(instant);
        given(clock.getZone()).willReturn(SEOUL_ZONE);
    }
}
