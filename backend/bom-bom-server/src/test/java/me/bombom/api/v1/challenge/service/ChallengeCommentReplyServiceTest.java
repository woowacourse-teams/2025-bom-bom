package me.bombom.api.v1.challenge.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDate;
import java.util.List;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.challenge.domain.Challenge;
import me.bombom.api.v1.challenge.domain.ChallengeComment;
import me.bombom.api.v1.challenge.domain.ChallengeCommentReply;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import me.bombom.api.v1.challenge.dto.request.CreateCommentReplyRequest;
import me.bombom.api.v1.challenge.dto.response.CommentReplyResponse;
import me.bombom.api.v1.challenge.repository.ChallengeCommentReplyRepository;
import me.bombom.api.v1.challenge.repository.ChallengeCommentRepository;
import me.bombom.api.v1.challenge.repository.ChallengeParticipantRepository;
import me.bombom.api.v1.challenge.repository.ChallengeRepository;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.newsletter.domain.NewsletterGroup;
import me.bombom.api.v1.newsletter.repository.NewsletterGroupRepository;
import me.bombom.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@IntegrationTest
class ChallengeCommentReplyServiceTest {

    @Autowired
    private ChallengeCommentReplyService challengeCommentReplyService;

    @Autowired
    private ChallengeCommentReplyRepository challengeCommentReplyRepository;

    @Autowired
    private ChallengeCommentRepository challengeCommentRepository;

    @Autowired
    private ChallengeParticipantRepository challengeParticipantRepository;

    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private NewsletterGroupRepository newsletterGroupRepository;

    private Member commentAuthorMember;
    private Member replyMember;
    private Challenge challenge;
    private ChallengeParticipant commentAuthorParticipant;
    private ChallengeComment challengeComment;

    @BeforeEach
    void setUp() {
        challengeCommentReplyRepository.deleteAllInBatch();
        challengeCommentRepository.deleteAllInBatch();
        challengeParticipantRepository.deleteAllInBatch();
        challengeRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
        newsletterGroupRepository.deleteAllInBatch();

        commentAuthorMember = memberRepository.save(
                TestFixture.createUniqueMember("commentAuthor", java.util.UUID.randomUUID().toString()));
        replyMember = memberRepository.save(
                TestFixture.createUniqueMember("replyAuthor", java.util.UUID.randomUUID().toString()));

        NewsletterGroup group = TestFixture.createNewsletterGroup("그룹");
        newsletterGroupRepository.save(group);
        challenge = challengeRepository.save(TestFixture.createChallenge(
                "reply-challenge",
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(5),
                7,
                group.getId()));

        commentAuthorParticipant = challengeParticipantRepository.save(
                TestFixture.createChallengeParticipant(
                        challenge.getId(),
                        commentAuthorMember.getId(),
                        0));

        challengeComment = challengeCommentRepository.save(
                TestFixture.createChallengeComment(
                        1L,
                        commentAuthorParticipant.getId(),
                        "article title",
                        "quote",
                        "comment"));
    }

    @Test
    void 코멘트에_답글을_작성한다() {
        // given
        ChallengeParticipant replyParticipant = challengeParticipantRepository.save(
                TestFixture.createChallengeParticipant(
                        challenge.getId(),
                        replyMember.getId(),
                        0));

        CreateCommentReplyRequest request = new CreateCommentReplyRequest("감사합니다!");

        //when
        challengeCommentReplyService.createCommentReply(
                challenge.getId(),
                challengeComment.getId(),
                replyMember.getId(),
                request
        );

        // then
        List<ChallengeCommentReply> replies = challengeCommentReplyRepository.findAll();
        assertSoftly(softly -> {
            softly.assertThat(replies).hasSize(1);
            softly.assertThat(replies.get(0).getCommentId()).isEqualTo(challengeComment.getId());
            softly.assertThat(replies.get(0).getParticipantId()).isEqualTo(replyParticipant.getId());
            softly.assertThat(replies.get(0).getReply()).isEqualTo("감사합니다!");
        });
    }

    @Test
    void 존재하지_않는_코멘트에_답글을_작성하면_예외가_발생한다() {
        // given
        Long notExistCommentId = 999L;
        CreateCommentReplyRequest request = new CreateCommentReplyRequest("reply");

        // when & then
        assertThatThrownBy(
                () -> challengeCommentReplyService.createCommentReply(
                        challenge.getId(),
                        notExistCommentId,
                        replyMember.getId(),
                        request))
                .isInstanceOf(CIllegalArgumentException.class);
    }

    @Test
    void 챌린지_참여자가_아닌_회원은_답글을_작성할_수_없다() {
        // given
        CreateCommentReplyRequest request = new CreateCommentReplyRequest("reply");

        // when & then
        assertThatThrownBy(() -> challengeCommentReplyService.createCommentReply(
                challenge.getId(),
                challengeComment.getId(),
                replyMember.getId(),
                request))
                .isInstanceOf(CIllegalArgumentException.class);
    }

    @Test
    void 코멘트에_달린_답글을_조회한다() {
        // given
        ChallengeParticipant replyParticipant = challengeParticipantRepository.save(
                TestFixture.createChallengeParticipant(
                        challenge.getId(),
                        replyMember.getId(),
                        0));

        challengeCommentReplyRepository.save(
                ChallengeCommentReply.builder()
                        .commentId(challengeComment.getId())
                        .participantId(replyParticipant.getId())
                        .reply("첫번째 답글")
                        .build());

        // when
        Page<CommentReplyResponse> page = challengeCommentReplyService.getCommentReplies(
                replyMember.getId(),
                challenge.getId(),
                challengeComment.getId(),
                PageRequest.of(0, 10));

        // then
        assertSoftly(softly -> {
            softly.assertThat(page.getTotalElements()).isEqualTo(1);
            softly.assertThat(page.getContent().get(0).reply()).isEqualTo("첫번째 답글");
            softly.assertThat(page.getContent().get(0).isMyReply()).isTrue();
        });
    }

    @Test
    void 챌린지_참여자가_아니면_답글을_조회할_수_없다() {
        // given
        Member outsider = memberRepository.save(
                TestFixture.createUniqueMember("챌린지 참여 안한 사람", java.util.UUID.randomUUID().toString()));

        // when & then
        assertThatThrownBy(() -> challengeCommentReplyService.getCommentReplies(
                outsider.getId(),
                challenge.getId(),
                challengeComment.getId(),
                PageRequest.of(0, 10)))
                .isInstanceOf(CIllegalArgumentException.class);
    }
}
