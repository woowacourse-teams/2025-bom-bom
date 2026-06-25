package news.bombomemail.nativenewsletter.maeilmail.repository;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import news.bombomemail.nativenewsletter.maeilmail.domain.MaeilMailSubscriptionTrack;
import news.bombomemail.nativenewsletter.maeilmail.domain.MaeilMailTrack;
import news.bombomemail.newsletter.domain.Newsletter;
import news.bombomemail.newsletter.domain.NewsletterPublicationStatus;
import news.bombomemail.newsletter.domain.NewsletterSource;
import news.bombomemail.newsletter.repository.NewsletterRepository;
import news.bombomemail.subscribe.domain.Subscribe;
import news.bombomemail.subscribe.domain.SubscribeStatus;
import news.bombomemail.subscribe.repository.SubscribeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

@DataJpaTest
class MaeilMailSubscriptionTrackRepositoryTest {

    @Autowired
    private MaeilMailSubscriptionTrackRepository trackRepository;

    @Autowired
    private NewsletterRepository newsletterRepository;

    @Autowired
    private SubscribeRepository subscribeRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void 발행대상은_subscribed_maeil_mail_active이고_오늘_미발행_track만_id순으로_조회한다() {
        // given
        LocalDate issueDate = LocalDate.of(2026, 4, 27);
        Newsletter maeilMail = saveNewsletter("maeil", NewsletterSource.MAEIL_MAIL,
                NewsletterPublicationStatus.ACTIVE);
        Newsletter externalNewsletter = saveNewsletter("external", NewsletterSource.EXTERNAL,
                NewsletterPublicationStatus.ACTIVE);
        Newsletter suspendedMaeilMail = saveNewsletter("suspended", NewsletterSource.MAEIL_MAIL,
                NewsletterPublicationStatus.SUSPENDED);

        MaeilMailSubscriptionTrack firstTarget = saveTrack(maeilMail, 1L, SubscribeStatus.SUBSCRIBED, null, 0);
        MaeilMailSubscriptionTrack secondTarget = saveTrack(maeilMail, 2L, SubscribeStatus.SUBSCRIBED,
                issueDate.minusDays(1), 0);
        saveTrack(maeilMail, 3L, SubscribeStatus.SUBSCRIBED, issueDate, 0);
        saveTrack(maeilMail, 4L, SubscribeStatus.UNSUBSCRIBING, null, 0);
        saveTrack(externalNewsletter, 5L, SubscribeStatus.SUBSCRIBED, null, 0);
        saveTrack(suspendedMaeilMail, 6L, SubscribeStatus.SUBSCRIBED, null, 0);
        flushAndClear();

        // when
        List<MaeilMailSubscriptionTrack> tracks = trackRepository.findIssueTargetsAfterId(
                issueDate,
                SubscribeStatus.SUBSCRIBED,
                NewsletterSource.MAEIL_MAIL,
                NewsletterPublicationStatus.ACTIVE,
                0L,
                PageRequest.of(0, 10)
        );
        List<MaeilMailSubscriptionTrack> nextPage = trackRepository.findIssueTargetsAfterId(
                issueDate,
                SubscribeStatus.SUBSCRIBED,
                NewsletterSource.MAEIL_MAIL,
                NewsletterPublicationStatus.ACTIVE,
                firstTarget.getId(),
                PageRequest.of(0, 1)
        );

        // then
        assertSoftly(softly -> {
            softly.assertThat(tracks)
                    .extracting(MaeilMailSubscriptionTrack::getId)
                    .containsExactly(firstTarget.getId(), secondTarget.getId());
            softly.assertThat(nextPage)
                    .extracting(MaeilMailSubscriptionTrack::getId)
                    .containsExactly(secondTarget.getId());
        });
    }

    @Test
    void markIssuedByIds는_curriculumIndex와_lastIssuedDate를_갱신한다() {
        // given
        LocalDate issueDate = LocalDate.of(2026, 4, 27);
        Newsletter newsletter = saveNewsletter("maeil", NewsletterSource.MAEIL_MAIL,
                NewsletterPublicationStatus.ACTIVE);
        MaeilMailSubscriptionTrack firstTrack = saveTrack(newsletter, 1L, SubscribeStatus.SUBSCRIBED, null, 2);
        MaeilMailSubscriptionTrack secondTrack = saveTrack(newsletter, 2L, SubscribeStatus.SUBSCRIBED, null, 0);
        flushAndClear();

        // when
        trackRepository.markIssuedByIds(List.of(firstTrack.getId(), secondTrack.getId()), issueDate);
        flushAndClear();

        // then
        MaeilMailSubscriptionTrack updatedFirstTrack = trackRepository.findById(firstTrack.getId()).orElseThrow();
        MaeilMailSubscriptionTrack updatedSecondTrack = trackRepository.findById(secondTrack.getId()).orElseThrow();
        assertSoftly(softly -> {
            softly.assertThat(updatedFirstTrack.getCurriculumIndex()).isEqualTo(3);
            softly.assertThat(updatedFirstTrack.getLastIssuedDate()).isEqualTo(issueDate);
            softly.assertThat(updatedSecondTrack.getCurriculumIndex()).isEqualTo(1);
            softly.assertThat(updatedSecondTrack.getLastIssuedDate()).isEqualTo(issueDate);
        });
    }

    private Newsletter saveNewsletter(
            String name,
            NewsletterSource source,
            NewsletterPublicationStatus status
    ) {
        return newsletterRepository.save(Newsletter.builder()
                .name(name)
                .description(name + " description")
                .imageUrl("image")
                .email(name + "@example.com")
                .categoryId(1L)
                .detailId(1L)
                .source(source)
                .status(status)
                .build());
    }

    private MaeilMailSubscriptionTrack saveTrack(
            Newsletter newsletter,
            Long memberId,
            SubscribeStatus subscribeStatus,
            LocalDate lastIssuedDate,
            int curriculumIndex
    ) {
        Subscribe subscribe = subscribeRepository.save(Subscribe.builder()
                .newsletterId(newsletter.getId())
                .memberId(memberId)
                .status(subscribeStatus)
                .build());
        MaeilMailSubscriptionTrack track = MaeilMailSubscriptionTrack.builder()
                .subscribeId(subscribe.getId())
                .memberId(memberId)
                .field(MaeilMailTrack.BE)
                .build();
        ReflectionTestUtils.setField(track, "lastIssuedDate", lastIssuedDate);
        ReflectionTestUtils.setField(track, "curriculumIndex", curriculumIndex);
        return trackRepository.save(track);
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
