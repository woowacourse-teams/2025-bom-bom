package me.bombom.api.v1.challenge.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.article.domain.Article;
import me.bombom.api.v1.article.repository.ArticleRepository;
import me.bombom.api.v1.challenge.domain.Challenge;
import me.bombom.api.v1.challenge.domain.ChallengeComment;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import me.bombom.api.v1.challenge.domain.ChallengeTeam;
import me.bombom.api.v1.challenge.domain.ChallengeTodoType;
import me.bombom.api.v1.challenge.dto.request.ChallengeCommentOptionsRequest;
import me.bombom.api.v1.challenge.dto.request.ChallengeCommentRequest;
import me.bombom.api.v1.challenge.dto.request.UpdateChallengeCommentRequest;
import me.bombom.api.v1.challenge.dto.response.ChallengeCommentLikeResponse;
import me.bombom.api.v1.challenge.dto.response.ChallengeCommentResponse;
import me.bombom.api.v1.challenge.dto.response.CreateCommentResponse;
import me.bombom.api.v1.challenge.repository.ChallengeCommentLikeRepository;
import me.bombom.api.v1.challenge.repository.ChallengeCommentRepository;
import me.bombom.api.v1.challenge.repository.ChallengeParticipantRepository;
import me.bombom.api.v1.challenge.repository.ChallengeRepository;
import me.bombom.api.v1.challenge.repository.ChallengeTeamRepository;
import me.bombom.api.v1.challenge.repository.ChallengeTodoRepository;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.newsletter.domain.Category;
import me.bombom.api.v1.newsletter.domain.Newsletter;
import me.bombom.api.v1.newsletter.domain.NewsletterDetail;
import me.bombom.api.v1.newsletter.domain.NewsletterGroup;
import me.bombom.api.v1.newsletter.repository.CategoryRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterDetailRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterGroupRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterRepository;
import me.bombom.support.integration.IntegrationTest;
import me.bombom.support.time.MutableClock;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

@IntegrationTest
class ChallengeCommentServiceTest {

    private static final LocalDate WEEKDAY = LocalDate.of(2026, 1, 9);
    private static final LocalDate SATURDAY = LocalDate.of(2026, 1, 10);
    private static final String VALID_COMMENT = "챌린지 코멘트 서비스 테스트를 위한 충분한 길이의 댓글입니다.";

    @Autowired
    private ChallengeCommentService challengeCommentService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private ChallengeTeamRepository challengeTeamRepository;

    @Autowired
    private ChallengeParticipantRepository challengeParticipantRepository;

    @Autowired
    private ChallengeTodoRepository challengeTodoRepository;

    @Autowired
    private ChallengeCommentRepository challengeCommentRepository;

    @Autowired
    private ChallengeCommentLikeRepository challengeCommentLikeRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private NewsletterGroupRepository newsletterGroupRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private NewsletterDetailRepository newsletterDetailRepository;

    @Autowired
    private NewsletterRepository newsletterRepository;

    @Autowired
    private MutableClock clock;

    @Test
    void 챌린지_코멘트를_생성하면_아티클_정보로_댓글을_저장한다() {
        // given
        clock.setDate(WEEKDAY);
        CommentFixture fixture = saveCommentFixture();
        Article article = saveArticle(fixture.member(), fixture.newsletter());

        // when
        CreateCommentResponse response = challengeCommentService.createChallengeComment(
                fixture.member().getId(),
                fixture.challenge().getId(),
                new ChallengeCommentRequest(article.getId(), "기억할 문장", VALID_COMMENT)
        );

        // then
        List<ChallengeComment> comments = challengeCommentRepository.findAll();
        assertSoftly(softly -> {
            softly.assertThat(response.isFirstCompletion()).isTrue();
            softly.assertThat(comments).hasSize(1);
            softly.assertThat(comments.getFirst().getNewsletterId()).isEqualTo(fixture.newsletter().getId());
            softly.assertThat(comments.getFirst().getParticipantId()).isEqualTo(fixture.participant().getId());
            softly.assertThat(comments.getFirst().getArticleTitle()).isEqualTo(article.getTitle());
            softly.assertThat(comments.getFirst().getQuotation()).isEqualTo("기억할 문장");
            softly.assertThat(comments.getFirst().getComment()).isEqualTo(VALID_COMMENT);
        });
    }

    @Test
    void 주말에는_챌린지_코멘트를_생성할_수_없다() {
        // given
        clock.setDate(SATURDAY);

        // when & then
        assertThatThrownBy(() -> challengeCommentService.createChallengeComment(
                1L,
                1L,
                new ChallengeCommentRequest(1L, "인용", VALID_COMMENT)
        ))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.PRECONDITION_FAILED);
    }

    @Test
    void 참가자가_아니면_챌린지_댓글을_조회할_수_없다() {
        // given
        clock.setDate(WEEKDAY);
        Member member = saveMember("cm");
        Challenge challenge = saveChallenge();

        // when & then
        assertThatThrownBy(() -> challengeCommentService.getChallengeComments(
                challenge.getId(),
                member.getId(),
                new ChallengeCommentOptionsRequest(WEEKDAY, WEEKDAY),
                PageRequest.of(0, 10)
        ))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.FORBIDDEN_RESOURCE);
    }

    @Test
    void 탈퇴한_회원의_챌린지_댓글은_탈퇴한_사용자로_조회된다() {
        // given
        clock.setDate(WEEKDAY);
        CommentFixture fixture = saveCommentFixture();
        Member viewer = saveMember("vw");
        saveParticipant(fixture.challenge(), fixture.team(), viewer);
        ChallengeComment comment = saveComment(fixture.newsletter(), fixture.participant());
        LocalDate commentDate = comment.getCreatedAt().toLocalDate();

        memberRepository.delete(fixture.member());

        // when
        List<ChallengeCommentResponse> result = challengeCommentService.getChallengeComments(
                fixture.challenge().getId(),
                viewer.getId(),
                new ChallengeCommentOptionsRequest(commentDate, commentDate),
                PageRequest.of(0, 10)
        ).getContent();

        // then
        assertSoftly(softly -> {
            softly.assertThat(result).hasSize(1);
            softly.assertThat(result.getFirst().commentId()).isEqualTo(comment.getId());
            softly.assertThat(result.getFirst().nickname()).isEqualTo("탈퇴한 사용자");
            softly.assertThat(result.getFirst().comment()).isEqualTo(VALID_COMMENT);
        });
    }

    @Test
    void 없는_아티클로는_챌린지_코멘트를_생성할_수_없다() {
        // given
        clock.setDate(WEEKDAY);
        CommentFixture fixture = saveCommentFixture();

        // when & then
        assertThatThrownBy(() -> challengeCommentService.createChallengeComment(
                fixture.member().getId(),
                fixture.challenge().getId(),
                new ChallengeCommentRequest(-1L, "인용", VALID_COMMENT)
        ))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.ENTITY_NOT_FOUND);
    }

    @Test
    void 다른_참가자의_댓글은_수정할_수_없다() {
        // given
        clock.setDate(WEEKDAY);
        CommentFixture fixture = saveCommentFixture();
        ChallengeParticipant otherParticipant = saveParticipant(
                fixture.challenge(),
                fixture.team(),
                saveMember("ot")
        );
        ChallengeComment comment = saveComment(fixture.newsletter(), fixture.participant());

        // when & then
        assertThatThrownBy(() -> challengeCommentService.updateChallengeComment(
                otherParticipant.getMemberId(),
                fixture.challenge().getId(),
                comment.getId(),
                new UpdateChallengeCommentRequest("다른 참가자가 수정하려는 충분한 길이의 댓글입니다.")
        ))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.FORBIDDEN_RESOURCE);
    }

    @Test
    void 존재하지_않는_댓글에는_좋아요를_추가할_수_없다() {
        // given
        clock.setDate(WEEKDAY);
        CommentFixture fixture = saveCommentFixture();

        // when & then
        assertThatThrownBy(() -> challengeCommentService.addChallengeCommentLike(
                fixture.member().getId(),
                fixture.challenge().getId(),
                -1L
        ))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.ENTITY_NOT_FOUND);
    }

    @Test
    void 같은_참가자가_중복으로_좋아요를_눌러도_한번만_집계된다() {
        // given
        clock.setDate(WEEKDAY);
        CommentFixture fixture = saveCommentFixture();
        ChallengeComment comment = saveComment(fixture.newsletter(), fixture.participant());

        // when
        challengeCommentService.addChallengeCommentLike(
                fixture.member().getId(),
                fixture.challenge().getId(),
                comment.getId()
        );
        ChallengeCommentLikeResponse response = challengeCommentService.addChallengeCommentLike(
                fixture.member().getId(),
                fixture.challenge().getId(),
                comment.getId()
        );

        // then
        ChallengeComment updatedComment = challengeCommentRepository.findById(comment.getId()).orElseThrow();
        assertSoftly(softly -> {
            softly.assertThat(response.likeCount()).isEqualTo(1);
            softly.assertThat(updatedComment.getLikeCount()).isEqualTo(1);
            softly.assertThat(challengeCommentLikeRepository.count()).isEqualTo(1);
        });
    }

    @Test
    void 좋아요가_없으면_삭제해도_집계가_감소하지_않는다() {
        // given
        clock.setDate(WEEKDAY);
        CommentFixture fixture = saveCommentFixture();
        ChallengeComment comment = saveComment(fixture.newsletter(), fixture.participant());

        // when
        ChallengeCommentLikeResponse response = challengeCommentService.deleteChallengeCommentLike(
                fixture.member().getId(),
                fixture.challenge().getId(),
                comment.getId()
        );

        // then
        ChallengeComment updatedComment = challengeCommentRepository.findById(comment.getId()).orElseThrow();
        assertSoftly(softly -> {
            softly.assertThat(response.likeCount()).isZero();
            softly.assertThat(updatedComment.getLikeCount()).isZero();
            softly.assertThat(challengeCommentLikeRepository.count()).isZero();
        });
    }

    private CommentFixture saveCommentFixture() {
        Member member = saveMember("cm");
        Challenge challenge = saveChallenge();
        ChallengeTeam team = challengeTeamRepository.save(TestFixture.createChallengeTeam(challenge.getId(), 0));
        challengeTodoRepository.save(TestFixture.createChallengeTodo(challenge.getId(), ChallengeTodoType.COMMENT));
        ChallengeParticipant participant = saveParticipant(challenge, team, member);
        Newsletter newsletter = saveNewsletter();

        return new CommentFixture(member, challenge, team, participant, newsletter);
    }

    private Challenge saveChallenge() {
        NewsletterGroup group = newsletterGroupRepository.save(TestFixture.createNewsletterGroup("챌린지 그룹"));
        return challengeRepository.save(TestFixture.createChallenge(
                "댓글 챌린지",
                WEEKDAY.minusDays(1),
                WEEKDAY.plusDays(9),
                10,
                group.getId()
        ));
    }

    private ChallengeParticipant saveParticipant(Challenge challenge, ChallengeTeam team, Member member) {
        return challengeParticipantRepository.save(TestFixture.createChallengeParticipantWithTeam(
                challenge.getId(),
                member.getId(),
                team.getId(),
                0,
                0
        ));
    }

    private Member saveMember(String prefix) {
        String token = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return memberRepository.save(TestFixture.createUniqueMember(prefix + token, prefix + "-" + token));
    }

    private Newsletter saveNewsletter() {
        Category category = categoryRepository.save(TestFixture.createCategory());
        NewsletterDetail detail = newsletterDetailRepository.save(TestFixture.createNewsletterDetail(false));
        String token = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return newsletterRepository.save(TestFixture.createNewsletter(
                "댓글 뉴스레터",
                token + "@comment.test",
                category.getId(),
                detail.getId()
        ));
    }

    private Article saveArticle(Member member, Newsletter newsletter) {
        return articleRepository.save(TestFixture.createArticle(
                "챌린지 댓글 대상 아티클",
                member.getId(),
                newsletter.getId(),
                LocalDateTime.of(WEEKDAY, java.time.LocalTime.of(9, 0))
        ));
    }

    private ChallengeComment saveComment(Newsletter newsletter, ChallengeParticipant participant) {
        return challengeCommentRepository.save(TestFixture.createChallengeComment(
                newsletter.getId(),
                participant.getId(),
                "댓글이 달린 아티클",
                "인용문",
                VALID_COMMENT
        ));
    }

    private record CommentFixture(
            Member member,
            Challenge challenge,
            ChallengeTeam team,
            ChallengeParticipant participant,
            Newsletter newsletter
    ) {
    }
}
