package me.bombom.api.v1.challenge.dto;

/**
 * 본인의 종료 챌린지 참여 단건(참여당 1행).
 * 메달 등급({@code ChallengeGrade}) 분류에 사용
 */
public record MemberMedalParticipationFlat(

        Long memberId,
        int attendedDays,
        int totalDays,
        boolean isSurvived
) {
}
