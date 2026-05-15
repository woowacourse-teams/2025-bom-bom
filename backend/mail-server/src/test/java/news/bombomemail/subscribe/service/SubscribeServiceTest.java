package news.bombomemail.subscribe.service;

import news.bombomemail.subscribe.domain.Subscribe;
import news.bombomemail.subscribe.domain.SubscribeStatus;
import news.bombomemail.subscribe.repository.SubscribeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(SubscribeService.class)
class SubscribeServiceTest {

    @Autowired
    SubscribeRepository subscribeRepository;

    @Autowired
    SubscribeService subscribeService;

    @Test
    void 구독이_정상적으로_저장된다() {
        // given
        Long newsletterId = 1L;
        Long memberId = 2L;
        String unsubscribeUrl = "unsubscribeUrl";

        // when
        subscribeService.upsertSubscribe(newsletterId, memberId, unsubscribeUrl, "뉴스레터", "아티클");

        // then
        boolean exists = subscribeRepository.existsByNewsletterIdAndMemberId(newsletterId, memberId);
        assertThat(exists).isTrue();
    }

    @Test
    void 새로_저장된_구독은_SUBSCRIBED_상태를_기본값으로_가진다() {
        // given
        Long newsletterId = 1L;
        Long memberId = 2L;
        String unsubscribeUrl = "unsubscribeUrl";

        // when
        subscribeService.upsertSubscribe(newsletterId, memberId, unsubscribeUrl, "뉴스레터", "아티클");

        // then
        Subscribe subscribe = subscribeRepository.findByMemberIdAndNewsletterId(memberId, newsletterId)
                .orElseThrow();
        assertThat(subscribe.getStatus()).isEqualTo(SubscribeStatus.SUBSCRIBED);
    }

    @Test
    void 이미_구독된_경우_중복저장되지_않는다() {
        // given
        Long newsletterId = 1L;
        Long memberId = 2L;
        String unsubscribeUrl = "unsubscribeUrl";

        // when
        subscribeService.upsertSubscribe(newsletterId, memberId, unsubscribeUrl, "뉴스레터", "아티클");
        subscribeService.upsertSubscribe(newsletterId, memberId, unsubscribeUrl, "뉴스레터", "아티클");

        // then
        long count = subscribeRepository.findAll().stream()
                .filter(s -> s.getNewsletterId().equals(newsletterId) && s.getMemberId().equals(memberId))
                .count();
        assertThat(count).isEqualTo(1);
    }

    @Test
    void 이미_구독된_경우_구독_취소_URL을_업데이트한다() {
        // given
        Long newsletterId = 1L;
        Long memberId = 2L;
        String oldUrl = "oldUnsubscribeUrl";
        String newUrl = "newUnsubscribeUrl";
        subscribeService.upsertSubscribe(newsletterId, memberId, oldUrl, "뉴스레터", "아티클");

        // when
        subscribeService.upsertSubscribe(newsletterId, memberId, newUrl, "뉴스레터", "아티클");

        // then
        subscribeRepository.findByMemberIdAndNewsletterId(newsletterId, memberId)
                .ifPresent(subscribe -> assertThat(subscribe.getUnsubscribeUrl()).isEqualTo(newUrl));
    }
} 
