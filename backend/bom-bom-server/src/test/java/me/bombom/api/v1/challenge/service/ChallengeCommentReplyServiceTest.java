package me.bombom.api.v1.challenge.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.challenge.domain.Challenge;
import me.bombom.api.v1.challenge.domain.ChallengeComment;
import me.bombom.api.v1.challenge.domain.ChallengeCommentReply;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import me.bombom.api.v1.challenge.domain.ChallengeTeam;
import me.bombom.api.v1.challenge.dto.request.CreateCommentReplyRequest;
import me.bombom.api.v1.challenge.dto.response.CommentReplyResponse;
import me.bombom.api.v1.challenge.repository.ChallengeCommentReplyRepository;
import me.bombom.api.v1.challenge.repository.ChallengeCommentRepository;
import me.bombom.api.v1.challenge.repository.ChallengeParticipantRepository;
import me.bombom.api.v1.challenge.repository.ChallengeRepository;
import me.bombom.api.v1.challenge.repository.ChallengeTeamRepository;
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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

@IntegrationTest
class ChallengeCommentReplyServiceTest {

    private static final String COMMENT = "챌린지 댓글 대댓글 서비스 테스트를 위한 충분한 길이의 댓글입니다.";

    @Autowired
    private ChallengeCommentReplyService challengeCommentReplyService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private ChallengeTeamRepository challengeTeamRepository;

    @Autowired
    private ChallengeParticipantRepository challengeParticipantRepository;

    @Autowired
    private ChallengeCommentRepository challengeCommentRepository;

    @Autowired
    private ChallengeCommentReplyRepository challengeCommentReplyRepository;

    @Autowired
    private NewsletterGroupRepository newsletterGroupRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private NewsletterDetailRepository newsletterDetailRepository;

    @Autowired
    private NewsletterRepository newsletterRepository;

    @Test
    void 대댓글을_생성하면_댓글의_답글_수가_증가한다() {
        // given
        ReplyFixture fixture = saveReplyFixture();

        // when
        challengeCommentReplyService.createCommentReply(
                fixture.challenge().getId(),
                fixture.comment().getId(),
                fixture.replyMember().getId(),
                new CreateCommentReplyRequest("서비스에서 작성한 대댓글입니다.", false)
        );

        // then
        List<ChallengeCommentReply> replies = challengeCommentReplyRepository.findAll();
        ChallengeComment updatedComment = challengeCommentRepository.findById(fixture.comment().getId()).orElseThrow();
        assertSoftly(softly -> {
            softly.assertThat(replies).hasSize(1);
            softly.assertThat(replies.getFirst().getCommentId()).isEqualTo(fixture.comment().getId());
            softly.assertThat(replies.getFirst().getParticipantId()).isEqualTo(fixture.replyParticipant().getId());
            softly.assertThat(replies.getFirst().getReply()).isEqualTo("서비스에서 작성한 대댓글입니다.");
            softly.assertThat(updatedComment.getReplyCount()).isEqualTo(1);
        });
    }

    @Test
    void 존재하지_않는_댓글에는_대댓글을_작성할_수_없다() {
        // when & then
        assertThatThrownBy(() -> challengeCommentReplyService.createCommentReply(
                1L,
                -1L,
                1L,
                new CreateCommentReplyRequest("댓글이 없어서 실패하는 대댓글입니다.", false)
        ))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.ENTITY_NOT_FOUND);
    }

    @Test
    void 참가자가_아니면_대댓글을_작성할_수_없다() {
        // given
        ReplyFixture fixture = saveReplyFixture();
        Member outsider = saveMember("out");

        // when & then
        assertThatThrownBy(() -> challengeCommentReplyService.createCommentReply(
                fixture.challenge().getId(),
                fixture.comment().getId(),
                outsider.getId(),
                new CreateCommentReplyRequest("참가자가 아니어서 실패하는 대댓글입니다.", false)
        ))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.FORBIDDEN_RESOURCE);
    }

    @Test
    void 참가자가_아니면_대댓글을_조회할_수_없다() {
        // given
        ReplyFixture fixture = saveReplyFixture();
        Member outsider = saveMember("out");

        // when & then
        assertThatThrownBy(() -> challengeCommentReplyService.getCommentReplies(
                outsider.getId(),
                fixture.challenge().getId(),
                fixture.comment().getId(),
                PageRequest.of(0, 10)
        ))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.FORBIDDEN_RESOURCE);
    }

    @Test
    void 대댓글_목록은_비공개_노출_정책을_적용한다() {
        // given
        ReplyFixture fixture = saveReplyFixture();
        ChallengeParticipant viewer = saveParticipant(fixture.challenge(), fixture.team(), saveMember("vw"));
        challengeCommentReplyRepository.save(TestFixture.createChallengeCommentReply(
                fixture.comment().getId(),
                fixture.replyParticipant().getId(),
                "공개 대댓글",
                false
        ));
        challengeCommentReplyRepository.save(TestFixture.createChallengeCommentReply(
                fixture.comment().getId(),
                fixture.replyParticipant().getId(),
                "비공개 대댓글",
                true
        ));

        // when
        List<CommentReplyResponse> result = challengeCommentReplyService.getCommentReplies(
                viewer.getMemberId(),
                fixture.challenge().getId(),
                fixture.comment().getId(),
                PageRequest.of(0, 10)
        ).getContent();

        // then
        assertSoftly(softly -> {
            softly.assertThat(result).extracting(CommentReplyResponse::reply)
                    .containsExactly("공개 대댓글");
            softly.assertThat(result).extracting(CommentReplyResponse::isPrivate)
                    .containsOnly(false);
        });
    }

    private ReplyFixture saveReplyFixture() {
        Member commentMember = saveMember("cm");
        Member replyMember = saveMember("rp");
        Challenge challenge = saveChallenge();
        ChallengeTeam team = challengeTeamRepository.save(TestFixture.createChallengeTeam(challenge.getId(), 0));
        ChallengeParticipant commentParticipant = saveParticipant(challenge, team, commentMember);
        ChallengeParticipant replyParticipant = saveParticipant(challenge, team, replyMember);
        Newsletter newsletter = saveNewsletter();
        ChallengeComment comment = challengeCommentRepository.save(TestFixture.createChallengeComment(
                newsletter.getId(),
                commentParticipant.getId(),
                "대댓글 대상 아티클",
                "인용문",
                COMMENT
        ));

        return new ReplyFixture(
                replyMember,
                challenge,
                team,
                replyParticipant,
                comment
        );
    }

    private Challenge saveChallenge() {
        NewsletterGroup group = newsletterGroupRepository.save(TestFixture.createNewsletterGroup("대댓글 챌린지 그룹"));
        return challengeRepository.save(TestFixture.createChallenge(
                "대댓글 챌린지",
                LocalDate.of(2026, 1, 9).minusDays(1),
                LocalDate.of(2026, 1, 9).plusDays(9),
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
                "대댓글 뉴스레터",
                token + "@reply.test",
                category.getId(),
                detail.getId()
        ));
    }

    private record ReplyFixture(
            Member replyMember,
            Challenge challenge,
            ChallengeTeam team,
            ChallengeParticipant replyParticipant,
            ChallengeComment comment
    ) {
    }
}
