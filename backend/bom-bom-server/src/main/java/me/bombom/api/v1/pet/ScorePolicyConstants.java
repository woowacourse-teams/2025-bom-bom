package me.bombom.api.v1.pet;

public final class ScorePolicyConstants {

    /**
     * 정책 별 할당 점수
     */
    public static final int ATTENDANCE_SCORE = 5;
    public static final int ARTICLE_READING_SCORE = 10;
    public static final int CONTINUE_READING_BONUS_SCORE = 5;

    /**
     * 읽기 점수 부여 기준
     */
    public static final int MIN_CONTINUE_READING_COUNT = 7;
    public static final int MAX_TODAY_READING_COUNT = 3;

    private ScorePolicyConstants() {}
}
