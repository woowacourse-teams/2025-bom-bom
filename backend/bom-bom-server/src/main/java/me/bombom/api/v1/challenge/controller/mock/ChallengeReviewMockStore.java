package me.bombom.api.v1.challenge.controller.mock;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import me.bombom.api.v1.challenge.dto.response.ChallengeReviewResponse;
import me.bombom.api.v1.challenge.dto.response.MyChallengeReviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

// TODO: Service 계층 도입 시 본 빈 삭제 — 실제 Repository/Service 로 대체
// 본 Store는 viewer-independent 데이터(MockReview)만 보관하며,
// viewer-dependent 필드(isMyReview) 변환은 호출자(Controller) 책임.
@Component
public class ChallengeReviewMockStore {

    private final CopyOnWriteArrayList<MockReview> reviews = new CopyOnWriteArrayList<>();
    private final AtomicLong idSequence = new AtomicLong(0);

    public ChallengeReviewMockStore() {
//        seed("나밍곰", "내가 쓴 비공개 리뷰입니다.", true);
//        seed("제나", "정말 유익한 챌린지였어요!", false);
//        seed("밍곰", "다음에도 또 참여하고 싶어요.", false);
    }

    // 최신순(reviewId DESC) 페이징 — Mock 한계로 createdAt 대용
    // TODO: 실 구현 단계에서 createdAt DESC 로 전환
    public Page<MockReview> findAllAsPage(Pageable pageable) {
        List<MockReview> sorted = reviews.stream()
                .sorted(Comparator.comparing(MockReview::reviewId).reversed())
                .toList();

        int total = sorted.size();
        int from = (int) Math.min((long) pageable.getPageNumber() * pageable.getPageSize(), total);
        int to = (int) Math.min((long) from + pageable.getPageSize(), total);
        List<MockReview> slice = sorted.subList(from, to);

        return new PageImpl<>(slice, pageable, total);
    }

    public MockReview save(String nickname, String comment, boolean isPrivate) {
        MockReview saved = new MockReview(
                idSequence.incrementAndGet(),
                nickname,
                comment,
                isPrivate
        );
        reviews.add(saved);
        return saved;
    }

    // TODO: Service 계층 도입 시 challengeId + memberId 복합 조건 조회로 대체
    public Optional<MockReview> findByNickname(String nickname) {
        return reviews.stream()
                .filter(review -> review.nickname().equals(nickname))
                .findFirst();
    }

    public Optional<MockReview> findById(Long reviewId) {
        return reviews.stream()
                .filter(review -> review.reviewId().equals(reviewId))
                .findFirst();
    }

    public boolean updateById(Long reviewId, String comment, boolean isPrivate) {
        for (int i = 0; i < reviews.size(); i++) {
            MockReview current = reviews.get(i);
            if (current.reviewId().equals(reviewId)) {
                reviews.set(i, new MockReview(
                        current.reviewId(),
                        current.nickname(),
                        comment,
                        isPrivate
                ));
                return true;
            }
        }
        return false;
    }

    // viewer-dependent 변환 — isMyReview 는 호출자가 전달한 viewerNickname 기준으로 산출
    // TODO: 실 구현 단계에서 memberId 기준으로 정확화 (동명이인 오판 방지)
    public static ChallengeReviewResponse toResponse(MockReview review, String viewerNickname) {
        return new ChallengeReviewResponse(
                review.reviewId(),
                review.nickname(),
                review.comment(),
                review.isPrivate(),
                review.nickname().equals(viewerNickname)
        );
    }

    // 내 리뷰 단건용 — isMyReview 필드 없음 (항상 본인 리뷰이므로 의미 없음)
    public static MyChallengeReviewResponse toMyResponse(MockReview review) {
        return new MyChallengeReviewResponse(
                review.reviewId(),
                review.nickname(),
                review.comment(),
                review.isPrivate()
        );
    }

    private void seed(String nickname, String comment, boolean isPrivate) {
        save(nickname, comment, isPrivate);
    }

    // Mock 전용 viewer-independent 저장 표현 — 실 Service 도입 시 삭제
    public record MockReview(Long reviewId, String nickname, String comment, boolean isPrivate) {
    }
}