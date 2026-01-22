package me.bombom.api.v1.badge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import me.bombom.api.v1.badge.domain.Badge;
import me.bombom.api.v1.badge.domain.BadgeGrade;
import me.bombom.api.v1.badge.domain.RankingBadge;
import me.bombom.api.v1.badge.repository.BadgeRepository;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.TestFixture;
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

    private Member member1;
    private Member member2;
    private Member member3;
    private LocalDate testPeriod;

    @BeforeEach
    void setUp() {
        badgeRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();

        member1 = memberRepository.save(TestFixture.createUniqueMember("member1", "provider1"));
        member2 = memberRepository.save(TestFixture.createUniqueMember("member2", "provider2"));
        member3 = memberRepository.save(TestFixture.createUniqueMember("member3", "provider3"));
        testPeriod = LocalDate.of(2024, 1, 1);
    }

    @Test
    void 빈_리스트일_때_뱃지를_발급하지_않는다() {
        // given
        List<Long> emptyList = Collections.emptyList();

        // when
        badgeService.issueRankingBadges(emptyList, testPeriod);

        // then
        assertThat(badgeRepository.count()).isZero();
    }

    @Test
    void 상위_3명에게_금_은_동_메달을_발급한다() {
        // given
        List<Long> topRankers = List.of(member1.getId(), member2.getId(), member3.getId());

        // when
        badgeService.issueRankingBadges(topRankers, testPeriod);

        // then
        List<Badge> badges = badgeRepository.findAll();
        assertThat(badges).hasSize(3);

        RankingBadge goldBadge = findRankingBadge(badges, member1.getId(), BadgeGrade.GOLD);
        RankingBadge silverBadge = findRankingBadge(badges, member2.getId(), BadgeGrade.SILVER);
        RankingBadge bronzeBadge = findRankingBadge(badges, member3.getId(), BadgeGrade.BRONZE);

        assertSoftly(softly -> {
            softly.assertThat(goldBadge).isNotNull();
            softly.assertThat(goldBadge.getPeriodYear()).isEqualTo(testPeriod.getYear());
            softly.assertThat(goldBadge.getPeriodMonth()).isEqualTo(testPeriod.getMonthValue());

            softly.assertThat(silverBadge).isNotNull();
            softly.assertThat(silverBadge.getPeriodYear()).isEqualTo(testPeriod.getYear());
            softly.assertThat(silverBadge.getPeriodMonth()).isEqualTo(testPeriod.getMonthValue());

            softly.assertThat(bronzeBadge).isNotNull();
            softly.assertThat(bronzeBadge.getPeriodYear()).isEqualTo(testPeriod.getYear());
            softly.assertThat(bronzeBadge.getPeriodMonth()).isEqualTo(testPeriod.getMonthValue());
        });
    }

    @Test
    void 상위_2명만_있을_때_금_은_메달만_발급한다() {
        // given
        List<Long> topRankers = List.of(member1.getId(), member2.getId());

        // when
        badgeService.issueRankingBadges(topRankers, testPeriod);

        // then
        List<Badge> badges = badgeRepository.findAll();
        assertThat(badges).hasSize(2);

        RankingBadge goldBadge = findRankingBadge(badges, member1.getId(), BadgeGrade.GOLD);
        RankingBadge silverBadge = findRankingBadge(badges, member2.getId(), BadgeGrade.SILVER);

        assertSoftly(softly -> {
            softly.assertThat(goldBadge).isNotNull();
            softly.assertThat(silverBadge).isNotNull();
            softly.assertThat(badges.stream()
                    .filter(b -> b instanceof RankingBadge)
                    .map(b -> (RankingBadge) b)
                    .anyMatch(b -> b.getGrade() == BadgeGrade.BRONZE)).isFalse();
        });
    }

    @Test
    void 상위_1명만_있을_때_금메달만_발급한다() {
        // given
        List<Long> topRankers = List.of(member1.getId());

        // when
        badgeService.issueRankingBadges(topRankers, testPeriod);

        // then
        List<Badge> badges = badgeRepository.findAll();
        assertThat(badges).hasSize(1);

        RankingBadge goldBadge = findRankingBadge(badges, member1.getId(), BadgeGrade.GOLD);

        assertSoftly(softly -> {
            softly.assertThat(goldBadge).isNotNull();
            softly.assertThat(goldBadge.getGrade()).isEqualTo(BadgeGrade.GOLD);
            softly.assertThat(badges.stream()
                    .filter(b -> b instanceof RankingBadge)
                    .map(b -> (RankingBadge) b)
                    .anyMatch(b -> b.getGrade() == BadgeGrade.SILVER || b.getGrade() == BadgeGrade.BRONZE))
                    .isFalse();
        });
    }

    @Test
    void 상위_3명_이상일_때도_최대_3명에게만_뱃지를_발급한다() {
        // given
        Member member4 = memberRepository.save(TestFixture.createUniqueMember("member4", "provider4"));
        List<Long> topRankers = List.of(member1.getId(), member2.getId(), member3.getId(), member4.getId());

        // when
        badgeService.issueRankingBadges(topRankers, testPeriod);

        // then
        List<Badge> badges = badgeRepository.findAll();
        assertThat(badges).hasSize(3);

        assertSoftly(softly -> {
            softly.assertThat(findRankingBadge(badges, member1.getId(), BadgeGrade.GOLD)).isNotNull();
            softly.assertThat(findRankingBadge(badges, member2.getId(), BadgeGrade.SILVER)).isNotNull();
            softly.assertThat(findRankingBadge(badges, member3.getId(), BadgeGrade.BRONZE)).isNotNull();
            softly.assertThat(badges.stream()
                    .anyMatch(b -> b.getMemberId().equals(member4.getId()))).isFalse();
        });
    }

    private RankingBadge findRankingBadge(List<Badge> badges, Long memberId, BadgeGrade grade) {
        return badges.stream()
                .filter(b -> b instanceof RankingBadge)
                .map(b -> (RankingBadge) b)
                .filter(b -> b.getMemberId().equals(memberId) && b.getGrade() == grade)
                .findFirst()
                .orElse(null);
    }
}
