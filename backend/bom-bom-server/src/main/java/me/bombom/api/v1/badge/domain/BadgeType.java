package me.bombom.api.v1.badge.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BadgeType {

    RANKING_GOLD("월간 읽기 랭킹 금메달", BadgeCategory.RANKING),
    RANKING_SILVER("월간 읽기 랭킹 은메달", BadgeCategory.RANKING),
    RANKING_BRONZE("월간 읽기 랭킹 동메달", BadgeCategory.RANKING),

    CHALLENGE_GOLD("챌린지 완주 금메달", BadgeCategory.CHALLENGE),
    CHALLENGE_SILVER("챌린지 완주 은메달", BadgeCategory.CHALLENGE),
    CHALLENGE_BRONZE("챌린지 완주 동메달", BadgeCategory.CHALLENGE),
    ;

    private final String name;
    private final BadgeCategory category;
}
