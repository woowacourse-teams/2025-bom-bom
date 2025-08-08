package me.bombom.api.v1.reading.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.UUID;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.common.config.QuerydslConfig;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.reading.domain.ContinueReading;
import me.bombom.api.v1.reading.domain.TodayReading;
import me.bombom.api.v1.reading.domain.WeeklyReading;
import me.bombom.api.v1.reading.repository.ContinueReadingRepository;
import me.bombom.api.v1.reading.repository.TodayReadingRepository;
import me.bombom.api.v1.reading.repository.WeeklyReadingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@Transactional
@ActiveProfiles("test")
@Import({ReadingService.class, QuerydslConfig.class})
class ReadingServiceTest {

    @Autowired
    private ReadingService readingService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ContinueReadingRepository continueReadingRepository;

    @Autowired
    private TodayReadingRepository todayReadingRepository;

    @Autowired
    private WeeklyReadingRepository weeklyReadingRepository;

    private Member member;
    private TodayReading todayReading;
    private ContinueReading continueReading;
    private WeeklyReading weeklyReading;

    @BeforeEach
    void setUp() {
        String nickname = "test_nickname_" + UUID.randomUUID();
        String providerId = "test_providerId_" + UUID.randomUUID();

        member = memberRepository.save(TestFixture.createUniqueMember(nickname, providerId));
        todayReading = todayReadingRepository.save(TestFixture.todayReadingFixtureZeroCurrentCount(member));
        continueReading = continueReadingRepository.save(TestFixture.continueReadingFixture(member));
        weeklyReading = weeklyReadingRepository.save(TestFixture.weeklyReadingFixture(member));

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();
    }

    @Test
    void 오늘_도착한_아티클을_읽으면_오늘_및_주간_읽기_횟수가_증가한다() {
        int initialTodayCount = todayReading.getCurrentCount();
        int initialWeeklyCount = weeklyReading.getCurrentCount();

        readingService.updateReadingCount(member.getId(), true);

        TodayReading updatedTodayReading = todayReadingRepository.findByMemberId(member.getId()).get();
        WeeklyReading updatedWeeklyReading = weeklyReadingRepository.findByMemberId(member.getId()).get();

        assertSoftly(softly -> {
            softly.assertThat(updatedTodayReading.getCurrentCount()).isEqualTo(initialTodayCount + 1);
            softly.assertThat(updatedWeeklyReading.getCurrentCount()).isEqualTo(initialWeeklyCount + 1);
        });
    }

    @Test
    void 오늘_도착한_아티클을_최초로_읽을_때_연속_읽기_횟수가_증가한다() {
        int initialContinueCount = continueReading.getDayCount();

        readingService.updateReadingCount(member.getId(), true);

        ContinueReading updatedContinueReading = continueReadingRepository.findByMemberId(member.getId()).get();

        assertThat(updatedContinueReading.getDayCount()).isEqualTo(initialContinueCount + 1);
    }

    @Test
    void 이미_연속_읽기_횟수가_증가하면_그날은_더이상_증가하지_않는다() {
        int initialContinueCount = continueReading.getDayCount();

        readingService.updateReadingCount(member.getId(), true);
        readingService.updateReadingCount(member.getId(), true);

        ContinueReading updatedContinueReading = continueReadingRepository.findByMemberId(member.getId()).get();

        assertThat(updatedContinueReading.getDayCount()).isEqualTo(initialContinueCount + 1);
    }

    @Test
    void 오늘_도착하지_않은_아티클을_읽으면_주간_읽기_횟수만_증가한다() {
        int initialTodayCount = todayReading.getCurrentCount();
        int initialContinueCount = continueReading.getDayCount();
        int initialWeeklyCount = weeklyReading.getCurrentCount();

        readingService.updateReadingCount(member.getId(), false);

        TodayReading updatedTodayReading = todayReadingRepository.findByMemberId(member.getId()).get();
        ContinueReading updatedContinueReading = continueReadingRepository.findByMemberId(member.getId()).get();
        WeeklyReading updatedWeeklyReading = weeklyReadingRepository.findByMemberId(member.getId()).get();

        assertSoftly(softly -> {
            softly.assertThat(updatedTodayReading.getCurrentCount()).isEqualTo(initialTodayCount);
            softly.assertThat(updatedContinueReading.getDayCount()).isEqualTo(initialContinueCount);
            softly.assertThat(updatedWeeklyReading.getCurrentCount()).isEqualTo(initialWeeklyCount + 1);
        });
    }
}
