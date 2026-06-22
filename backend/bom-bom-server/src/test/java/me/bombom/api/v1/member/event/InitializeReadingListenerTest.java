package me.bombom.api.v1.member.event;

import static org.assertj.core.api.Assertions.assertThat;

import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.reading.repository.TodayReadingRepository;
import me.bombom.api.v1.reading.repository.MonthlyReadingSnapshotRepository;
import me.bombom.api.v1.reading.service.ReadingService;
import me.bombom.support.IntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@IntegrationTest
class InitializeReadingListenerTest {

    @Autowired
    ApplicationEventPublisher publisher;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    ReadingService readingService;

    @Autowired
    TodayReadingRepository todayReadingRepository;

    @Autowired
    MonthlyReadingSnapshotRepository monthlyReadingSnapshotRepository;

    private Long baselineMemberId;
    private Long memberId;

    @AfterEach
    void tearDown() {
        if (memberId != null) {
            readingService.deleteAllByMemberId(memberId);
        }
        if (baselineMemberId != null) {
            readingService.deleteAllByMemberId(baselineMemberId);
        }
    }

    @Test
    void 회원가입_이벤트_발행_시_읽기정보가_초기화된다() {
        // given
        memberRepository.deleteAllInBatch();
        Member baselineMember = memberRepository.save(TestFixture.createUniqueMember("baseline", "baseline-provider"));
        baselineMemberId = baselineMember.getId();
        monthlyReadingSnapshotRepository.save(TestFixture.monthlyReadingFixture(baselineMember));

        Member member = memberRepository.save(TestFixture.normalMemberFixture());
        memberId = member.getId();

        // when
        publisher.publishEvent(new MemberSignupEvent(member.getId()));
        TestTransaction.flagForCommit();
        TestTransaction.end();

        // then
        assertThat(todayReadingRepository.findByMemberId(member.getId())).isPresent();
    }
}
