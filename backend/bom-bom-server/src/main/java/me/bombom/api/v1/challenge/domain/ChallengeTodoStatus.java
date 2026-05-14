package me.bombom.api.v1.challenge.domain;

public enum ChallengeTodoStatus {

    COMPLETE,
    INCOMPLETE,
    ;

    public static ChallengeTodoStatus getStatus(boolean isDone) {
        return isDone ? COMPLETE : INCOMPLETE;
    }
}
