package me.bombom.api.v1.challenge.domain;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import me.bombom.api.v1.challenge.dto.OngoingChallengeParticipantFlat;
import me.bombom.api.v1.common.exception.CServerErrorException;
import me.bombom.api.v1.common.exception.ErrorDetail;

/**
 * 진행 중 챌린지 1건의 마이페이지 요약 계산 결과
 */
public record OngoingChallengeSummary(
        Long challengeId,
        String title,
        LocalDate startDate,
        LocalDate endDate,
        int remainingDays,
        int progressRate,
        Rank myTeamRank,
        Rank teamRank,
        AttendanceComparison myAttendanceComparison,
        AttendanceComparison teamAttendanceComparison
) {

    public record Rank(int rank, int total) {
    }

    public record AttendanceComparison(int attendanceRate, int differencePoint) {
    }

    public static OngoingChallengeSummary of(
            List<OngoingChallengeParticipantFlat> participants,
            Long memberId,
            LocalDate today
    ) {
        OngoingChallengeParticipantFlat me = getMine(participants, memberId);

        double myAttendance = attendance(me);

        Map<Long, List<OngoingChallengeParticipantFlat>> byTeam = participants.stream()
                .collect(Collectors.groupingBy(OngoingChallengeParticipantFlat::challengeTeamId));
        Map<Long, Double> teamAverageById = byTeam.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> averageAttendance(entry.getValue())));

        double myTeamAverage = teamAverageById.get(me.challengeTeamId());
        double overallAverage = averageAttendance(participants);
        double overallTeamAverage = teamAverageById.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        Rank myTeamRank = new Rank(
                competitionRank(myAttendance, attendances(byTeam.get(me.challengeTeamId()))),
                byTeam.get(me.challengeTeamId()).size()
        );
        Rank teamRank = new Rank(
                competitionRank(myTeamAverage, List.copyOf(teamAverageById.values())),
                teamAverageById.size()
        );

        return new OngoingChallengeSummary(
                me.challengeId(),
                me.title(),
                me.startDate(),
                me.endDate(),
                (int) ChronoUnit.DAYS.between(today, me.endDate()),
                round(myAttendance),
                myTeamRank,
                teamRank,
                new AttendanceComparison(round(myAttendance), round(myAttendance - overallAverage)),
                new AttendanceComparison(round(myTeamAverage), round(myTeamAverage - overallTeamAverage))
        );
    }

    private static OngoingChallengeParticipantFlat getMine(
            List<OngoingChallengeParticipantFlat> participants,
            Long memberId
    ) {
        return participants.stream()
                .filter(participant -> participant.memberId().equals(memberId))
                .findFirst()
                .orElseThrow(() -> new CServerErrorException(ErrorDetail.INTERNAL_SERVER_ERROR)
                        .addContext("memberId", memberId)
                );
    }

    private static double attendance(OngoingChallengeParticipantFlat participant) {
        if (participant.totalDays() <= 0) {
            return 0.0;
        }
        return (double) participant.completedDays() / participant.totalDays() * 100;
    }

    private static List<Double> attendances(List<OngoingChallengeParticipantFlat> participants) {
        return participants.stream()
                .map(OngoingChallengeSummary::attendance)
                .toList();
    }

    private static double averageAttendance(List<OngoingChallengeParticipantFlat> participants) {
        return participants.stream()
                .mapToDouble(OngoingChallengeSummary::attendance)
                .average()
                .orElse(0.0);
    }

    private static int competitionRank(double myScore, List<Double> scores) {
        long higherCount = scores.stream()
                .filter(score -> score > myScore)
                .count();
        return (int) higherCount + 1;
    }

    private static int round(double value) {
        return (int) Math.round(value);
    }
}
