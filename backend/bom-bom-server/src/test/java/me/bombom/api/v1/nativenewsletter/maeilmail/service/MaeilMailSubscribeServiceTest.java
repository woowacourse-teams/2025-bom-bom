package me.bombom.api.v1.nativenewsletter.maeilmail.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.List;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailSubscription;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailSubscriptionTrack;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailTrack;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.WeeklyIssueCount;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.MaeilMailSubscribeRequest;
import me.bombom.api.v1.nativenewsletter.maeilmail.repository.MaeilMailSubscriptionRepository;
import me.bombom.api.v1.nativenewsletter.maeilmail.repository.MaeilMailSubscriptionTrackRepository;
import me.bombom.api.v1.newsletter.domain.Category;
import me.bombom.api.v1.newsletter.domain.Newsletter;
import me.bombom.api.v1.newsletter.domain.NewsletterDetail;
import me.bombom.api.v1.newsletter.domain.NewsletterSource;
import me.bombom.api.v1.newsletter.repository.CategoryRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterDetailRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterRepository;
import me.bombom.api.v1.subscribe.domain.Subscribe;
import me.bombom.api.v1.subscribe.repository.SubscribeRepository;
import me.bombom.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
class MaeilMailSubscribeServiceTest {

    @Autowired
    private MaeilMailSubscribeService maeilMailSubscribeService;

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
    private MaeilMailSubscriptionRepository maeilMailSubscriptionRepository;

    @Autowired
    private MaeilMailSubscriptionTrackRepository maeilMailSubscriptionTrackRepository;

    @BeforeEach
    void setup() {
        maeilMailSubscriptionTrackRepository.deleteAllInBatch();
        maeilMailSubscriptionRepository.deleteAllInBatch();
        subscribeRepository.deleteAllInBatch();
        newsletterRepository.deleteAllInBatch();
        newsletterDetailRepository.deleteAllInBatch();
        categoryRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @Test
    void 매일메일_구독에_성공한다() {
        Member member = memberRepository.save(TestFixture.normalMemberFixture());
        Newsletter newsletter = newsletterRepository.save(createMaeilMailNewsletter());

        MaeilMailSubscribeRequest request = new MaeilMailSubscribeRequest(
                newsletter.getId(),
                List.of(MaeilMailTrack.BE, MaeilMailTrack.FE),
                WeeklyIssueCount.FIVE.getValue()
        );

        maeilMailSubscribeService.subscribe(member.getId(), request);

        List<Subscribe> subscribes = subscribeRepository.findAll();
        List<MaeilMailSubscription> subscriptions = maeilMailSubscriptionRepository.findAll();
        List<MaeilMailSubscriptionTrack> tracks = maeilMailSubscriptionTrackRepository.findAll();

        assertThat(subscribes).hasSize(1);
        assertThat(subscriptions).hasSize(1);
        assertThat(tracks).hasSize(2);

        Subscribe subscribe = subscribes.getFirst();
        MaeilMailSubscription subscription = subscriptions.getFirst();

        assertSoftly(softly -> {
            softly.assertThat(subscribe.getMemberId()).isEqualTo(member.getId());
            softly.assertThat(subscribe.getNewsletterId()).isEqualTo(newsletter.getId());
            softly.assertThat(subscription.getSubscribeId()).isEqualTo(subscribe.getId());
            softly.assertThat(subscription.getMemberId()).isEqualTo(member.getId());
            softly.assertThat(subscription.getWeeklyIssueCount()).isEqualTo(WeeklyIssueCount.FIVE);
            softly.assertThat(tracks)
                    .extracting(MaeilMailSubscriptionTrack::getMaeilMailSubscriptionId)
                    .containsOnly(subscription.getId());
            softly.assertThat(tracks)
                    .extracting(MaeilMailSubscriptionTrack::getField)
                    .containsExactlyInAnyOrder(MaeilMailTrack.BE, MaeilMailTrack.FE);
        });
    }

    @Test
    void 외부_뉴스레터는_매일메일_구독_API로_구독할_수_없다() {
        Member member = memberRepository.save(TestFixture.normalMemberFixture());
        Newsletter newsletter = newsletterRepository.save(createExternalNewsletter());

        MaeilMailSubscribeRequest request = new MaeilMailSubscribeRequest(
                newsletter.getId(),
                List.of(MaeilMailTrack.BE),
                WeeklyIssueCount.ONE.getValue()
        );

        assertThatThrownBy(() -> maeilMailSubscribeService.subscribe(member.getId(), request))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.INVALID_INPUT_VALUE);

        assertSoftly(softly -> {
            softly.assertThat(subscribeRepository.findAll()).isEmpty();
            softly.assertThat(maeilMailSubscriptionRepository.findAll()).isEmpty();
        });
    }

    @Test
    void 이미_구독중이면_예외가_발생한다() {
        Member member = memberRepository.save(TestFixture.normalMemberFixture());
        Newsletter newsletter = newsletterRepository.save(createMaeilMailNewsletter());
        subscribeRepository.save(Subscribe.builder()
                .memberId(member.getId())
                .newsletterId(newsletter.getId())
                .build());

        MaeilMailSubscribeRequest request = new MaeilMailSubscribeRequest(
                newsletter.getId(),
                List.of(MaeilMailTrack.BE),
                WeeklyIssueCount.ONE.getValue()
        );

        assertThatThrownBy(() -> maeilMailSubscribeService.subscribe(member.getId(), request))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.DUPLICATED_DATA);

        assertSoftly(softly -> {
            softly.assertThat(subscribeRepository.findAll()).hasSize(1);
            softly.assertThat(maeilMailSubscriptionRepository.findAll()).isEmpty();
        });
    }

    @Test
    void 중복된_트랙이_들어오면_예외가_발생한다() {
        Member member = memberRepository.save(TestFixture.normalMemberFixture());
        Newsletter newsletter = newsletterRepository.save(createMaeilMailNewsletter());

        MaeilMailSubscribeRequest request = new MaeilMailSubscribeRequest(
                newsletter.getId(),
                List.of(MaeilMailTrack.BE, MaeilMailTrack.BE),
                WeeklyIssueCount.FIVE.getValue()
        );

        assertThatThrownBy(() -> maeilMailSubscribeService.subscribe(member.getId(), request))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.DUPLICATED_DATA);

        assertSoftly(softly -> {
            softly.assertThat(subscribeRepository.findAll()).isEmpty();
            softly.assertThat(maeilMailSubscriptionRepository.findAll()).isEmpty();
            softly.assertThat(maeilMailSubscriptionTrackRepository.findAll()).isEmpty();
        });
    }

    @Test
    void 유효하지_않은_주간_발행_횟수가_들어오면_예외가_발생한다() {
        Member member = memberRepository.save(TestFixture.normalMemberFixture());
        Newsletter newsletter = newsletterRepository.save(createMaeilMailNewsletter());

        MaeilMailSubscribeRequest request = new MaeilMailSubscribeRequest(
                newsletter.getId(),
                List.of(MaeilMailTrack.BE),
                0
        );

        assertThatThrownBy(() -> maeilMailSubscribeService.subscribe(member.getId(), request))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.INVALID_INPUT_VALUE);

        assertSoftly(softly -> {
            softly.assertThat(subscribeRepository.findAll()).isEmpty();
            softly.assertThat(maeilMailSubscriptionRepository.findAll()).isEmpty();
        });
    }

    private Newsletter createMaeilMailNewsletter() {
        Category category = categoryRepository.save(TestFixture.createCategory());
        NewsletterDetail detail = newsletterDetailRepository.save(TestFixture.createNewsletterDetail(true));

        return TestFixture.createNewsletter(
                "매일메일",
                "maeil@bombom.news",
                category.getId(),
                detail.getId(),
                NewsletterSource.MAEIL_MAIL
        );
    }

    private Newsletter createExternalNewsletter() {
        Category category = categoryRepository.save(TestFixture.createCategory());
        NewsletterDetail detail = newsletterDetailRepository.save(TestFixture.createNewsletterDetail(true));

        return TestFixture.createNewsletter(
                "외부레터",
                "external@bombom.news",
                category.getId(),
                detail.getId(),
                NewsletterSource.EXTERNAL
        );
    }
}
