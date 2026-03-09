package me.bombom.api.v1.challenge.domain;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;

public enum ChallengeFilter {

    SUMMARY {
        @Override
        public boolean isVisible(Challenge challenge, LocalDate today, boolean isJoined) {
            ChallengeStatus status = challenge.getStatus(today);
            RegistrationPhase phase = challenge.getRegistrationPhase(today);
            return (status == ChallengeStatus.ONGOING && isJoined)
                    || phase.isRecruiting();
        }

        @Override
        public Optional<Comparator<Challenge>> orderComparator(LocalDate today, Set<Long> joinedChallengeIds) {
            return Optional.of(
                    Comparator.comparingInt(challenge -> getSummaryOrder(challenge, today, joinedChallengeIds))
            );
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

        @Override
        public Optional<Comparator<Challenge>> orderComparator(LocalDate today, Set<Long> joinedChallengeIds) {
            return Optional.empty();
        }
    };

    public abstract boolean isVisible(Challenge challenge, LocalDate today, boolean isJoined);

    public abstract Optional<Comparator<Challenge>> orderComparator(LocalDate today, Set<Long> joinedChallengeIds);

    static int getSummaryOrder(Challenge challenge, LocalDate today, Set<Long> joinedChallengeIds) {
        ChallengeStatus status = challenge.getStatus(today);
        RegistrationPhase phase = challenge.getRegistrationPhase(today);
        boolean isJoined = joinedChallengeIds.contains(challenge.getId());

        if (status == ChallengeStatus.ONGOING && isJoined) {
            return 0;
        }
        if (phase == RegistrationPhase.LATE) {
            return 1;
        }
        if (phase == RegistrationPhase.EARLY) {
            return 2;
        }
        return 3;
    }
}
