package me.bombom.api.v1.challenge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

import java.util.List;
import me.bombom.api.v1.challenge.dto.response.ChallengeReviewListItem;
import me.bombom.api.v1.challenge.dto.response.ChallengeReviewResponse;
import me.bombom.api.v1.challenge.repository.ChallengeRepository;
import me.bombom.api.v1.challenge.repository.ChallengeReviewRepository;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ChallengeReviewServiceTest {

    private static final Long CHALLENGE_ID = 100L;
    private static final Long VIEWER_MEMBER_ID = 1L;
    private static final Long OTHER_MEMBER_ID = 2L;

    @Mock
    private ChallengeReviewRepository challengeReviewRepository;

    @Mock
    private ChallengeRepository challengeRepository;

    @InjectMocks
    private ChallengeReviewService challengeReviewService;

    @Test
    void 챌린지가_존재하지_않으면_404_예외를_던진다() {
        // given
        given(challengeRepository.existsById(CHALLENGE_ID)).willReturn(false);

        // when // then
        assertThatThrownBy(() -> challengeReviewService.getReviews(CHALLENGE_ID, VIEWER_MEMBER_ID, PageRequest.of(0, 20)))
                .isInstanceOf(CIllegalArgumentException.class);
    }

    @Test
    void 본인_리뷰는_isMyReview_가_true_타인_리뷰는_false_로_매핑된다() {
        // given
        given(challengeRepository.existsById(CHALLENGE_ID)).willReturn(true);
        givenReviews(List.of(
                item(10L, "나밍곰", VIEWER_MEMBER_ID),
                item(20L, "제나", OTHER_MEMBER_ID)
        ));

        // when
        Page<ChallengeReviewResponse> result = challengeReviewService.getReviews(
                CHALLENGE_ID, VIEWER_MEMBER_ID, PageRequest.of(0, 20)
        );

        // then
        assertThat(result.getContent())
                .extracting(ChallengeReviewResponse::isMyReview)
                .containsExactly(true, false);
        assertThat(result.getContent())
                .extracting(ChallengeReviewResponse::nickname)
                .containsExactly("나밍곰", "제나");
    }

    @Test
    void Member_의_nickname_이_null_이면_탈퇴한_사용자_로_표시된다() {
        // given
        given(challengeRepository.existsById(CHALLENGE_ID)).willReturn(true);
        givenReviews(List.of(item(30L, null, OTHER_MEMBER_ID)));

        // when
        Page<ChallengeReviewResponse> result = challengeReviewService.getReviews(
                CHALLENGE_ID, VIEWER_MEMBER_ID, PageRequest.of(0, 20)
        );

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).nickname()).isEqualTo("탈퇴한 사용자");
        assertThat(result.getContent().get(0).isMyReview()).isFalse();
    }

    private void givenReviews(List<ChallengeReviewListItem> items) {
        given(challengeReviewRepository.findVisibleReviews(anyLong(), anyLong(), any(Pageable.class)))
                .willReturn(new PageImpl<>(items, PageRequest.of(0, 20), items.size()));
    }

    private ChallengeReviewListItem item(Long reviewId, String nickname, Long memberId) {
        return new ChallengeReviewListItem(reviewId, nickname, "리뷰 코멘트", false, memberId);
    }
}
