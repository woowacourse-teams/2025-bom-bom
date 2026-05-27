package me.bombom.api.v1.challenge.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.LocalDate;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.challenge.domain.Challenge;
import me.bombom.api.v1.challenge.domain.ChallengeReview;
import me.bombom.api.v1.challenge.dto.response.ChallengeReviewListItem;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@IntegrationTest
class ChallengeReviewRepositoryTest {

    @Autowired
    private ChallengeReviewRepository challengeReviewRepository;

    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private Clock clock;

    private Member viewer;
    private Member otherMember;
    private Long challengeAId;
    private Long challengeBId;

    @BeforeEach
    void setUp() {
        challengeReviewRepository.deleteAllInBatch();
        challengeRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();

        viewer = memberRepository.save(TestFixture.createUniqueMember("나밍곰", "viewer-provider"));
        otherMember = memberRepository.save(TestFixture.createUniqueMember("제나", "other-provider"));

        Challenge challengeA = challengeRepository.save(
                TestFixture.createChallenge("챌린지A", LocalDate.now(clock), LocalDate.now(clock).plusDays(10), 11, 1L)
        );
        Challenge challengeB = challengeRepository.save(
                TestFixture.createChallenge("챌린지B", LocalDate.now(clock), LocalDate.now(clock).plusDays(10), 11, 2L)
        );
        challengeAId = challengeA.getId();
        challengeBId = challengeB.getId();
    }

    @Test
    void 가시성_정책에_맞는_리뷰만_조회한다__본인의_리뷰와_타인의_공개_리뷰() {
        // given
        Member anotherMember = saveMember("익명", "another-provider");
        Member hiddenMember = saveMember("숨김", "hidden-provider");
        ChallengeReview mineHidden = save(challengeAId, viewer.getId(), "내 비공개", true);
        ChallengeReview otherPublic = save(challengeAId, otherMember.getId(), "타인 공개", false);
        ChallengeReview anotherPublic = save(challengeAId, anotherMember.getId(), "또 다른 타인 공개", false);
        save(challengeAId, hiddenMember.getId(), "타인 비공개", true);
        save(challengeBId, viewer.getId(), "다른 챌린지 본인 공개", false);

        // when
        Page<ChallengeReviewListItem> result = challengeReviewRepository.findVisibleReviews(
                challengeAId,
                viewer.getId(),
                PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        // then
        assertThat(result.getContent())
                .extracting(ChallengeReviewListItem::reviewId)
                .containsExactlyInAnyOrder(mineHidden.getId(), otherPublic.getId(), anotherPublic.getId());
    }

    @Test
    void 응답의_nickname은_조인된_Member의_nickname을_반영한다() {
        // given
        save(challengeAId, otherMember.getId(), "타인 공개", false);

        // when
        Page<ChallengeReviewListItem> result = challengeReviewRepository.findVisibleReviews(
                challengeAId,
                viewer.getId(),
                PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        // then
        assertThat(result.getContent())
                .extracting(ChallengeReviewListItem::nickname)
                .containsExactly(otherMember.getNickname());
    }

    private ChallengeReview save(Long challengeId, Long memberId, String comment, boolean isPrivate) {
        return challengeReviewRepository.save(
                ChallengeReview.builder()
                        .challengeId(challengeId)
                        .memberId(memberId)
                        .comment(comment)
                        .isPrivate(isPrivate)
                        .build()
        );
    }

    private Member saveMember(String nickname, String providerId) {
        return memberRepository.save(TestFixture.createUniqueMember(nickname, providerId));
    }

}
