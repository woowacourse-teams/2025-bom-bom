package me.bombom.api.v1;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import me.bombom.api.v1.article.domain.Article;
import me.bombom.api.v1.article.domain.ArticleReadHistory;
import me.bombom.api.v1.article.domain.RecentArticle;
import me.bombom.api.v1.blog.domain.BlogCategory;
import me.bombom.api.v1.blog.domain.BlogHashtag;
import me.bombom.api.v1.blog.domain.BlogImageAsset;
import me.bombom.api.v1.blog.domain.BlogImageAssetStatus;
import me.bombom.api.v1.blog.domain.BlogPost;
import me.bombom.api.v1.blog.domain.BlogPostStatus;
import me.bombom.api.v1.blog.domain.BlogPostTag;
import me.bombom.api.v1.blog.domain.BlogPostVisibility;
import me.bombom.api.v1.bookmark.domain.Bookmark;
import me.bombom.api.v1.challenge.domain.Challenge;
import me.bombom.api.v1.challenge.domain.ChallengeComment;
import me.bombom.api.v1.challenge.domain.ChallengeCommentReply;
import me.bombom.api.v1.challenge.domain.ChallengeDailyGuide;
import me.bombom.api.v1.challenge.domain.ChallengeDailyGuideComment;
import me.bombom.api.v1.challenge.domain.ChallengeDailyResult;
import me.bombom.api.v1.challenge.domain.ChallengeDailyStatus;
import me.bombom.api.v1.challenge.domain.ChallengeDailyTodo;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import me.bombom.api.v1.challenge.domain.ChallengeTeam;
import me.bombom.api.v1.challenge.domain.ChallengeTodo;
import me.bombom.api.v1.challenge.domain.ChallengeTodoType;
import me.bombom.api.v1.challenge.domain.DailyGuideType;
import me.bombom.api.v1.highlight.domain.Color;
import me.bombom.api.v1.highlight.domain.Highlight;
import me.bombom.api.v1.highlight.domain.HighlightLocation;
import me.bombom.api.v1.highlight.dto.request.HighlightCreateRequest;
import me.bombom.api.v1.highlight.dto.request.HighlightLocationRequest;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.enums.Gender;
import me.bombom.api.v1.newsletter.domain.Category;
import me.bombom.api.v1.newsletter.domain.Newsletter;
import me.bombom.api.v1.newsletter.domain.NewsletterDetail;
import me.bombom.api.v1.newsletter.domain.NewsletterGroup;
import me.bombom.api.v1.newsletter.domain.NewsletterGroupItem;
import me.bombom.api.v1.newsletter.domain.NewsletterPreviousPolicy;
import me.bombom.api.v1.newsletter.domain.NewsletterPreviousStrategy;
import me.bombom.api.v1.newsletter.domain.NewsletterPublicationStatus;
import me.bombom.api.v1.newsletter.domain.NewsletterSource;
import me.bombom.api.v1.notice.domain.Notice;
import me.bombom.api.v1.notice.domain.NoticeCategory;
import me.bombom.api.v1.pet.domain.Pet;
import me.bombom.api.v1.pet.domain.Stage;
import me.bombom.api.v1.reading.domain.ContinueReadingRealtime;
import me.bombom.api.v1.reading.domain.MonthlyReadingRealtime;
import me.bombom.api.v1.reading.domain.MonthlyReadingSnapshot;
import me.bombom.api.v1.reading.domain.TodayReading;
import me.bombom.api.v1.reading.domain.WeeklyReading;
import me.bombom.api.v1.subscribe.domain.NewsletterSubscriptionCount;
import me.bombom.api.v1.subscribe.domain.Subscribe;

public final class TestFixture {

    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2025, 7, 15, 10, 0);

    private TestFixture() {
    }

    /**
     * Member
     */

    public static Member createUniqueMember(String nickname, String providerId) {
        return Member.builder()
                .provider("provider")
                .providerId(providerId)
                .email("email@bombom.news")
                .nickname(nickname)
                .gender(Gender.FEMALE)
                .roleId(1L)
                .build();
    }

    public static Member normalMemberFixture() {
        return Member.builder()
                .provider("apple")
                .providerId("providerId")
                .email("email@bombom.news")
                .nickname("nickname")
                .gender(Gender.FEMALE)
                .roleId(1L)
                .build();
    }

    public static Member uniqueMemberFixture() {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return Member.builder()
                .provider("apple")
                .providerId("providerId" + suffix)
                .email("email" + suffix + "@bombom.news")
                .nickname("nickname" + suffix)
                .gender(Gender.FEMALE)
                .roleId(1L)
                .build();
    }

    public static Member createMemberFixture(String email, String nickname) {
        return Member.builder()
                .provider("provider")
                .providerId("providerId")
                .email(email)
                .nickname(nickname)
                .gender(Gender.FEMALE)
                .roleId(1L)
                .build();
    }

    public static Member createMemberWithRole(String nickname, String providerId, Long roleId) {
        return Member.builder()
                .provider("provider")
                .providerId(providerId)
                .email(providerId + "@bombom.news")
                .nickname(nickname)
                .gender(Gender.FEMALE)
                .roleId(roleId)
                .build();
    }

    /**
     * Category
     */
    public static Category createCategory() {
        return Category.builder()
                .name("경제")
                .build();
    }

    public static List<Category> createCategories() {
        return List.of(
                Category.builder()
                        .name("경제")
                        .build(),
                Category.builder()
                        .name("테크")
                        .build(),
                Category.builder()
                        .name("푸드")
                        .build()
        );
    }

    /**
     * Newsletter
     */
    public static List<Newsletter> createNewslettersWithDetails(List<Category> categories,
                                                                List<NewsletterDetail> details) {
        return createNewsletters(
                categories,
                List.of(details.get(0).getId(), details.get(1).getId(), details.get(2).getId(), details.get(3).getId())
        );
    }

    public static Newsletter createNewsletter(String name, String email, Long categoryId, Long detailId) {
        return createNewsletter(name, email, categoryId, detailId, NewsletterSource.EXTERNAL);
    }

    public static Newsletter createNewsletter(
            String name,
            String email,
            Long categoryId,
            Long detailId,
            NewsletterSource source
    ) {
        return Newsletter.builder()
                .name(name)
                .description("설명")
                .imageUrl("https://cdn.bombom.me/img.png")
                .email(email)
                .categoryId(categoryId)
                .detailId(detailId)
                .source(source)
                .build();
    }

    public static Newsletter createSuspendedNewsletter(
            String name,
            String email,
            Long categoryId,
            Long detailId,
            LocalDate suspendedAt
    ) {
        return Newsletter.builder()
                .name(name)
                .description("설명")
                .imageUrl("https://cdn.bombom.me/img.png")
                .email(email)
                .categoryId(categoryId)
                .detailId(detailId)
                .status(NewsletterPublicationStatus.SUSPENDED)
                .suspendedAt(suspendedAt)
                .build();
    }

    private static List<Newsletter> createNewsletters(List<Category> categories, List<Long> detailIds) {
        return List.of(
                createNewsletter("뉴스픽", "news@newspick.com", categories.get(0).getId(), detailIds.get(0)),
                createNewsletter("IT타임즈", "editor@ittimes.io", categories.get(1).getId(), detailIds.get(1)),
                createNewsletter("비즈레터", "biz@biz.com", categories.get(2).getId(), detailIds.get(2)),
                createNewsletter("우테코", "woowa@biz.com", categories.get(2).getId(), detailIds.get(3))
        );
    }

    /**
     * NewsletterSubscriptionCount
     */
    public static List<NewsletterSubscriptionCount> createNewsletterSubscriptionCounts(List<Newsletter> newsletters) {
        return List.of(
                NewsletterSubscriptionCount.builder()
                        .newsletterId(newsletters.get(0).getId())
                        .total(1000)
                        .build(),
                NewsletterSubscriptionCount.builder()
                        .newsletterId(newsletters.get(1).getId())
                        .total(850)
                        .build(),
                NewsletterSubscriptionCount.builder()
                        .newsletterId(newsletters.get(2).getId())
                        .total(600)
                        .build(),
                NewsletterSubscriptionCount.builder()
                        .newsletterId(newsletters.get(3).getId())
                        .total(900)
                        .build()
        );
    }

    public static NewsletterSubscriptionCount createNewsletterSubscriptionCount(Long newsletterId, int total) {
        return NewsletterSubscriptionCount.builder()
                .newsletterId(newsletterId)
                .total(total)
                .build();
    }

    /**
     * NewsletterDetail
     */
    public static List<NewsletterDetail> createNewsletterDetails() {
        return List.of(
                NewsletterDetail.builder()
                        .mainPageUrl("https://news1.com")
                        .subscribeUrl("https://news1.com/subscribe")
                        .issueCycle("매일 발행")
                        .subscribeCount(1000)
                        .sender("발신자")
                        .build(),
                NewsletterDetail.builder()
                        .mainPageUrl("https://ittimes.com")
                        .subscribeUrl("https://ittimes.com/subscribe")
                        .issueCycle("매주 월요일")
                        .subscribeCount(850)
                        .sender("발신자")
                        .build(),
                NewsletterDetail.builder()
                        .mainPageUrl("https://biz.com")
                        .subscribeUrl("https://biz.com/subscribe")
                        .issueCycle("격주 화요일")
                        .subscribeCount(600)
                        .sender("발신자")
                        .build(),
                NewsletterDetail.builder()
                        .mainPageUrl("https://biz.com")
                        .subscribeUrl("https://biz.com/subscribe")
                        .issueCycle("격주 토요일")
                        .subscribeCount(900)
                        .sender("발신자")
                        .build()
        );
    }

    public static NewsletterDetail createNewsletterDetail(boolean previousAllowed) {
        return NewsletterDetail.builder()
                .mainPageUrl("https://news1.com")
                .subscribeUrl("https://news1.com/subscribe")
                .issueCycle("매일 발행")
                .subscribeCount(1000)
                .sender("발신자")
                .previousAllowed(previousAllowed)
                .build();
    }

    /**
     * Subscribe
     */
    public static Subscribe createSubscribe(Newsletter newsletter, Member member) {
        return Subscribe.builder()
                .newsletterId(newsletter.getId())
                .memberId(member.getId())
                .build();
    }

    /**
     * Bookmark
     */
    public static Bookmark createBookmark(Member member, Long articleId) {
        return Bookmark.builder()
                .memberId(member.getId())
                .articleId(articleId)
                .build();
    }

    /**
     * Article 11 개
     */
    public static List<Article> createArticles(Member member, List<Newsletter> newsletters) {
        return List.of(
                createArticle("뉴스", member.getId(), newsletters.get(0).getId(), BASE_TIME.minusMinutes(5)),
                createArticle("뉴스", member.getId(), newsletters.get(1).getId(), BASE_TIME.minusMinutes(10)),
                createArticle("레터", member.getId(), newsletters.get(2).getId(), BASE_TIME.minusMinutes(20)),
                createArticle("레터", member.getId(), newsletters.get(2).getId(), BASE_TIME.minusDays(1)),
                createArticle("공지", member.getId(), newsletters.get(3).getId(), BASE_TIME.minusDays(2)),
                createArticle("공지", member.getId(), newsletters.get(3).getId(), BASE_TIME.minusDays(3)),
                createArticle("공지", member.getId(), newsletters.get(3).getId(), BASE_TIME.minusDays(4)),
                createArticle("공지", member.getId(), newsletters.get(3).getId(), BASE_TIME.minusDays(5)),
                createArticle("공지", member.getId(), newsletters.get(3).getId(), BASE_TIME.minusDays(6)),
                createArticle("공지", member.getId(), newsletters.get(3).getId(), BASE_TIME.minusDays(7)),
                createArticle("공지", member.getId(), newsletters.get(3).getId(), BASE_TIME.minusDays(8))
        );
    }

    public static Article createArticle(String title, Long memberId, Long newsletterId, LocalDateTime arrivedTime) {
        return Article.builder()
                .title(title)
                .contents("<h1>아티클</h1>")
                .contentsText("아티클")
                .thumbnailUrl("https://example.com/images/thumb.png")
                .expectedReadTime(5)
                .contentsSummary("요약")
                .isRead(false)
                .memberId(memberId)
                .newsletterId(newsletterId)
                .arrivedDateTime(arrivedTime)
                .build();
    }

    public static ArticleReadHistory createArticleReadHistory(
            Member member,
            Long articleId,
            Newsletter newsletter,
            LocalDateTime readAt
    ) {
        return ArticleReadHistory.builder()
                .memberId(member.getId())
                .articleId(articleId)
                .newsletterId(newsletter.getId())
                .categoryId(newsletter.getCategoryId())
                .readAt(readAt)
                .build();
    }

    /**
     * RecentArticle
     */
    public static RecentArticle createRecentArticle(String title, Long memberId, Long newsletterId,
                                                    LocalDateTime arrivedTime) {
        return createRecentArticle(title, memberId, newsletterId, arrivedTime, memberId);
    }

    public static RecentArticle createRecentArticle(String title, Long memberId, Long newsletterId,
                                                    LocalDateTime arrivedTime, Long articleId) {
        return RecentArticle.builder()
                .articleId(articleId)
                .title(title)
                .contents("<h1>" + title + "</h1>")
                .contentsText(title)
                .thumbnailUrl("https://example.com/images/thumb.png")
                .expectedReadTime(5)
                .contentsSummary("요약")
                .isRead(false)
                .memberId(memberId)
                .newsletterId(newsletterId)
                .arrivedDateTime(arrivedTime)
                .build();
    }

    /**
     * NewsletterPreviousPolicy
     */
    public static NewsletterPreviousPolicy createNewsletterPreviousPolicy(
            Long newsletterId,
            NewsletterPreviousStrategy strategy,
            int recentCount,
            int fixedCount,
            int exposureRatio
    ) {
        return NewsletterPreviousPolicy.builder()
                .newsletterId(newsletterId)
                .strategy(strategy)
                .recentCount(recentCount)
                .fixedCount(fixedCount)
                .exposureRatio(exposureRatio)
                .build();
    }

    public static NewsletterPreviousPolicy createNewsletterPreviousPolicy(
            Long newsletterId,
            NewsletterPreviousStrategy strategy,
            int lastestCount,
            int fixedCount
    ) {
        return createNewsletterPreviousPolicy(newsletterId, strategy, lastestCount, fixedCount, 100);
    }

    /**
     * PreviousArticle
     */
    public static me.bombom.api.v1.article.domain.PreviousArticle createPreviousArticle(
            String title,
            Long newsletterId,
            LocalDateTime arrivedTime
    ) {
        return me.bombom.api.v1.article.domain.PreviousArticle.builder()
                .title(title)
                .contents("<h1>고정 아티클 내용</h1>")
                .contentsSummary("고정 아티클 요약")
                .expectedReadTime(5)
                .newsletterId(newsletterId)
                .arrivedDateTime(arrivedTime)
                .isFixed(true)  // 직접 생성된 고정 아티클
                .build();
    }

    /**
     * ContinueReadingRealtime
     */
    public static ContinueReadingRealtime continueReadingFixture(Member member) {
        return ContinueReadingRealtime.builder()
                .memberId(member.getId())
                .dayCount(10)
                .build();
    }

    /**
     * TodayReading
     */
    public static TodayReading todayReadingFixture(Member member) {
        return TodayReading.builder()
                .memberId(member.getId())
                .currentCount(1)
                .totalCount(3)
                .readCount(1)
                .build();
    }

    public static TodayReading todayReadingFixtureZeroCurrentCount(Member member) {
        return TodayReading.builder()
                .memberId(member.getId())
                .currentCount(0)
                .totalCount(3)
                .readCount(0)
                .build();
    }

    /**
     * WeeklyReading
     */
    public static WeeklyReading weeklyReadingFixture(Member member) {
        return WeeklyReading.builder()
                .memberId(member.getId())
                .currentCount(3)
                .goalCount(5)
                .build();
    }

    /**
     * MonthlyReadingRealtime
     */
    public static MonthlyReadingRealtime monthlyReadingRealtimeFixture(Member member, int currentCount) {
        return MonthlyReadingRealtime.builder()
                .memberId(member.getId())
                .currentCount(currentCount)
                .build();
    }

    /**
     * MonthlyReadingSnapshot
     */
    public static MonthlyReadingSnapshot monthlyReadingFixture(Member member) {
        return MonthlyReadingSnapshot.builder()
                .memberId(member.getId())
                .currentCount(10)
                .build();
    }

    public static MonthlyReadingSnapshot monthlyReadingSnapshot(Member member, int currentCount) {
        return MonthlyReadingSnapshot.builder()
                .memberId(member.getId())
                .currentCount(currentCount)
                .build();
    }

    public static MonthlyReadingSnapshot monthlyReadingSnapshotWithRank(
            Member member,
            int currentCount,
            Integer rankOrder,
            Integer nextRankDifference
    ) {
        return MonthlyReadingSnapshot.builder()
                .memberId(member.getId())
                .currentCount(currentCount)
                .rankOrder(rankOrder)
                .nextRankDifference(nextRankDifference)
                .build();
    }

    /**
     * Highlight
     */
    public static List<Highlight> createHighlightFixtures(List<Article> articles) {
        Article firstArticle = articles.get(0);
        Article secondArticle = articles.get(1);
        Article thirdArticle = articles.get(2);
        return List.of(
                Highlight.builder()
                        .highlightLocation(new HighlightLocation(0, "div[0]/p[0]", 10, "div[0]/p[0]"))
                        .memberId(firstArticle.getMemberId())
                        .newsletterId(firstArticle.getNewsletterId())
                        .articleId(firstArticle.getId())
                        .title(firstArticle.getTitle())
                        .color(Color.from("#ffeb3b"))
                        .text("첫 번째 하이라이트")
                        .memo("메모")
                        .build(),
                Highlight.builder()
                        .highlightLocation(new HighlightLocation(15, "div[0]/p[1]", 25, "div[0]/p[1]"))
                        .memberId(firstArticle.getMemberId())
                        .newsletterId(firstArticle.getNewsletterId())
                        .articleId(firstArticle.getId())
                        .title(firstArticle.getTitle())
                        .color(Color.from("#4caf50"))
                        .text("두 번째 하이라이트")
                        .memo("메모")
                        .build(),
                Highlight.builder()
                        .highlightLocation(new HighlightLocation(5, "div[0]/h1", 15, "div[0]/h1"))
                        .memberId(secondArticle.getMemberId())
                        .newsletterId(secondArticle.getNewsletterId())
                        .articleId(secondArticle.getId())
                        .title(secondArticle.getTitle())
                        .color(Color.from("#2196f3"))
                        .text("세 번째 하이라이트")
                        .memo("메모")
                        .build(),
                Highlight.builder()
                        .highlightLocation(new HighlightLocation(5, "div[0]/h1", 15, "div[0]/h1"))
                        .memberId(thirdArticle.getMemberId())
                        .newsletterId(thirdArticle.getNewsletterId())
                        .articleId(thirdArticle.getId())
                        .title(thirdArticle.getTitle())
                        .color(Color.from("#0016fb"))
                        .text("네 번째 하이라이트")
                        .memo("메모")
                        .build(),
                Highlight.builder()
                        .highlightLocation(new HighlightLocation(5, "div[0]/h1", 15, "div[0]/h1"))
                        .memberId(thirdArticle.getMemberId())
                        .newsletterId(thirdArticle.getNewsletterId())
                        .articleId(thirdArticle.getId())
                        .title(thirdArticle.getTitle())
                        .color(Color.from("#21b6f3"))
                        .text("디섯 번째 하이라이트")
                        .memo("메모")
                        .build(),
                Highlight.builder()
                        .highlightLocation(new HighlightLocation(5, "div[0]/h1", 15, "div[0]/h1"))
                        .memberId(thirdArticle.getMemberId())
                        .newsletterId(thirdArticle.getNewsletterId())
                        .articleId(thirdArticle.getId())
                        .title(thirdArticle.getTitle())
                        .color(Color.from("#b196f2"))
                        .text("여섯 번째 하이라이트")
                        .memo("메모")
                        .build()
        );
    }

    public static HighlightCreateRequest createHighlightRequest(Long articleId) {
        return new HighlightCreateRequest(
                new HighlightLocationRequest(0, "div[0]/p[2]", 20, "div[0]/p[2]"),
                articleId,
                Color.from("#f44336"),
                "새로운 하이라이트 텍스트",
                "메모"
        );
    }

    /**
     * Stage
     */
    public static Stage createStage(int level, int totalScore) {
        return Stage.builder()
                .level(level)
                .requiredScore(totalScore)
                .build();
    }

    public static List<Stage> createStages() {
        return List.of(
                createStage(1, 50),
                createStage(2, 100),
                createStage(3, 160),
                createStage(4, 215),
                createStage(5, 330)
        );
    }

    /**
     * Pet
     */
    public static Pet createPet(Member member, Long stageId) {
        return Pet.builder()
                .memberId(member.getId())
                .stageId(stageId)
                .currentScore(0)
                .build();
    }

    public static Pet createPetWithScore(Member member, Long stageId, int currentScore) {
        return Pet.builder()
                .memberId(member.getId())
                .stageId(stageId)
                .currentScore(currentScore)
                .build();
    }

    /*
     * Notice
     */
    public static Notice createNotice(String title, NoticeCategory noticeCategory) {
        return Notice.builder()
                .title(title)
                .content("content")
                .noticeCategory(noticeCategory)
                .build();
    }

    /**
     * Challenge
     */
    public static Challenge createChallenge(
            String name,
            LocalDate startDate,
            LocalDate endDate,
            int totalDays,
            Long newsletterGroupId
    ) {
        return Challenge.builder()
                .name(name)
                .generation(1)
                .startDate(startDate)
                .endDate(endDate)
                .totalDays(totalDays)
                .newsletterGroupId(newsletterGroupId)
                .build();
    }

    public static Challenge createChallenge(
            String name,
            int generation,
            LocalDate startDate,
            LocalDate endDate,
            Long newsletterGroupId
    ) {
        int totalDays = (int) java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
        return Challenge.builder()
                .name(name)
                .generation(generation)
                .startDate(startDate)
                .endDate(endDate)
                .totalDays(totalDays)
                .newsletterGroupId(newsletterGroupId)
                .build();
    }

    public static ChallengeParticipant createChallengeParticipant(Long challengeId, Long memberId, int completedDays) {
        return ChallengeParticipant.builder()
                .challengeId(challengeId)
                .memberId(memberId)
                .completedDays(completedDays)
                .isSurvived(true)
                .shield(0)
                .streak(0)
                .build();
    }

    public static ChallengeParticipant createChallengeParticipantWithStreak(Long challengeId, Long memberId, int streak) {
        return ChallengeParticipant.builder()
                .challengeId(challengeId)
                .memberId(memberId)
                .completedDays(streak)
                .isSurvived(true)
                .shield(0)
                .streak(streak)
                .build();
    }

    public static ChallengeTodo createChallengeTodo(Long challengeId, ChallengeTodoType todoType) {
        return ChallengeTodo.builder()
                .challengeId(challengeId)
                .todoType(todoType)
                .build();
    }

    public static ChallengeDailyTodo createChallengeDailyTodo(Long participantId, LocalDate todoDate,
                                                              Long challengeTodoId) {
        return ChallengeDailyTodo.builder()
                .participantId(participantId)
                .todoDate(todoDate)
                .challengeTodoId(challengeTodoId)
                .build();
    }

    /**
     * ChallengeParticipant
     */
    public static ChallengeParticipant createChallengeParticipant(
            Long challengeId,
            Long memberId,
            int completedDays,
            boolean isSurvived
    ) {
        return ChallengeParticipant.builder()
                .challengeId(challengeId)
                .memberId(memberId)
                .completedDays(completedDays)
                .isSurvived(isSurvived)
                .shield(0)
                .build();
    }

    public static ChallengeParticipant createChallengeParticipantWithTeam(
            Long challengeId,
            Long memberId,
            Long challengeTeamId,
            int completedDays,
            int shield
    ) {
        return ChallengeParticipant.builder()
                .challengeId(challengeId)
                .memberId(memberId)
                .challengeTeamId(challengeTeamId)
                .completedDays(completedDays)
                .shield(shield)
                .isSurvived(true)
                .build();
    }

    public static ChallengeParticipant createChallengeParticipantWithTeam(
            Long challengeId,
            Long memberId,
            Long challengeTeamId,
            int completedDays,
            int shield,
            boolean isSurvived
    ) {
        return ChallengeParticipant.builder()
                .challengeId(challengeId)
                .memberId(memberId)
                .challengeTeamId(challengeTeamId)
                .completedDays(completedDays)
                .shield(shield)
                .isSurvived(isSurvived)
                .build();
    }

    /**
     * ChallengeDailyResult
     */
    public static ChallengeDailyResult createChallengeDailyResult(
            Long participantId,
            LocalDate date,
            ChallengeDailyStatus status
    ) {
        return ChallengeDailyResult.builder()
                .participantId(participantId)
                .date(date)
                .status(status)
                .build();
    }

    /**
     * ChallengeTeam
     */
    public static ChallengeTeam createChallengeTeam(Long challengeId, int progress) {
        return ChallengeTeam.builder()
                .challengeId(challengeId)
                .progress(progress)
                .build();
    }

    /**
     * NewsletterGroup
     */
    public static NewsletterGroup createNewsletterGroup(String name) {
        return NewsletterGroup.builder()
                .name(name)
                .build();
    }

    /**
     * NewsletterGroupItem
     */
    public static NewsletterGroupItem createNewsletterGroupItem(
            Long newsletterGroupId,
            Long newsletterId
    ) {
        return NewsletterGroupItem.builder()
                .newsletterGroupId(newsletterGroupId)
                .newsletterId(newsletterId)
                .build();
    }

    /**
     * ChallengeComment
     */
    public static ChallengeComment createChallengeComment(
            Long newsletterId,
            Long participantId,
            String articleTitle,
            String quotation,
            String comment
    ) {
        return ChallengeComment.builder()
                .newsletterId(newsletterId)
                .participantId(participantId)
                .articleTitle(articleTitle)
                .quotation(quotation)
                .comment(comment)
                .build();
    }

    /**
     * ChallengeDailyGuide
     */
    public static ChallengeDailyGuide createChallengeDailyGuide(
            Long challengeId,
            int dayIndex,
            DailyGuideType type,
            String imageUrl,
            String notice,
            boolean commentEnabled
    ) {
        return ChallengeDailyGuide.builder()
                .challengeId(challengeId)
                .dayIndex(dayIndex)
                .type(type)
                .imageUrl(imageUrl)
                .notice(notice)
                .commentEnabled(commentEnabled)
                .build();
    }

    /**
     * ChallengeDailyGuideComment
     */
    public static ChallengeDailyGuideComment createChallengeDailyGuideComment(
            Long guideId,
            Long participantId,
            String content
    ) {
        return ChallengeDailyGuideComment.builder()
                .guideId(guideId)
                .participantId(participantId)
                .content(content)
                .build();
    }

    /**
     * ChallengeCommentReply
     */
    public static ChallengeCommentReply createChallengeCommentReply(
            Long commentId,
            Long participantId,
            String reply,
            boolean isPrivate
    ) {
        return ChallengeCommentReply.builder()
                .commentId(commentId)
                .participantId(participantId)
                .reply(reply)
                .isPrivate(isPrivate)
                .build();
    }

    /**
     * BlogCategory
     */
    public static BlogCategory createBlogCategory(String name) {
        return BlogCategory.builder()
                .name(name)
                .build();
    }

    /**
     * BlogPost
     */
    public static BlogPost createBlogPost(
            Long memberId,
            String title,
            String content,
            Long thumbnailImageId,
            Long categoryId,
            BlogPostStatus status,
            BlogPostVisibility visibility,
            LocalDateTime publishedAt
    ) {
        return BlogPost.builder()
                .memberId(memberId)
                .title(title)
                .content(content)
                .description(title + " 설명")
                .thumbnailImageId(thumbnailImageId)
                .status(status)
                .visibility(visibility)
                .categoryId(categoryId)
                .publishedAt(publishedAt)
                .build();
    }

    /**
     * BlogImageAsset
     */
    public static BlogImageAsset createBlogImageAsset(Long blogPostId, String objectKey, String imageUrl) {
        return BlogImageAsset.builder()
                .blogPostId(blogPostId)
                .objectKey(objectKey)
                .imageUrl(imageUrl)
                .status(BlogImageAssetStatus.ATTACHED)
                .build();
    }

    /**
     * BlogHashtag
     */
    public static BlogHashtag createBlogHashtag(String name) {
        return BlogHashtag.builder()
                .name(name)
                .build();
    }

    /**
     * BlogPostTag
     */
    public static BlogPostTag createBlogPostTag(Long blogPostId, Long blogHashtagId) {
        return BlogPostTag.builder()
                .blogPostId(blogPostId)
                .blogHashtagId(blogHashtagId)
                .build();
    }
}
