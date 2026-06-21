package me.bombom.support;

import static org.assertj.core.api.Assertions.assertThat;

import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.member.repository.MemberRepository;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CleanUpAutoCleaningTest {

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @Order(1)
    void 첫_테스트가_데이터를_남겨도() {
        memberRepository.save(TestFixture.uniqueMemberFixture());
        assertThat(memberRepository.count()).isEqualTo(1);
    }

    @Test
    @Order(2)
    void 다음_테스트는_빈_DB에서_시작한다() {
        // 리스너가 beforeTestMethod에서 cleanUp.all()을 호출했으므로 수동 정리 없이도 비어 있다
        assertThat(memberRepository.count()).isZero();
    }
}
