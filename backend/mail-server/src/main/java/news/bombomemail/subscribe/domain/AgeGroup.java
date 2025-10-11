package news.bombomemail.subscribe.domain;

import lombok.Getter;

@Getter
public enum AgeGroup {
    AGE0S("age0s"),
    AGE10S("age10s"),
    AGE20S("age20s"),
    AGE30S("age30s"),
    AGE40S("age40s"),
    AGE50S("age50s"),
    AGE60PLUS("age60plus"),
    ;

    private final String dbKey;

    AgeGroup(String dbKey) {
        this.dbKey = dbKey;
    }

    public static AgeGroup fromBirthYear(int currentYear, int birthYear) {
        int age = currentYear - birthYear;
        int decade = Math.max(0, Math.min(6, age / 10)); // 0~60+ clamp
        return switch (decade) {
            case 0 -> AGE0S;
            case 1 -> AGE10S;
            case 2 -> AGE20S;
            case 3 -> AGE30S;
            case 4 -> AGE40S;
            case 5 -> AGE50S;
            default -> AGE60PLUS;
        };
    }
}
