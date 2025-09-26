package me.bombom.api.v1.guidemail.service;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.pet.domain.Pet;
import me.bombom.api.v1.pet.domain.Stage;
import me.bombom.api.v1.pet.repository.PetRepository;
import me.bombom.api.v1.pet.repository.StageRepository;
import me.bombom.api.v1.reading.domain.ContinueReading;
import me.bombom.api.v1.reading.domain.MonthlyReadingRealtime;
import me.bombom.api.v1.reading.domain.MonthlyReadingSnapshot;
import me.bombom.api.v1.reading.domain.TodayReading;
import me.bombom.api.v1.reading.domain.WeeklyReading;
import me.bombom.api.v1.reading.repository.ContinueReadingRepository;
import me.bombom.api.v1.reading.repository.MonthlyReadingRealtimeRepository;
import me.bombom.api.v1.reading.repository.MonthlyReadingSnapshotRepository;
import me.bombom.api.v1.reading.repository.TodayReadingRepository;
import me.bombom.api.v1.reading.repository.WeeklyReadingRepository;
import me.bombom.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@IntegrationTest
class GuideMailServiceTest {

    @Autowired
    private GuideMailService guideMailService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private StageRepository stageRepository;

    @Autowired
    private TodayReadingRepository todayReadingRepository;

    @Autowired
    private WeeklyReadingRepository weeklyReadingRepository;

    @Autowired
    private ContinueReadingRepository continueReadingRepository;

    @Autowired
    private MonthlyReadingSnapshotRepository monthlyReadingSnapshotRepository;

    @Autowired
    private MonthlyReadingRealtimeRepository monthlyReadingRealtimeRepository;

    private Member member;
    private Pet pet;
    private TodayReading todayReading;
    private WeeklyReading weeklyReading;
    private ContinueReading continueReading;
    private MonthlyReadingSnapshot monthlyReadingSnapshot;

    @BeforeEach
    void setUp() {
        monthlyReadingRealtimeRepository.deleteAllInBatch();
        monthlyReadingSnapshotRepository.deleteAllInBatch();
        continueReadingRepository.deleteAllInBatch();
        weeklyReadingRepository.deleteAllInBatch();
        todayReadingRepository.deleteAllInBatch();
        petRepository.deleteAllInBatch();
        stageRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();

        member = memberRepository.save(TestFixture.createUniqueMember("nickname", "providerId"));
        Stage stage = stageRepository.save(TestFixture.createStage(1, 0));
        pet = petRepository.save(TestFixture.createPet(member, stage.getId()));
        todayReading = todayReadingRepository.save(TestFixture.todayReadingFixtureZeroCurrentCount(member));
        weeklyReading = weeklyReadingRepository.save(TestFixture.weeklyReadingFixture(member));
        continueReading = continueReadingRepository.save(TestFixture.continueReadingFixture(member));
        monthlyReadingSnapshot = monthlyReadingSnapshotRepository.save(TestFixture.monthlyReadingFixture(member));
        monthlyReadingRealtimeRepository.save(MonthlyReadingRealtime.builder()
                .memberId(member.getId())
                .currentCount(0)
                .build());
    }

    @Test
    void 가이드_메일_읽기_시_키우기_점수와_읽기_횟수가_증가한다() {
        // given
        int currentScore = pet.getCurrentScore();
        int currentTodayCount = todayReading.getCurrentCount();
        int currentWeeklyCount = weeklyReading.getCurrentCount();
        int currentContinueDayCount = continueReading.getDayCount();

        // when
        guideMailService.updateReadScore(member.getId());

        // then
        Pet updatedPet = petRepository.findByMemberId(member.getId()).get();
        TodayReading updatedTodayReading = todayReadingRepository.findByMemberId(member.getId()).get();
        WeeklyReading updatedWeeklyReading = weeklyReadingRepository.findByMemberId(member.getId()).get();
        ContinueReading updatedContinueReading = continueReadingRepository.findByMemberId(member.getId()).get();

        assertSoftly(softly -> {
            softly.assertThat(updatedPet.getCurrentScore()).isGreaterThan(currentScore);
            softly.assertThat(updatedTodayReading.getCurrentCount()).isGreaterThan(currentTodayCount);
            softly.assertThat(updatedWeeklyReading.getCurrentCount()).isGreaterThan(currentWeeklyCount);
            softly.assertThat(updatedContinueReading.getDayCount()).isGreaterThan(currentContinueDayCount);
        });
    }
}
