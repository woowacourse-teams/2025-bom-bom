package news.bombomemail.subscribe.service;

import news.bombomemail.subscribe.domain.Subscribe;
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

        // when
        subscribeService.save(newsletterId, memberId);

        // then
        boolean exists = subscribeRepository.existsByNewsletterIdAndMemberId(newsletterId, memberId);
        assertThat(exists).isTrue();
    }

    @Test
    void 이미_구독된_경우_중복저장되지_않는다() {
        // given
        Long newsletterId = 1L;
        Long memberId = 2L;
        subscribeRepository.save(Subscribe.builder()
                .newsletterId(newsletterId)
                .memberId(memberId)
                .build());

        // when
        subscribeService.save(newsletterId, memberId);

        // then
        long count = subscribeRepository.findAll().stream()
                .filter(s -> s.getNewsletterId().equals(newsletterId) && s.getMemberId().equals(memberId))
                .count();
        assertThat(count).isEqualTo(1);
    }
} 
