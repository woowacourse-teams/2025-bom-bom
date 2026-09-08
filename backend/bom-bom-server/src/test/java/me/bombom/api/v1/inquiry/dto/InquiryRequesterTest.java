package me.bombom.api.v1.inquiry.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class InquiryRequesterTest {

    @Test
    void memberId만_있으면_회원으로_판별된다() {
        InquiryRequester requester = new InquiryRequester(1L, null);

        assertThat(requester.isMember()).isTrue();
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
