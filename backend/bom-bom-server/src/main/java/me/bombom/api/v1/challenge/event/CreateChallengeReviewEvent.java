package me.bombom.api.v1.challenge.event;

import java.time.LocalDate;

public record CreateChallengeReviewEvent(Long participantId, LocalDate reviewDate) {
}
