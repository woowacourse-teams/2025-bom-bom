package me.bombom.api.v1.challenge.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ChallengeReviewTest {

    private static final Long OWNER_MEMBER_ID = 1L;
    private static final Long OTHER_MEMBER_ID = 2L;
    private static final Long CHALLENGE_ID = 100L;

    private ChallengeReview review() {
        return ChallengeReview.builder()
                .challengeId(CHALLENGE_ID)
                .memberId(OWNER_MEMBER_ID)
                .comment("좋았어요")
                .isPrivate(false)
                .build();
    }

    @Test
    void isOwnedBy_같은_memberId_이면_true_반환() {
        assertThat(review().isOwnedBy(OWNER_MEMBER_ID)).isTrue();
    }

    @Test
    void isOwnedBy_다른_memberId_이면_false_반환() {
        assertThat(review().isOwnedBy(OTHER_MEMBER_ID)).isFalse();
    }

    @Test
    void update_는_comment_와_isPrivate_을_새_값으로_변경한다() {
        // given
        ChallengeReview review = review();

        // when
        review.update("수정된 코멘트", true);

        // then
        assertThat(review.getComment()).isEqualTo("수정된 코멘트");
        assertThat(review.isPrivate()).isTrue();
    }
}
