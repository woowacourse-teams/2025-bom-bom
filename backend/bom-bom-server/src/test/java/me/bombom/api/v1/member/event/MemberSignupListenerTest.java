package me.bombom.api.v1.member.event;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.reading.service.ReadingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
class MemberSignupListenerTest {

    @Autowired
    ApplicationEventPublisher publisher;

    @Autowired
    MemberRepository memberRepository;

    @MockitoBean
    ReadingService readingService;

    @Test
    void 회원가입_이벤트_발행_시_읽기정보_초기화_메서드가_호출된다() {
        // given
        Member member = memberRepository.save(TestFixture.normalMemberFixture());

        // when
        publisher.publishEvent(new MemberSignupEvent(member.getId()));
        TestTransaction.flagForCommit();
        TestTransaction.end();

        // then
        verify(readingService, times(1)).initializeReadingInformation(member.getId());
    }
}
