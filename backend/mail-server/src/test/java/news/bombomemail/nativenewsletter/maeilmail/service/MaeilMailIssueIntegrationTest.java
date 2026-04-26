package news.bombomemail.nativenewsletter.maeilmail.service;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import news.bombomemail.article.domain.Article;
import news.bombomemail.article.domain.RecentArticle;
import news.bombomemail.article.repository.ArticleRepository;
import news.bombomemail.article.repository.RecentArticleRepository;
import news.bombomemail.nativenewsletter.maeilmail.domain.MaeilMailContent;
import news.bombomemail.nativenewsletter.maeilmail.domain.MaeilMailIssueHistory;
import news.bombomemail.nativenewsletter.maeilmail.domain.MaeilMailSentContent;
import news.bombomemail.nativenewsletter.maeilmail.domain.MaeilMailSubscriptionTrack;
import news.bombomemail.nativenewsletter.maeilmail.domain.MaeilMailTopic;
import news.bombomemail.nativenewsletter.maeilmail.domain.MaeilMailTrack;
import news.bombomemail.nativenewsletter.maeilmail.repository.MaeilMailContentRepository;
import news.bombomemail.nativenewsletter.maeilmail.repository.MaeilMailIssueHistoryRepository;
import news.bombomemail.nativenewsletter.maeilmail.repository.MaeilMailSentContentRepository;
import news.bombomemail.nativenewsletter.maeilmail.repository.MaeilMailSubscriptionTrackRepository;
import news.bombomemail.nativenewsletter.maeilmail.repository.MaeilMailTopicRepository;
import news.bombomemail.newsletter.domain.Newsletter;
import news.bombomemail.newsletter.domain.NewsletterPublicationStatus;
import news.bombomemail.newsletter.domain.NewsletterSource;
import news.bombomemail.newsletter.repository.NewsletterRepository;
import news.bombomemail.notification.domain.ArticleArrivalNotification;
import news.bombomemail.notification.repository.ArticleArrivalNotificationRepository;
import news.bombomemail.reading.domain.TodayReading;
import news.bombomemail.reading.repository.TodayReadingRepository;
import news.bombomemail.subscribe.domain.Subscribe;
import news.bombomemail.subscribe.repository.SubscribeRepository;
import news.bombomemail.subscribe.service.SubscribeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class MaeilMailIssueIntegrationTest {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final LocalDate ISSUE_DATE = LocalDate.of(2026, 4, 27);

    @Autowired
    private MaeilMailIssueService issueService;

    @Autowired
    private NewsletterRepository newsletterRepository;

    @Autowired
    private SubscribeRepository subscribeRepository;

    @Autowired
    private MaeilMailTopicRepository topicRepository;

    @Autowired
    private MaeilMailContentRepository contentRepository;

    @Autowired
    private MaeilMailSubscriptionTrackRepository trackRepository;

    @Autowired
    private MaeilMailSentContentRepository sentContentRepository;

    @Autowired
    private MaeilMailIssueHistoryRepository issueHistoryRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private RecentArticleRepository recentArticleRepository;

    @Autowired
    private ArticleArrivalNotificationRepository notificationRepository;

    @Autowired
    private TodayReadingRepository todayReadingRepository;

    @MockitoBean
    private SubscribeService subscribeService;

    @BeforeEach
    void setup() {
        issueHistoryRepository.deleteAll();
        sentContentRepository.deleteAll();
        trackRepository.deleteAll();
        contentRepository.deleteAll();
        topicRepository.deleteAll();
        todayReadingRepository.deleteAll();
        notificationRepository.deleteAll();
        recentArticleRepository.deleteAll();
        articleRepository.deleteAll();
        subscribeRepository.deleteAll();
        newsletterRepository.deleteAll();
    }

    @Test
    void 매일메일_발행결과를_article로_저장하고_기존_article_이벤트_흐름을_탄다() {
        // given
        Long memberId = 100L;
        Newsletter newsletter = newsletterRepository.save(Newsletter.builder()
                .name("매일메일")
                .description("매일메일 설명")
                .imageUrl("image")
                .email("maeil@example.com")
                .categoryId(1L)
                .detailId(1L)
                .source(NewsletterSource.MAEIL_MAIL)
                .status(NewsletterPublicationStatus.ACTIVE)
                .build());
        Subscribe firstSubscribe = subscribeRepository.save(createSubscribe(newsletter.getId(), memberId));
        Subscribe secondSubscribe = subscribeRepository.save(createSubscribe(newsletter.getId(), memberId));
        MaeilMailTopic topic = topicRepository.save(MaeilMailTopic.builder()
                .track(MaeilMailTrack.BE)
                .name("Spring")
                .displayOrder(1)
                .build());
        contentRepository.save(MaeilMailContent.builder()
                .topicId(topic.getId())
                .title("매일메일 제목")
                .content("<p>매일메일 본문</p>")
                .contentsText("매일메일 본문")
                .contentsSummary("매일메일 요약")
                .expectedReadTime(3)
                .build());
        trackRepository.saveAll(List.of(
                createTrack(firstSubscribe.getId(), memberId, MaeilMailTrack.BE),
                createTrack(secondSubscribe.getId(), memberId, MaeilMailTrack.BE)
        ));
        todayReadingRepository.save(TodayReading.builder()
                .memberId(memberId)
                .totalCount(0)
                .currentCount(0)
                .build());

        // when
        issueService.issue();

        // then
        List<Article> articles = articleRepository.findAll();
        List<RecentArticle> recentArticles = recentArticleRepository.findAll();
        List<ArticleArrivalNotification> notifications = notificationRepository.findAll();
        List<MaeilMailSentContent> sentContents = sentContentRepository.findAll();
        List<MaeilMailIssueHistory> issueHistories = issueHistoryRepository.findAll();
        List<MaeilMailSubscriptionTrack> tracks = trackRepository.findAll();
        TodayReading todayReading = todayReadingRepository.findByMemberId(memberId).orElseThrow();

        assertSoftly(softly -> {
            softly.assertThat(articles).hasSize(1);
            Article article = articles.getFirst();
            softly.assertThat(article.getTitle()).isEqualTo("매일메일 제목");
            softly.assertThat(article.getContents()).isEqualTo("<p>매일메일 본문</p>");
            softly.assertThat(article.getMemberId()).isEqualTo(memberId);
            softly.assertThat(article.getNewsletterId()).isEqualTo(newsletter.getId());

            softly.assertThat(recentArticles).hasSize(1);
            softly.assertThat(recentArticles.getFirst().getArticleId()).isEqualTo(article.getId());
            softly.assertThat(recentArticles.getFirst().getTitle()).isEqualTo("매일메일 제목");

            softly.assertThat(notifications).hasSize(1);
            softly.assertThat(notifications.getFirst().getArticleId()).isEqualTo(article.getId());
            softly.assertThat(notifications.getFirst().getNewsletterName()).isEqualTo("매일메일");

            softly.assertThat(todayReading.getTotalCount()).isEqualTo(1);
            softly.assertThat(sentContents).hasSize(1);
            softly.assertThat(sentContents.getFirst().getMemberId()).isEqualTo(memberId);
            softly.assertThat(sentContents.getFirst().getTopicId()).isEqualTo(topic.getId());

            softly.assertThat(issueHistories).hasSize(1);
            softly.assertThat(issueHistories.getFirst().getIssueDate()).isEqualTo(ISSUE_DATE);
            softly.assertThat(issueHistories.getFirst().getMemberId()).isEqualTo(memberId);
            softly.assertThat(issueHistories.getFirst().getTopicId()).isEqualTo(topic.getId());

            softly.assertThat(tracks).hasSize(2);
            softly.assertThat(tracks).allSatisfy(track -> {
                softly.assertThat(track.getCurriculumIndex()).isEqualTo(1);
                softly.assertThat(track.getLastIssuedDate()).isEqualTo(ISSUE_DATE);
            });
        });
        verifyNoInteractions(subscribeService);
    }

    @Test
    void 같은_날_재실행해도_article과_발행이력을_중복_생성하지_않는다() {
        // given
        Long memberId = 100L;
        Newsletter newsletter = newsletterRepository.save(Newsletter.builder()
                .name("매일메일")
                .description("매일메일 설명")
                .imageUrl("image")
                .email("maeil@example.com")
                .categoryId(1L)
                .detailId(1L)
                .source(NewsletterSource.MAEIL_MAIL)
                .status(NewsletterPublicationStatus.ACTIVE)
                .build());
        Subscribe subscribe = subscribeRepository.save(createSubscribe(newsletter.getId(), memberId));
        MaeilMailTopic topic = topicRepository.save(MaeilMailTopic.builder()
                .track(MaeilMailTrack.BE)
                .name("Spring")
                .displayOrder(1)
                .build());
        contentRepository.save(MaeilMailContent.builder()
                .topicId(topic.getId())
                .title("매일메일 제목")
                .content("<p>매일메일 본문</p>")
                .contentsText("매일메일 본문")
                .contentsSummary("매일메일 요약")
                .expectedReadTime(3)
                .build());
        trackRepository.save(createTrack(subscribe.getId(), memberId, MaeilMailTrack.BE));
        todayReadingRepository.save(TodayReading.builder()
                .memberId(memberId)
                .totalCount(0)
                .currentCount(0)
                .build());

        // when
        issueService.issue();
        issueService.issue();

        // then
        List<MaeilMailSubscriptionTrack> tracks = trackRepository.findAll();
        TodayReading todayReading = todayReadingRepository.findByMemberId(memberId).orElseThrow();
        assertSoftly(softly -> {
            softly.assertThat(articleRepository.findAll()).hasSize(1);
            softly.assertThat(recentArticleRepository.findAll()).hasSize(1);
            softly.assertThat(notificationRepository.findAll()).hasSize(1);
            softly.assertThat(sentContentRepository.findAll()).hasSize(1);
            softly.assertThat(issueHistoryRepository.findAll()).hasSize(1);
            softly.assertThat(todayReading.getTotalCount()).isEqualTo(1);
            softly.assertThat(tracks).hasSize(1);
            softly.assertThat(tracks.getFirst().getCurriculumIndex()).isEqualTo(1);
            softly.assertThat(tracks.getFirst().getLastIssuedDate()).isEqualTo(ISSUE_DATE);
        });
        verifyNoInteractions(subscribeService);
    }

    private Subscribe createSubscribe(Long newsletterId, Long memberId) {
        return Subscribe.builder()
                .newsletterId(newsletterId)
                .memberId(memberId)
                .build();
    }

    private MaeilMailSubscriptionTrack createTrack(Long subscribeId, Long memberId, MaeilMailTrack field) {
        return MaeilMailSubscriptionTrack.builder()
                .subscribeId(subscribeId)
                .memberId(memberId)
                .field(field)
                .build();
    }

    @TestConfiguration
    static class FixedClockConfig {

        @Bean
        @Primary
        Clock fixedClock() {
            LocalDateTime fixedDateTime = LocalDateTime.of(2026, 4, 27, 7, 0);
            return Clock.fixed(fixedDateTime.atZone(SEOUL).toInstant(), SEOUL);
        }
    }
}
