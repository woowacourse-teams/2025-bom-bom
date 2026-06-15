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
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.MaeilMailSubscriptionResponse;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.MaeilMailUpdateSubscriptionRequest;
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
        newsletterRepository.save(createMaeilMailNewsletter());

        maeilMailSubscribeService.putSubscription(member, new MaeilMailUpdateSubscriptionRequest(
                List.of(MaeilMailTrack.BE, MaeilMailTrack.FE)
        ));

        MaeilMailSubscriptionResponse response = maeilMailSubscribeService.getSubscription(member.getId());

        assertThat(response.tracks()).containsExactlyInAnyOrder(MaeilMailTrack.BE, MaeilMailTrack.FE);
    }

    @Test
    void 매일메일_미구독이면_빈_트랙_목록을_반환한다() {
        Member member = memberRepository.save(TestFixture.normalMemberFixture());

        MaeilMailSubscriptionResponse response = maeilMailSubscribeService.getSubscription(member.getId());

        assertThat(response.tracks()).isEmpty();
    }

    @Test
    void 미구독_상태에서_트랙을_보내면_신규_구독한다() {
        Member member = memberRepository.save(TestFixture.normalMemberFixture());
        Newsletter newsletter = newsletterRepository.save(createMaeilMailNewsletter());

        maeilMailSubscribeService.putSubscription(member, new MaeilMailUpdateSubscriptionRequest(
                List.of(MaeilMailTrack.BE, MaeilMailTrack.FE)
        ));

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
    void 구독_중인_트랙을_변경하면_요청한_트랙으로_치환된다() {
        Member member = memberRepository.save(TestFixture.normalMemberFixture());
        newsletterRepository.save(createMaeilMailNewsletter());
        maeilMailSubscribeService.putSubscription(member, new MaeilMailUpdateSubscriptionRequest(
                List.of(MaeilMailTrack.BE, MaeilMailTrack.FE)
        ));

        maeilMailSubscribeService.putSubscription(member, new MaeilMailUpdateSubscriptionRequest(
                List.of(MaeilMailTrack.BE)
        ));

        MaeilMailSubscriptionResponse response = maeilMailSubscribeService.getSubscription(member.getId());
        assertThat(response.tracks()).containsExactly(MaeilMailTrack.BE);
    }

    @Test
    void 구독_삭제를_요청하면_구독이_해지된다() {
        Member member = memberRepository.save(TestFixture.normalMemberFixture());
        newsletterRepository.save(createMaeilMailNewsletter());
        maeilMailSubscribeService.putSubscription(member, new MaeilMailUpdateSubscriptionRequest(
                List.of(MaeilMailTrack.BE, MaeilMailTrack.FE)
        ));

        maeilMailSubscribeService.deleteSubscription(member.getId());

        assertSoftly(softly -> {
            softly.assertThat(subscribeRepository.findAll()).isEmpty();
            softly.assertThat(maeilMailSubscriptionTrackRepository.findAll()).isEmpty();
        });
    }

    @Test
    void 미구독_상태에서_구독_삭제를_요청하면_아무_변화도_없다() {
        Member member = memberRepository.save(TestFixture.normalMemberFixture());
        newsletterRepository.save(createMaeilMailNewsletter());

        maeilMailSubscribeService.deleteSubscription(member.getId());

        assertSoftly(softly -> {
            softly.assertThat(subscribeRepository.findAll()).isEmpty();
            softly.assertThat(maeilMailSubscriptionTrackRepository.findAll()).isEmpty();
        });
    }

    @Test
    void 빈_트랙으로_구독_생성_수정을_요청하면_예외가_발생한다() {
        Member member = memberRepository.save(TestFixture.normalMemberFixture());
        newsletterRepository.save(createMaeilMailNewsletter());

        assertThatThrownBy(() -> maeilMailSubscribeService.putSubscription(
                member,
                new MaeilMailUpdateSubscriptionRequest(List.of())
        ))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.INVALID_REQUEST_BODY_VALIDATION);

        assertSoftly(softly -> {
            softly.assertThat(subscribeRepository.findAll()).isEmpty();
            softly.assertThat(maeilMailSubscriptionTrackRepository.findAll()).isEmpty();
        });
    }

    @Test
    void 중복된_트랙이_들어오면_예외가_발생한다() {
        Member member = memberRepository.save(TestFixture.normalMemberFixture());
        newsletterRepository.save(createMaeilMailNewsletter());

        assertThatThrownBy(() -> maeilMailSubscribeService.putSubscription(member, new MaeilMailUpdateSubscriptionRequest(
                List.of(MaeilMailTrack.BE, MaeilMailTrack.BE)
        )))
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
}
