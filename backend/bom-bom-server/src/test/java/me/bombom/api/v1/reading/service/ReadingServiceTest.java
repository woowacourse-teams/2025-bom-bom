package me.bombom.api.v1.reading.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.dto.request.UpdateWeeklyGoalCountRequest;
import me.bombom.api.v1.member.dto.response.ReadingInformationResponse;
import me.bombom.api.v1.member.dto.response.WeeklyGoalCountResponse;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.reading.domain.WeeklyReading;
import me.bombom.api.v1.reading.repository.ContinueReadingRepository;
import me.bombom.api.v1.reading.repository.TodayReadingRepository;
import me.bombom.api.v1.reading.repository.WeeklyReadingRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(ReadingService.class)
class ReadingServiceTest {

    @Autowired
    private ReadingService readingService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private WeeklyReadingRepository weeklyReadingRepository;

    @Autowired
    private ContinueReadingRepository continueReadingRepository;

    @Autowired
    private TodayReadingRepository todayReadingRepository;

    @Test
    void 주간_목표를_수정할_수_있다() {
        // given
        Member savedMember = memberRepository.save(TestFixture.normalMemberFixture());

        WeeklyReading weeklyReading = WeeklyReading.builder()
                .memberId(savedMember.getId())
                .goalCount(0)
                .currentCount(2)
                .build();
        weeklyReadingRepository.save(weeklyReading);

        UpdateWeeklyGoalCountRequest request = new UpdateWeeklyGoalCountRequest(savedMember.getId(), 3);

        // when
        WeeklyGoalCountResponse result = readingService.updateWeeklyGoalCount(request);

        // then
        assertThat(result.weeklyGoalCount()).isEqualTo(3);
    }

    @Test
    void 주간_목표_수정에서_회원_정보가_존재하지_않을_경우_예외가_발생한다() {
        // given
        WeeklyReading weeklyReading = WeeklyReading.builder()
                .memberId(0L)
                .goalCount(0)
                .currentCount(2)
                .build();
        weeklyReadingRepository.save(weeklyReading);

        UpdateWeeklyGoalCountRequest request = new UpdateWeeklyGoalCountRequest(0L, 3);

        // when & then
        assertThatThrownBy(() -> readingService.updateWeeklyGoalCount(request))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.ENTITY_NOT_FOUND);
    }

    @Test
    void 주간_목표_수정에서_주간_목표_정보가_존재하지_않을_경우_예외가_발생한다() {
        // given
        Member savedMember = memberRepository.save(TestFixture.normalMemberFixture());
        UpdateWeeklyGoalCountRequest request = new UpdateWeeklyGoalCountRequest(savedMember.getId(), 3);

        // when & then
        assertThatThrownBy(() -> readingService.updateWeeklyGoalCount(request))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.ENTITY_NOT_FOUND);
    }

    @Test
    void 읽기_현황_종합_정보를_조회할_수_있다() {
        // given
        Member savedMember = memberRepository.save(TestFixture.normalMemberFixture());
        continueReadingRepository.save(TestFixture.continueReadingFixture(savedMember));
        todayReadingRepository.save(TestFixture.todayReadingFixture(savedMember));
        weeklyReadingRepository.save(TestFixture.weeklyReadingFixture(savedMember));

        // when
        ReadingInformationResponse response = readingService.getReadingInformation(savedMember.getId());

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.streakReadDay()).isEqualTo(10);
            softly.assertThat(response.today().readCount()).isEqualTo(1);
            softly.assertThat(response.today().totalCount()).isEqualTo(3);
            softly.assertThat(response.weekly().readCount()).isEqualTo(3);
            softly.assertThat(response.weekly().goalCount()).isEqualTo(5);
        });
    }

    @Test
    void 읽기_현황_종합_정보_조회에서_회원_정보가_존재하지_않을_경우_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> readingService.getReadingInformation(1L))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.ENTITY_NOT_FOUND);
    }

    @Test
    void 읽기_현황_종합_정보_조회에서_연속_읽기가_존재하지_않을_경우_예외가_발생한다() {
        // given
        Member savedMember = memberRepository.save(TestFixture.normalMemberFixture());

        // when & then
        assertThatThrownBy(() -> readingService.getReadingInformation(savedMember.getId()))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.ENTITY_NOT_FOUND);
    }

    @Test
    void 읽기_현황_종합_정보_조회에서_일간_읽기_정보가_존재하지_않을_경우_예외가_발생한다() {
        // given
        Member savedMember = memberRepository.save(TestFixture.normalMemberFixture());
        continueReadingRepository.save(TestFixture.continueReadingFixture(savedMember));

        // when & then
        assertThatThrownBy(() -> readingService.getReadingInformation(savedMember.getId()))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.ENTITY_NOT_FOUND);
    }

    @Test
    void 읽기_현황_종합_정보_조회에서_주간_목표_정보가_존재하지_않을_경우_예외가_발생한다() {
        // given
        Member savedMember = memberRepository.save(TestFixture.normalMemberFixture());
        continueReadingRepository.save(TestFixture.continueReadingFixture(savedMember));
        todayReadingRepository.save(TestFixture.todayReadingFixture(savedMember));

        // when & then
        assertThatThrownBy(() -> readingService.getReadingInformation(savedMember.getId()))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.ENTITY_NOT_FOUND);
    }
}
