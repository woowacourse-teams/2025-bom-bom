package me.bombom.api.v1.challenge.domain;

import java.time.LocalDate;

public enum ChallengeFilter {

    SUMMARY {
        @Override
        public boolean isVisible(Challenge challenge, LocalDate today, boolean isJoined) {
            ChallengeStatus status = challenge.getStatus(today);
            RegistrationPhase phase = challenge.getRegistrationPhase(today);
            return (status == ChallengeStatus.ONGOING && isJoined)
                    || phase.isRecruiting();
        }
    },
    DEFAULT {
        @Override
        public boolean isVisible(Challenge challenge, LocalDate today, boolean isJoined) {
            ChallengeStatus status = challenge.getStatus(today);
            return status == ChallengeStatus.COMING_SOON
                    || status == ChallengeStatus.BEFORE_START
                    || isJoined
                    || challenge.isLatePhase(today);
        }
    };

    public abstract boolean isVisible(Challenge challenge, LocalDate today, boolean isJoined);
}
