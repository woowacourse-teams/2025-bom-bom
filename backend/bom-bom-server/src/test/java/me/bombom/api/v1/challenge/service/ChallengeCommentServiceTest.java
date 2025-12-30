package me.bombom.api.v1.challenge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.article.domain.Article;
import me.bombom.api.v1.article.repository.ArticleRepository;
import me.bombom.api.v1.challenge.domain.ChallengeComment;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import me.bombom.api.v1.challenge.dto.request.ChallengeCommentOptionsRequest;
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

        List<Newsletter> newsletters = TestFixture.createNewslettersWithDetails(categories, details);
        newsletterRepository.saveAll(newsletters);

        article = TestFixture.createArticles(member, newsletters).get(0);
        articleRepository.save(article);

        participant = challengeParticipantRepository.save(
                ChallengeParticipant.builder()
                        .challengeId(1L)
                        .memberId(member.getId())
                        .challengeTeamId(10L)
                        .completedDays(0)
                        .shield(0)
                        .build()
        );

        Member otherMember = TestFixture.createMemberFixture("other@bombom.news", "other");
        memberRepository.save(otherMember);

        ChallengeParticipant otherTeamParticipant = challengeParticipantRepository.save(
                ChallengeParticipant.builder()
                        .challengeId(2L)
                        .memberId(otherMember.getId())
                        .challengeTeamId(20L)
                        .completedDays(0)
                        .shield(0)
                        .build()
        );

        challengeCommentRepository.save(
                ChallengeComment.builder()
                        .articleId(article.getId())
                        .participantId(participant.getId())
                        .quotation("quote")
                        .comment("우리 팀 댓글")
                        .build()
        );
        challengeCommentRepository.save(
                ChallengeComment.builder()
                        .articleId(article.getId())
                        .participantId(otherTeamParticipant.getId())
                        .quotation("quote2")
                        .comment("다른 팀 댓글")
                        .build()
        );
    }

    @Test
    void 같은_팀_댓글만_기간_내_조회_성공() {
        // given
        LocalDate start = LocalDate.now().minusDays(1);
        LocalDate end = LocalDate.now().plusDays(1);

        // when
        Page<ChallengeCommentResponse> result = challengeCommentService.getChallengeComments(
                1L,
                member.getId(),
                new ChallengeCommentOptionsRequest(start, end),
                PageRequest.of(0, 10)
        );

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).comment()).isEqualTo("우리 팀 댓글");
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
}
