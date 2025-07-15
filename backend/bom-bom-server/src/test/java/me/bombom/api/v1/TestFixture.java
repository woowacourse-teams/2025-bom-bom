package me.bombom.api.v1;

import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.enums.Gender;

public final class TestFixture {

    private TestFixture(){}

    public static Member normalMemberFixture(){
        return Member.builder()
                .email("email")
                .nickname("nickname")
                .gender(Gender.FEMALE)
                .roleId(1L)
                .build();
    }
}
