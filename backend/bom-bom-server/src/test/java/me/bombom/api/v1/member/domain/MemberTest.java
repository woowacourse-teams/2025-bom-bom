package me.bombom.api.v1.member.domain;

import static org.assertj.core.api.Assertions.assertThat;

import me.bombom.api.v1.member.enums.Gender;
import org.junit.jupiter.api.Test;

class MemberTest {

    @Test
    void Apple_이전_식별자를_저장한다() {
        Member member = Member.builder()
                .provider("apple")
                .providerId("apple-sub")
                .email("apple@example.com")
                .nickname("apple-member")
                .gender(Gender.NONE)
                .roleId(1L)
                .build();

        member.updateAppleTransferSub("transfer-sub");

        assertThat(member.getAppleTransferSub()).isEqualTo("transfer-sub");
    }
}
