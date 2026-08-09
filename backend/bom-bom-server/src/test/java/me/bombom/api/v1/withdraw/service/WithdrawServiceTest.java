package me.bombom.api.v1.withdraw.service;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.article.domain.ArticleReadHistory;
import me.bombom.api.v1.article.repository.ArticleReadHistoryRepository;
import me.bombom.api.v1.badge.domain.StreakBadge;
import me.bombom.api.v1.badge.repository.BadgeRepository;
import me.bombom.api.v1.bookmark.domain.Bookmark;
import me.bombom.api.v1.bookmark.repository.BookmarkRepository;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import me.bombom.api.v1.challenge.repository.ChallengeParticipantRepository;
import me.bombom.api.v1.highlight.domain.Color;
import me.bombom.api.v1.highlight.domain.Highlight;
import me.bombom.api.v1.highlight.domain.HighlightLocation;
import me.bombom.api.v1.highlight.repository.HighlightRepository;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.enums.Gender;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.reading.domain.ContinueReadingRealtime;
import me.bombom.api.v1.reading.repository.ContinueReadingRealtimeRepository;
import me.bombom.api.v1.withdraw.domain.WithdrawnMember;
import me.bombom.api.v1.withdraw.repository.WithdrawnMemberRepository;
import me.bombom.support.integration.IntegrationTest;
import me.bombom.support.time.MutableClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
class WithdrawServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 1, 1);

    @Autowired
    private WithdrawService withdrawService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private WithdrawnMemberRepository withdrawnMemberRepository;

    @Autowired
    private HighlightRepository highlightRepository;

    @Autowired
    private ContinueReadingRealtimeRepository continueReadingRepository;

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private BadgeRepository badgeRepository;

    @Autowired
    private ChallengeParticipantRepository challengeParticipantRepository;

    @Autowired
    private ArticleReadHistoryRepository articleReadHistoryRepository;

    @Autowired
    private MutableClock clock;

    @BeforeEach
    void setUp() {
        clock.setDate(TODAY);
    }

    @Test
    void 회원_탈퇴_시_탈퇴_회원_정보로_이전된다() {
        // given
        Member member = memberRepository.save(TestFixture.normalMemberFixture());
        continueReadingRepository.save(ContinueReadingRealtime.builder()
                .memberId(member.getId())
                .dayCount(10)
                .build());
        bookmarkRepository.save(Bookmark.builder()
                .memberId(member.getId())
                .articleId(1L)
                .build());
        saveHighlights(member.getId(), 6);

        // when
        withdrawService.migrateDeletedMember(member);

        // then
        List<WithdrawnMember> withdrawn = withdrawnMemberRepository.findAll();
        assertSoftly(softly -> {
            softly.assertThat(withdrawn).hasSize(1);
            softly.assertThat(withdrawn.getFirst().getMemberId()).isEqualTo(member.getId());
            softly.assertThat(withdrawn.getFirst().getEmail()).isEqualTo(member.getEmail());
            softly.assertThat(withdrawn.getFirst().getContinueReading()).isEqualTo(10);
            softly.assertThat(withdrawn.getFirst().getBookmarkedCount()).isEqualTo(1);
            softly.assertThat(withdrawn.getFirst().getHighlightCount()).isEqualTo(6);
            softly.assertThat(withdrawn.getFirst().isCleanupCompleted()).isFalse();
        });
    }

    @Test
    void 마이그레이션_시_가입경로와_활동량_지표가_수집된다() {
        // given
        Member member = memberRepository.save(TestFixture.uniqueMemberFixture());
        Long memberId = member.getId();

        badgeRepository.save(StreakBadge.builder().memberId(memberId).streakDayCount(3).build());
        challengeParticipantRepository.save(ChallengeParticipant.builder()
                .challengeId(1L)
                .memberId(memberId)
                .build());
        articleReadHistoryRepository.save(ArticleReadHistory.builder()
                .memberId(memberId)
                .articleId(1L)
                .newsletterId(1L)
                .categoryId(1L)
                .readAt(TODAY.atStartOfDay())
                .build());
        articleReadHistoryRepository.save(ArticleReadHistory.builder()
                .memberId(memberId)
                .articleId(2L)
                .newsletterId(1L)
                .categoryId(1L)
                .readAt(LocalDateTime.of(TODAY, LocalTime.NOON))
                .build());

        // when
        withdrawService.migrateDeletedMember(member);

        // then
        WithdrawnMember withdrawn = withdrawnMemberRepository.findAll().getFirst();
        assertSoftly(softly -> {
            softly.assertThat(withdrawn.getProvider()).isEqualTo("apple");
            softly.assertThat(withdrawn.getBadgeCount()).isEqualTo(1);
            softly.assertThat(withdrawn.getChallengeCount()).isEqualTo(1);
            softly.assertThat(withdrawn.getTotalReadCount()).isEqualTo(2);
            softly.assertThat(withdrawn.getLastReadDate()).isEqualTo(TODAY);
        });
    }

    @Test
    void 연속_읽기_정보가_없어도_회원_탈퇴_정보는_0일로_이전된다() {
        // given
        Member member = memberRepository.save(TestFixture.normalMemberFixture());

        // when
        withdrawService.migrateDeletedMember(member);

        // then
        List<WithdrawnMember> withdrawn = withdrawnMemberRepository.findAll();
        assertSoftly(softly -> {
            softly.assertThat(withdrawn).hasSize(1);
            softly.assertThat(withdrawn.getFirst().getMemberId()).isEqualTo(member.getId());
            softly.assertThat(withdrawn.getFirst().getContinueReading()).isZero();
            softly.assertThat(withdrawn.getFirst().getBookmarkedCount()).isZero();
            softly.assertThat(withdrawn.getFirst().getHighlightCount()).isZero();
        });
    }

    @Test
    void 만료일이_오늘인_탈퇴_회원_정보는_삭제된다() {
        // given
        WithdrawnMember expired = WithdrawnMember.builder()
                .memberId(1L)
                .email("expired@bombom.news")
                .gender(Gender.MALE)
                .joinedDate(TODAY.minusDays(200))
                .deletedDate(TODAY.minusDays(90))
                .expireDate(TODAY) // 오늘 만료
                .build();
        withdrawnMemberRepository.save(expired);

        WithdrawnMember notExpired = WithdrawnMember.builder()
                .memberId(2L)
                .email("not-expired@bombom.news")
                .gender(Gender.FEMALE)
                .joinedDate(TODAY.minusDays(200))
                .deletedDate(TODAY.minusDays(79))
                .expireDate(TODAY.plusDays(1)) // 아직 유효
                .build();
        withdrawnMemberRepository.save(notExpired);

        WithdrawnMember cleanupNotCompleted = WithdrawnMember.builder()
                .memberId(3L)
                .email("cleanup-not-completed@bombom.news")
                .gender(Gender.NONE)
                .joinedDate(TODAY.minusDays(200))
                .deletedDate(TODAY.minusDays(90))
                .expireDate(TODAY) // 오늘 만료됐지만 정리 미완료
                .build();
        withdrawnMemberRepository.save(cleanupNotCompleted);

        // when
        withdrawService.completeCleanup(1L);
        withdrawService.deleteExpiredWithdrawnMembers();

        // then
        List<WithdrawnMember> remaining = withdrawnMemberRepository.findAll();
        assertSoftly(softly -> {
            softly.assertThat(remaining).extracting(WithdrawnMember::getMemberId)
                    .containsExactlyInAnyOrder(2L, 3L);
        });
    }

    private void saveHighlights(Long memberId, int count) {
        for (int index = 0; index < count; index++) {
            highlightRepository.save(Highlight.builder()
                    .highlightLocation(new HighlightLocation(index, "div[0]/p[0]", index + 1, "div[0]/p[0]"))
                    .memberId(memberId)
                    .articleId((long) index + 1)
                    .newsletterId(1L)
                    .color(Color.from("#ffeb3b"))
                    .title("아티클")
                    .text("하이라이트")
                    .build());
        }
    }
}
