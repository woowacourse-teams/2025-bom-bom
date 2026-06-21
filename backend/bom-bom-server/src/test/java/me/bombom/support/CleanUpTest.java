package me.bombom.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.common.holiday.domain.Holiday;
import me.bombom.api.v1.common.holiday.repository.HolidayRepository;
import me.bombom.api.v1.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
class CleanUpTest {

    @Autowired
    private CleanUp cleanUp;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private HolidayRepository holidayRepository;

    @Test
    void 정리_대상에_도메인_테이블이_포함된다() {
        // when
        List<String> tables = cleanUp.cleanableTableNames();

        // then
        assertThat(tables).contains("member", "challenge");
    }

    @Test
    void 정리_대상에서_seed와_인프라_테이블은_제외된다() {
        // when
        List<String> tables = cleanUp.cleanableTableNames();

        // then
        // flyway_schema_history, SPRING_SESSION은 JPA 엔티티가 아니라 메타모델에 없으므로 자동 제외된다.
        assertThat(tables)
                .doesNotContain("flyway_schema_history")
                .doesNotContain("SPRING_SESSION")
                .doesNotContain("holiday")
                .doesNotContain("monthly_reading_snapshot_meta");
    }

    @Test
    void all_호출시_도메인_데이터는_지우고_seed는_보존한다() {
        // given
        memberRepository.save(TestFixture.uniqueMemberFixture());
        ensureHolidayExists();
        long holidayCountBefore = holidayRepository.count();

        // when
        cleanUp.all();

        // then
        assertThat(memberRepository.count()).isZero();
        assertThat(holidayRepository.count()).isEqualTo(holidayCountBefore);
    }

    /**
     * 컨테이너 재사용 환경에서는 holiday 행이 이미 존재할 수 있으므로
     * 중복 삽입을 피하기 위해 해당 날짜가 없을 때만 저장한다.
     */
    private void ensureHolidayExists() {
        LocalDate testDate = LocalDate.of(2099, 1, 1);
        if (!holidayRepository.existsByDate(testDate)) {
            holidayRepository.save(
                    Holiday.builder()
                            .date(testDate)
                            .name("테스트용_공휴일")
                            .build()
            );
        }
    }
}
