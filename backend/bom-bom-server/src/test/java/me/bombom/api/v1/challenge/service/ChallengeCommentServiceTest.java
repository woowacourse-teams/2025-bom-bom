package me.bombom.api.v1.challenge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.article.domain.Article;
import me.bombom.api.v1.article.repository.ArticleRepository;
import me.bombom.api.v1.challenge.domain.ChallengeComment;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import me.bombom.api.v1.challenge.dto.request.ChallengeCommentOptionsRequest;
import me.bombom.api.v1.challenge.dto.request.ChallengeCommentRequest;
import me.bombom.api.v1.challenge.dto.response.ChallengeCommentCandidateArticleResponse;
import me.bombom.api.v1.challenge.dto.response.ChallengeCommentResponse;
import me.bombom.api.v1.challenge.repository.ChallengeCommentRepository;
import me.bombom.api.v1.challenge.repository.ChallengeParticipantRepository;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.newsletter.domain.Category;
import me.bombom.api.v1.newsletter.domain.Newsletter;
import me.bombom.api.v1.newsletter.domain.NewsletterDetail;
import me.bombom.api.v1.newsletter.repository.CategoryRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterDetailRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterRepository;
import me.bombom.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@IntegrationTest
class ChallengeCommentServiceTest {

    @Autowired
    private ChallengeCommentService challengeCommentService;

    @Autowired
    private ChallengeCommentRepository challengeCommentRepository;

    @Autowired
    private ChallengeParticipantRepository challengeParticipantRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private NewsletterRepository newsletterRepository;

    @Autowired
    private NewsletterDetailRepository newsletterDetailRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Member member;
    private Article article;
    private List<Article> articles;
    private List<Newsletter> newsletters;
    private ChallengeParticipant participant;

    @BeforeEach
    void setUp() {
        challengeCommentRepository.deleteAllInBatch();
        challengeParticipantRepository.deleteAllInBatch();
        articleRepository.deleteAllInBatch();
        newsletterRepository.deleteAllInBatch();
        newsletterDetailRepository.deleteAllInBatch();
        categoryRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();

        member = TestFixture.normalMemberFixture();
        memberRepository.save(member);

        List<Category> categories = TestFixture.createCategories();
        categoryRepository.saveAll(categories);

        List<NewsletterDetail> details = TestFixture.createNewsletterDetails();
        newsletterDetailRepository.saveAll(details);

        newsletters = newsletterRepository.saveAll(TestFixture.createNewslettersWithDetails(categories, details));

        articles = articleRepository.saveAll(TestFixture.createArticles(member, newsletters));
        article = articles.get(0);

        participant = challengeParticipantRepository.save(
                TestFixture.createChallengeParticipantWithTeam(
                        1L,
                        member.getId(),
                        10L,
                        0,
                        0
                )
        );
    }

    @Test
    void 같은_팀_댓글만_기간_내_조회_성공() {
        // given
        LocalDate start = LocalDate.now().minusDays(1);
        LocalDate end = LocalDate.now().plusDays(1);

        Member otherMember = TestFixture.createMemberFixture("other@bombom.news", "other");
        memberRepository.save(otherMember);

        ChallengeParticipant otherTeamParticipant = challengeParticipantRepository.save(
                TestFixture.createChallengeParticipantWithTeam(
                        2L,
                        otherMember.getId(),
                        20L,
                        0,
                        0
                )
        );

        ChallengeComment otherTeamComment = challengeCommentRepository.save(
                TestFixture.createChallengeComment(
                        article.getNewsletterId(),
                        participant.getId(),
                        article.getTitle(),
                        "quote",
                        "우리 팀 댓글"
                )
        );

        challengeCommentRepository.save(
                TestFixture.createChallengeComment(
                        article.getNewsletterId(),
                        otherTeamParticipant.getId(),
                        article.getTitle(),
                        "quote2",
                        "다른 팀 댓글"
                )
        );

        // when
        Page<ChallengeCommentResponse> result = challengeCommentService.getChallengeComments(
                1L,
                member.getId(),
                new ChallengeCommentOptionsRequest(start, end),
                PageRequest.of(0, 10)
        );

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).comment()).isEqualTo(otherTeamComment.getComment());
    }

    @Test
    void 요청_기간에_댓글이_없으면_빈_페이지를_반환한다() {
        // given
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = LocalDate.now().plusDays(2);

        // when
        Page<ChallengeCommentResponse> result = challengeCommentService.getChallengeComments(
                1L,
                member.getId(),
                new ChallengeCommentOptionsRequest(start, end),
                PageRequest.of(0, 10)
        );

        // then
        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void 챌린지_참가자가_아니면_예외_발생() {
        // given
        LocalDate start = LocalDate.now().minusDays(1);
        LocalDate end = LocalDate.now().plusDays(1);

        // when & then
        assertThatThrownBy(() -> challengeCommentService.getChallengeComments(
                99L,
                member.getId(),
                new ChallengeCommentOptionsRequest(start, end),
                PageRequest.of(0, 10)
        )).isInstanceOf(CIllegalArgumentException.class);
    }

    @Test
    void 지정한_날짜의_읽은_내_아티클만_챌린지_코멘트_후보로_조회한다() {
        // given
        Member otherMember = memberRepository.save(
                TestFixture.createMemberFixture("other@bombom.news", "other")
        );

        Article otherArticle = articleRepository.save(
                TestFixture.createArticle(
                        "아티클 제목",
                        otherMember.getId(),
                        newsletters.getFirst().getId(),
                        LocalDateTime.now()
                )
        );

        articles.get(0).markAsRead();
        articles.get(1).markAsRead();
        articleRepository.saveAll(articles);

        otherArticle.markAsRead();
        articleRepository.save(otherArticle);

        LocalDate targetDate = articles.get(0).getArrivedDateTime().toLocalDate();

        // when
        List<ChallengeCommentCandidateArticleResponse> result =
                challengeCommentService.getChallengeCommentCandidateArticles(
                        member.getId(),
                        targetDate
                );

        // then
        List<Long> resultArticleIds = result.stream()
                .map(ChallengeCommentCandidateArticleResponse::articleId)
                .toList();

        assertSoftly(softly -> {
            softly.assertThat(resultArticleIds).containsExactly(
                    articles.get(0).getId(),
                    articles.get(1).getId()
            );
            softly.assertThat(resultArticleIds).doesNotContain(otherArticle.getId());
            softly.assertThat(resultArticleIds).doesNotContain(articles.get(2).getId());
        });
    }

    @Test
    void 챌린지_댓글을_생성한다() {
        // given
        ChallengeCommentRequest request = new ChallengeCommentRequest(
                article.getId(),
                null, // 인용구 optional 테스트
                "챌린지 한 줄 코멘트로 20자 이상의 댓글을 작성했습니다."
        );

        // when
        challengeCommentService.createChallengeComment(
                member.getId(),
                participant.getChallengeId(),
                request
        );

        // then
        List<ChallengeComment> comments = challengeCommentRepository.findAll();
        assertSoftly(softly -> {
            softly.assertThat(comments).hasSize(1);
            softly.assertThat(comments.getFirst().getParticipantId()).isEqualTo(participant.getId());
            softly.assertThat(comments.getFirst().getArticleTitle()).isEqualTo(article.getTitle());
            softly.assertThat(comments.getFirst().getQuotation()).isNull();
            softly.assertThat(comments.getFirst().getComment()).isEqualTo(request.comment());
        });
    }

    @Test
    void 챌린지_참가자가_없으면_댓글_생성시_예외가_발생한다() {
        // given
        ChallengeCommentRequest request = new ChallengeCommentRequest(
                article.getId(),
                "quote",
                "챌린지 한 줄 코멘트로 20자 이상의 댓글을 작성했습니다."
        );

        // when & then
        assertThatThrownBy(() -> challengeCommentService.createChallengeComment(
                member.getId(),
                999L,
                request
        )).isInstanceOf(CIllegalArgumentException.class);
    }
}
