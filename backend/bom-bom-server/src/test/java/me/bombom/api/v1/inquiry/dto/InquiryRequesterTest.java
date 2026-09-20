package me.bombom.api.v1.inquiry.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import me.bombom.api.v1.member.domain.Member;
import org.junit.jupiter.api.Test;

class InquiryRequesterTest {

    @Test
    void memberId만_있으면_회원으로_판별된다() {
        InquiryRequester requester = new InquiryRequester(1L, null);

        assertThat(requester.isMember()).isTrue();
    }

    @Test
    void Member로부터_memberId를_추출해_생성한다() {
        Member member = mock(Member.class);
        given(member.getId()).willReturn(1L);

        InquiryRequester requester = InquiryRequester.of(member, null);

        assertThat(requester.memberId()).isEqualTo(1L);
    }

    @Test
    void Member가_null이면_guestId로_생성한다() {
        InquiryRequester requester = InquiryRequester.of(null, "guest-uuid");

        assertThat(requester.memberId()).isNull();
        assertThat(requester.guestId()).isEqualTo("guest-uuid");
    }

    @Test
    void guestId만_있으면_비회원으로_판별된다() {
        InquiryRequester requester = new InquiryRequester(null, "guest-uuid");

        assertThat(requester.isMember()).isFalse();
    }

    @Test
    void memberId와_guestId가_둘_다_있으면_회원으로_판별된다() {
        InquiryRequester requester = new InquiryRequester(1L, "guest-uuid");

        assertThat(requester.isMember()).isTrue();
    }
}
