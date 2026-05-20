package me.bombom.api.v1.challenge.controller.mock;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import me.bombom.api.v1.challenge.dto.response.ChallengeReviewResponse;
import org.springframework.stereotype.Component;

// TODO: Service 계층 도입 시 본 빈 삭제 — 실제 Repository/Service 로 대체
@Component
public class ChallengeReviewMockStore {

    private final CopyOnWriteArrayList<ChallengeReviewResponse> reviews = new CopyOnWriteArrayList<>();
    private final AtomicLong idSequence = new AtomicLong(0);

    public ChallengeReviewMockStore() {
//        seed("나밍곰", "내가 쓴 비공개 리뷰입니다.", true);
//        seed("제나", "정말 유익한 챌린지였어요!", false);
//        seed("밍곰", "다음에도 또 참여하고 싶어요.", false);
    }

    public List<ChallengeReviewResponse> findAll() {
        return List.copyOf(reviews);
    }

    public ChallengeReviewResponse save(String nickname, String comment, boolean isPrivate) {
        ChallengeReviewResponse saved = new ChallengeReviewResponse(
                idSequence.incrementAndGet(),
                nickname,
                comment,
                isPrivate
        );
        reviews.add(saved);
        return saved;
    }

    // TODO: Service 계층 도입 시 challengeId + memberId 복합 조건 조회로 대체
    public Optional<ChallengeReviewResponse> findByNickname(String nickname) {
        return reviews.stream()
                .filter(review -> review.nickname().equals(nickname))
                .findFirst();
    }

    public boolean updateById(Long reviewId, String comment, boolean isPrivate) {
        for (int i = 0; i < reviews.size(); i++) {
            ChallengeReviewResponse current = reviews.get(i);
            if (current.reviewId().equals(reviewId)) {
                reviews.set(i, new ChallengeReviewResponse(
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

    private void seed(String nickname, String comment, boolean isPrivate) {
        save(nickname, comment, isPrivate);
    }
}
