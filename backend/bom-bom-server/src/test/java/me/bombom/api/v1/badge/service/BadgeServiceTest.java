package me.bombom.api.v1.badge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.badge.domain.Badge;
import me.bombom.api.v1.badge.domain.BadgeGrade;
import me.bombom.api.v1.badge.domain.ChallengeBadge;
import me.bombom.api.v1.badge.domain.RankingBadge;
import me.bombom.api.v1.badge.domain.StreakBadge;
import me.bombom.api.v1.badge.repository.BadgeRepository;
import me.bombom.api.v1.challenge.domain.Challenge;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import me.bombom.api.v1.challenge.repository.ChallengeParticipantRepository;
import me.bombom.api.v1.challenge.repository.ChallengeRepository;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.newsletter.domain.NewsletterGroup;
import me.bombom.api.v1.newsletter.repository.NewsletterGroupRepository;
import me.bombom.api.v1.reading.dto.RankerInfo;
import me.bombom.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
class BadgeServiceTest {

    @Autowired
    private BadgeService badgeService;

    @Autowired
    private BadgeRepository badgeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private ChallengeParticipantRepository challengeParticipantRepository;

    @Autowired
    private NewsletterGroupRepository newsletterGroupRepository;

    private Member member1;
    private Member member2;
    private Member member3;
    private LocalDate testPeriod;

    @BeforeEach
    void setUp() {
        badgeRepository.deleteAllInBatch();
        challengeParticipantRepository.deleteAllInBatch();
        challengeRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
        newsletterGroupRepository.deleteAllInBatch();

        member1 = memberRepository.save(TestFixture.createUniqueMember("member1", "provider1"));
        member2 = memberRepository.save(TestFixture.createUniqueMember("member2", "provider2"));
        member3 = memberRepository.save(TestFixture.createUniqueMember("member3", "provider3"));
        testPeriod = LocalDate.of(2024, 1, 1);
    }

    @Test
    void 빈_리스트일_때_뱃지를_발급하지_않는다() {
        // given
        List<RankerInfo> emptyList = Collections.emptyList();

        // when
        badgeService.issueRankingBadges(emptyList, testPeriod);

        // then
        assertThat(badgeRepository.count()).isZero();
    }

    @Test
    void 상위_3명에게_금_은_동_메달을_발급한다() {
        // given
        List<RankerInfo> topRankers = List.of(
                new RankerInfo(member1.getId(), 1),
                new RankerInfo(member2.getId(), 2),
                new RankerInfo(member3.getId(), 3)
        );

        // when
        badgeService.issueRankingBadges(topRankers, testPeriod);

        // then
        List<Badge> badges = badgeRepository.findAll();
        assertThat(badges).hasSize(3);

        Optional<RankingBadge> goldBadge = findRankingBadge(badges, member1.getId(), BadgeGrade.GOLD);
        Optional<RankingBadge> silverBadge = findRankingBadge(badges, member2.getId(), BadgeGrade.SILVER);
        Optional<RankingBadge> bronzeBadge = findRankingBadge(badges, member3.getId(), BadgeGrade.BRONZE);

        assertSoftly(softly -> {
            softly.assertThat(goldBadge).isPresent();
            softly.assertThat(goldBadge.get().getPeriodYear()).isEqualTo(testPeriod.getYear());
            softly.assertThat(goldBadge.get().getPeriodMonth()).isEqualTo(testPeriod.getMonthValue());

            softly.assertThat(silverBadge).isPresent();
            softly.assertThat(silverBadge.get().getPeriodYear()).isEqualTo(testPeriod.getYear());
            softly.assertThat(silverBadge.get().getPeriodMonth()).isEqualTo(testPeriod.getMonthValue());

            softly.assertThat(bronzeBadge).isPresent();
            softly.assertThat(bronzeBadge.get().getPeriodYear()).isEqualTo(testPeriod.getYear());
            softly.assertThat(bronzeBadge.get().getPeriodMonth()).isEqualTo(testPeriod.getMonthValue());
        });
    }

    @Test
    void 상위_2명만_있을_때_금_은_메달만_발급한다() {
        // given
        List<RankerInfo> topRankers = List.of(
            new RankerInfo(member1.getId(), 1),
            new RankerInfo(member2.getId(), 2)
        );

        // when
        badgeService.issueRankingBadges(topRankers, testPeriod);

        // then
        List<Badge> badges = badgeRepository.findAll();
        assertThat(badges).hasSize(2);

        Optional<RankingBadge> goldBadge = findRankingBadge(badges, member1.getId(), BadgeGrade.GOLD);
        Optional<RankingBadge> silverBadge = findRankingBadge(badges, member2.getId(), BadgeGrade.SILVER);

        assertSoftly(softly -> {
            softly.assertThat(goldBadge).isPresent();
            softly.assertThat(silverBadge).isPresent();
            softly.assertThat(badges.stream()
                    .filter(b -> b instanceof RankingBadge)
                    .map(b -> (RankingBadge) b)
                    .anyMatch(b -> b.getGrade() == BadgeGrade.BRONZE)).isFalse();
        });
    }

    @Test
    void 상위_1명만_있을_때_금메달만_발급한다() {
        // given
        List<RankerInfo> topRankers = List.of(new RankerInfo(member1.getId(), 1));

        // when
        badgeService.issueRankingBadges(topRankers, testPeriod);

        // then
        List<Badge> badges = badgeRepository.findAll();
        assertThat(badges).hasSize(1);

        Optional<RankingBadge> goldBadge = findRankingBadge(badges, member1.getId(), BadgeGrade.GOLD);

        assertSoftly(softly -> {
            softly.assertThat(goldBadge).isPresent();
            softly.assertThat(goldBadge.get().getGrade()).isEqualTo(BadgeGrade.GOLD);
            softly.assertThat(badges.stream()
                    .filter(b -> b instanceof RankingBadge)
                    .map(b -> (RankingBadge) b)
                    .anyMatch(
                        b -> b.getGrade() == BadgeGrade.SILVER || b.getGrade() == BadgeGrade.BRONZE))
                    .isFalse();
        });
    }

    @Test
    void 공동_1등이면_모두_금메달을_받는다() {
        // given
        Member member4 = memberRepository.save(TestFixture.createUniqueMember("member4", "provider4"));
        List<RankerInfo> topRankers = List.of(
            new RankerInfo(member1.getId(), 1),
            new RankerInfo(member2.getId(), 1),
            new RankerInfo(member3.getId(), 1),
            new RankerInfo(member4.getId(), 2)
        );

        // when
        badgeService.issueRankingBadges(topRankers, testPeriod);

        // then
        List<Badge> badges = badgeRepository.findAll();
        assertThat(badges).hasSize(4);

        assertSoftly(softly -> {
            softly.assertThat(
                    findRankingBadge(badges, member1.getId(), BadgeGrade.GOLD)).isPresent();
            softly.assertThat(
                    findRankingBadge(badges, member2.getId(), BadgeGrade.GOLD)).isPresent();
            softly.assertThat(
                    findRankingBadge(badges, member3.getId(), BadgeGrade.GOLD)).isPresent();
            softly.assertThat(
                    findRankingBadge(badges, member4.getId(), BadgeGrade.SILVER)).isPresent();
        });
    }

    private Optional<RankingBadge> findRankingBadge(List<Badge> badges, Long memberId, BadgeGrade grade) {
        return badges.stream()
            .filter(b -> b instanceof RankingBadge)
            .map(b -> (RankingBadge) b)
            .filter(b -> b.getMemberId().equals(memberId) && b.getGrade() == grade)
            .findFirst();
    }

    @Test
    void 챌린지_참가자_리스트가_비어있을_때_뱃지를_발급하지_않는다() {
        // given
        NewsletterGroup group = TestFixture.createNewsletterGroup("그룹");
        newsletterGroupRepository.save(group);
        Challenge challenge = challengeRepository.save(TestFixture.createChallenge(
            "Test Challenge",
            LocalDate.now().minusDays(10),
            LocalDate.now().minusDays(1),
            10,
            group.getId()
        ));

        // when
        badgeService.issueChallengeBadges(challenge, List.of());

        // then
        assertThat(badgeRepository.count()).isZero();
    }

    @Test
    void 진행률_100퍼센트인_참가자에게_금메달을_발급한다() {
        // given
        NewsletterGroup goldGroup = TestFixture.createNewsletterGroup("금메달 그룹");
        newsletterGroupRepository.save(goldGroup);
        Challenge challenge = challengeRepository.save(TestFixture.createChallenge(
            "Test Challenge",
            LocalDate.now().minusDays(10),
            LocalDate.now().minusDays(1),
            10,
            goldGroup.getId()
        ));
        ChallengeParticipant participant = challengeParticipantRepository.save(
            TestFixture.createChallengeParticipant(
                challenge.getId(),
                member1.getId(),
                10, // 10일 중 10일 완료 = 100%
                true
            ));

        // when
        badgeService.issueChallengeBadges(challenge, List.of(participant));

        // then
        List<Badge> badges = badgeRepository.findAll();
        assertThat(badges).hasSize(1);

        Optional<ChallengeBadge> badge = findChallengeBadge(badges, member1.getId(),
            BadgeGrade.GOLD);
        assertSoftly(softly -> {
            softly.assertThat(badge).isPresent();
            softly.assertThat(badge.get().getChallengeId()).isEqualTo(challenge.getId());
            softly.assertThat(badge.get().getChallengeName()).isEqualTo(challenge.getName());
            softly.assertThat(badge.get().getChallengeGeneration())
                .isEqualTo(challenge.getGeneration());
        });
    }

    @Test
    void 진행률_90퍼센트인_참가자에게_은메달을_발급한다() {
        // given
        NewsletterGroup silverGroup = TestFixture.createNewsletterGroup("은메달 그룹");
        newsletterGroupRepository.save(silverGroup);
        Challenge challenge = challengeRepository.save(TestFixture.createChallenge(
            "Test Challenge",
            LocalDate.now().minusDays(10),
            LocalDate.now().minusDays(1),
            10,
            silverGroup.getId()
        ));
        ChallengeParticipant participant = challengeParticipantRepository.save(
            TestFixture.createChallengeParticipant(
                challenge.getId(),
                member1.getId(),
                9, // 10일 중 9일 완료 = 90%
                true
            ));

        // when
        badgeService.issueChallengeBadges(challenge, List.of(participant));

        // then
        List<Badge> badges = badgeRepository.findAll();
        assertThat(badges).hasSize(1);

        Optional<ChallengeBadge> badge = findChallengeBadge(badges, member1.getId(),
            BadgeGrade.SILVER);
        assertSoftly(softly -> {
            softly.assertThat(badge).isPresent();
            softly.assertThat(badge.get().getGrade()).isEqualTo(BadgeGrade.SILVER);
        });
    }

    @Test
    void 진행률_80퍼센트인_참가자에게_동메달을_발급한다() {
        // given
        NewsletterGroup bronzeGroup = TestFixture.createNewsletterGroup("동메달 그룹");
        newsletterGroupRepository.save(bronzeGroup);
        Challenge challenge = challengeRepository.save(TestFixture.createChallenge(
            "Test Challenge",
            LocalDate.now().minusDays(10),
            LocalDate.now().minusDays(1),
            10,
            bronzeGroup.getId()
        ));
        ChallengeParticipant participant = challengeParticipantRepository.save(
            TestFixture.createChallengeParticipant(
                challenge.getId(),
                member1.getId(),
                8, // 10일 중 8일 완료 = 80%
                true
            ));

        // when
        badgeService.issueChallengeBadges(challenge, List.of(participant));

        // then
        List<Badge> badges = badgeRepository.findAll();
        assertThat(badges).hasSize(1);

        Optional<ChallengeBadge> badge = findChallengeBadge(badges, member1.getId(),
            BadgeGrade.BRONZE);
        assertSoftly(softly -> {
            softly.assertThat(badge).isPresent();
            softly.assertThat(badge.get().getGrade()).isEqualTo(BadgeGrade.BRONZE);
        });
    }

    @Test
    void 진행률_80퍼센트_미만인_참가자에게는_뱃지를_발급하지_않는다() {
        // given
        NewsletterGroup noBadgeGroup = TestFixture.createNewsletterGroup("뱃지없음 그룹");
        newsletterGroupRepository.save(noBadgeGroup);
        Challenge challenge = challengeRepository.save(TestFixture.createChallenge(
            "Test Challenge",
            LocalDate.now().minusDays(10),
            LocalDate.now().minusDays(1),
            10,
            noBadgeGroup.getId()
        ));
        ChallengeParticipant participant = challengeParticipantRepository.save(
            TestFixture.createChallengeParticipant(
                challenge.getId(),
                member1.getId(),
                7, // 10일 중 7일 완료 = 70%
                true
            ));

        // when
        badgeService.issueChallengeBadges(challenge, List.of(participant));

        // then
        assertThat(badgeRepository.count()).isZero();
    }

    @Test
    void 탈락한_참가자에게는_뱃지를_발급하지_않는다() {
        // given
        NewsletterGroup failedGroup = TestFixture.createNewsletterGroup("탈락 그룹");
        newsletterGroupRepository.save(failedGroup);
        Challenge challenge = challengeRepository.save(TestFixture.createChallenge(
            "Test Challenge",
            LocalDate.now().minusDays(10),
            LocalDate.now().minusDays(1),
            10,
            failedGroup.getId()
        ));
        ChallengeParticipant participant = challengeParticipantRepository.save(
            TestFixture.createChallengeParticipant(
                challenge.getId(),
                member1.getId(),
                10, // 100% 완료했지만
                false
            )); // 탈락

        // when
        badgeService.issueChallengeBadges(challenge, List.of(participant));

        // then
        assertThat(badgeRepository.count()).isZero();
    }

    @Test
    void 여러_참가자가_각각_다른_등급의_뱃지를_받는다() {
        // given
        NewsletterGroup multiGroup = TestFixture.createNewsletterGroup("다중 그룹");
        newsletterGroupRepository.save(multiGroup);
        Challenge challenge = challengeRepository.save(TestFixture.createChallenge(
            "Test Challenge",
            LocalDate.now().minusDays(10),
            LocalDate.now().minusDays(1),
            10,
            multiGroup.getId()
        ));
        ChallengeParticipant goldParticipant = challengeParticipantRepository.save(
            TestFixture.createChallengeParticipant(
                challenge.getId(),
                member1.getId(),
                10, // 100% → 금메달
                true
            ));
        ChallengeParticipant silverParticipant = challengeParticipantRepository.save(
            TestFixture.createChallengeParticipant(
                challenge.getId(),
                member2.getId(),
                9, // 90% → 은메달
                true
            ));
        ChallengeParticipant bronzeParticipant = challengeParticipantRepository.save(
            TestFixture.createChallengeParticipant(
                challenge.getId(),
                member3.getId(),
                8, // 80% → 동메달
                true
            ));

        // when
        badgeService.issueChallengeBadges(challenge, List.of(
            goldParticipant, silverParticipant, bronzeParticipant));

        // then
        List<Badge> badges = badgeRepository.findAll();
        assertThat(badges).hasSize(3);

        assertSoftly(softly -> {
            softly.assertThat(findChallengeBadge(badges, member1.getId(), BadgeGrade.GOLD))
                    .isPresent();
            softly.assertThat(findChallengeBadge(badges, member2.getId(), BadgeGrade.SILVER))
                    .isPresent();
            softly.assertThat(findChallengeBadge(badges, member3.getId(), BadgeGrade.BRONZE))
                    .isPresent();
        });
    }

    @Test
    void 진행률_경계값_테스트() {
        // given
        NewsletterGroup boundaryGroup = TestFixture.createNewsletterGroup("경계값 그룹");
        newsletterGroupRepository.save(boundaryGroup);
        Challenge challenge = challengeRepository.save(TestFixture.createChallenge(
            "Test Challenge",
            LocalDate.now().minusDays(10),
            LocalDate.now().minusDays(1),
            10,
            boundaryGroup.getId()
        ));

        // when & then: 100% (10/10) → 금메달
        ChallengeParticipant gold100 = challengeParticipantRepository.save(
            TestFixture.createChallengeParticipant(challenge.getId(), member1.getId(), 10, true));
        badgeService.issueChallengeBadges(challenge, List.of(gold100));
        assertThat(findChallengeBadge(badgeRepository.findAll(), member1.getId(), BadgeGrade.GOLD)).isPresent();
        badgeRepository.deleteAllInBatch();
        challengeParticipantRepository.deleteAllInBatch();

        // when & then: 90% (9/10) → 은메달
        ChallengeParticipant silver90 = challengeParticipantRepository.save(
            TestFixture.createChallengeParticipant(challenge.getId(), member1.getId(), 9, true));
        badgeService.issueChallengeBadges(challenge, List.of(silver90));
        assertThat(findChallengeBadge(badgeRepository.findAll(), member1.getId(), BadgeGrade.SILVER)).isPresent();
        badgeRepository.deleteAllInBatch();
        challengeParticipantRepository.deleteAllInBatch();

        // when & then: 80% (8/10) → 동메달
        ChallengeParticipant bronze80 = challengeParticipantRepository.save(
            TestFixture.createChallengeParticipant(challenge.getId(), member1.getId(), 8, true));
        badgeService.issueChallengeBadges(challenge, List.of(bronze80));
        assertThat(findChallengeBadge(badgeRepository.findAll(), member1.getId(), BadgeGrade.BRONZE)).isPresent();
        badgeRepository.deleteAllInBatch();
        challengeParticipantRepository.deleteAllInBatch();

        // when & then: 79% (7/10) → 뱃지 없음
        ChallengeParticipant noBadge = challengeParticipantRepository.save(
            TestFixture.createChallengeParticipant(challenge.getId(), member1.getId(), 7, true));
        badgeService.issueChallengeBadges(challenge, List.of(noBadge));
        assertThat(badgeRepository.count()).isZero();
    }

    private Optional<ChallengeBadge> findChallengeBadge(List<Badge> badges, Long memberId, BadgeGrade grade) {
        return badges.stream()
            .filter(b -> b instanceof ChallengeBadge)
            .map(b -> (ChallengeBadge) b)
            .filter(b -> b.getMemberId().equals(memberId) && b.getGrade() == grade)
            .findFirst();
    }

    @Test
    void 연속_읽기_일수가_마일스톤에_도달하면_스트릭_뱃지를_발급한다() {
        badgeService.issueStreakBadge(member1.getId(), 7);

        List<Badge> badges = badgeRepository.findAll();
        assertThat(badges).hasSize(1);
        assertThat(badges.get(0)).isInstanceOf(StreakBadge.class);
        StreakBadge streakBadge = (StreakBadge) badges.get(0);
        assertThat(streakBadge.getStreakDayCount()).isEqualTo(7);
    }

    @Test
    void 마일스톤이_아니면_스트릭_뱃지를_발급하지_않는다() {
        badgeService.issueStreakBadge(member1.getId(), 6);

        assertThat(badgeRepository.count()).isZero();
    }

    @Test
    void 동일_마일스톤도_여러_번_발급할_수_있다() {
        badgeService.issueStreakBadge(member1.getId(), 100);
        badgeService.issueStreakBadge(member1.getId(), 100);

        List<Badge> badges = badgeRepository.findAll();
        assertThat(badges).hasSize(2);
        assertThat(badges.stream().filter(b -> b instanceof StreakBadge).count()).isEqualTo(2);
    }
}
