package me.bombom.api.v1;

import me.bombom.api.v1.member.domain.ContinueReading;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.domain.TodayReading;
import me.bombom.api.v1.member.domain.WeeklyReading;
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

    public static ContinueReading continueReadingFixture(Member member){
        return ContinueReading.builder()
                .memberId(member.getId())
                .dayCount(10)
                .build();
    }

    public static TodayReading todayReadingFixture(Member member){
        return TodayReading.builder()
                .memberId(member.getId())
                .currentCount(1)
                .totalCount(3)
                .build();
    }

    public static WeeklyReading weeklyReadingFixture(Member member) {
        return WeeklyReading.builder()
                .memberId(member.getId())
                .currentCount(3)
                .goalCount(5)
                .build();
    }
}
