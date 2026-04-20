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
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailSubscriptionTrack;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailTrack;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.MaeilMailSubscribeRequest;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.MaeilMailSubscriptionResponse;
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
    private MaeilMailSubscriptionTrackRepository maeilMailSubscriptionTrackRepository;

    @BeforeEach
    void setup() {
        maeilMailSubscriptionTrackRepository.deleteAllInBatch();
        subscribeRepository.deleteAllInBatch();
        newsletterRepository.deleteAllInBatch();
        newsletterDetailRepository.deleteAllInBatch();
        categoryRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @Test
    void 매일메일_구독_중이면_구독_정보를_반환한다() {
        Member member = memberRepository.save(TestFixture.normalMemberFixture());
        Newsletter newsletter = newsletterRepository.save(createMaeilMailNewsletter());

        maeilMailSubscribeService.subscribe(member.getId(), new MaeilMailSubscribeRequest(
                newsletter.getId(),
                List.of(MaeilMailTrack.BE, MaeilMailTrack.FE)
        ));

        MaeilMailSubscriptionResponse response = maeilMailSubscribeService.getSubscription(member.getId());

        assertSoftly(softly -> {
            softly.assertThat(response.subscribed()).isTrue();
            softly.assertThat(response.tracks()).containsExactlyInAnyOrder(
                    MaeilMailTrack.BE,
                    MaeilMailTrack.FE
            );
        });
    }

    @Test
    void 매일메일_미구독이면_미구독_상태를_반환한다() {
        Member member = memberRepository.save(TestFixture.normalMemberFixture());

        MaeilMailSubscriptionResponse response = maeilMailSubscribeService.getSubscription(member.getId());

        assertSoftly(softly -> {
            softly.assertThat(response.subscribed()).isFalse();
            softly.assertThat(response.tracks()).isEmpty();
        });
    }

    @Test
    void 매일메일_구독에_성공한다() {
        Member member = memberRepository.save(TestFixture.normalMemberFixture());
        Newsletter newsletter = newsletterRepository.save(createMaeilMailNewsletter());

        MaeilMailSubscribeRequest request = new MaeilMailSubscribeRequest(
                newsletter.getId(),
                List.of(MaeilMailTrack.BE, MaeilMailTrack.FE)
        );

        maeilMailSubscribeService.subscribe(member.getId(), request);

        List<Subscribe> subscribes = subscribeRepository.findAll();
        List<MaeilMailSubscriptionTrack> tracks = maeilMailSubscriptionTrackRepository.findAll();

        assertThat(subscribes).hasSize(1);
        assertThat(tracks).hasSize(2);

        Subscribe subscribe = subscribes.getFirst();

        assertSoftly(softly -> {
            softly.assertThat(subscribe.getMemberId()).isEqualTo(member.getId());
            softly.assertThat(subscribe.getNewsletterId()).isEqualTo(newsletter.getId());
            softly.assertThat(tracks)
                    .extracting(MaeilMailSubscriptionTrack::getSubscribeId)
                    .containsOnly(subscribe.getId());
            softly.assertThat(tracks)
                    .extracting(MaeilMailSubscriptionTrack::getMemberId)
                    .containsOnly(member.getId());
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
                List.of(MaeilMailTrack.BE)
        );

        assertThatThrownBy(() -> maeilMailSubscribeService.subscribe(member.getId(), request))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.INVALID_INPUT_VALUE);

        assertThat(subscribeRepository.findAll()).isEmpty();
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
                List.of(MaeilMailTrack.BE)
        );

        assertThatThrownBy(() -> maeilMailSubscribeService.subscribe(member.getId(), request))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.DUPLICATED_DATA);

        assertThat(subscribeRepository.findAll()).hasSize(1);
    }

    @Test
    void 중복된_트랙이_들어오면_예외가_발생한다() {
        Member member = memberRepository.save(TestFixture.normalMemberFixture());
        Newsletter newsletter = newsletterRepository.save(createMaeilMailNewsletter());

        MaeilMailSubscribeRequest request = new MaeilMailSubscribeRequest(
                newsletter.getId(),
                List.of(MaeilMailTrack.BE, MaeilMailTrack.BE)
        );

        assertThatThrownBy(() -> maeilMailSubscribeService.subscribe(member.getId(), request))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.DUPLICATED_DATA);

        assertSoftly(softly -> {
            softly.assertThat(subscribeRepository.findAll()).isEmpty();
            softly.assertThat(maeilMailSubscriptionTrackRepository.findAll()).isEmpty();
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
