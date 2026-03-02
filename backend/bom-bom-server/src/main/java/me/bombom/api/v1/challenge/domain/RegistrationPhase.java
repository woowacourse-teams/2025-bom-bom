package me.bombom.api.v1.challenge.domain;

public enum RegistrationPhase {

    EARLY,
    LATE,
    CLOSED,
    ;

    public boolean isRecruiting() {
        return this == EARLY || this == LATE;
    }
}
