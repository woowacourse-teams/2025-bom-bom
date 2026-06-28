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

    public OngoingChallengeSummary(
            OngoingChallengeParticipantFlat participation,
            int remainingDays,
            int progressRate,
            Rank myTeamRank,
            Rank teamRank,
            AttendanceComparison myAttendanceComparison,
            AttendanceComparison teamAttendanceComparison
    ) {
        this(
                participation.challengeId(),
                participation.title(),
                participation.startDate(),
                participation.endDate(),
                remainingDays,
                progressRate,
                myTeamRank,
                teamRank,
                myAttendanceComparison,
                teamAttendanceComparison
        );
    }

    public static OngoingChallengeSummary of(
            List<OngoingChallengeParticipantFlat> participants,
            Long memberId,
            LocalDate today
    ) {
        OngoingChallengeParticipantFlat me = getMine(participants, memberId);
        Map<Long, List<OngoingChallengeParticipantFlat>> byTeam = groupByTeam(participants);
        Map<Long, Double> teamAverageById = teamAverages(byTeam);

        double myAttendance = attendance(me);
        double myTeamAverage = teamAverageById.get(me.challengeTeamId());

        return new OngoingChallengeSummary(
                me,
                (int) ChronoUnit.DAYS.between(today, me.endDate()),
                round(myAttendance),
                myTeamRank(me, byTeam),
                teamRank(myTeamAverage, teamAverageById),
                comparison(myAttendance, averageAttendance(participants)),
                comparison(myTeamAverage, overallTeamAverage(teamAverageById))
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

    private static Map<Long, List<OngoingChallengeParticipantFlat>> groupByTeam(
            List<OngoingChallengeParticipantFlat> participants
    ) {
        return participants.stream()
                .collect(Collectors.groupingBy(OngoingChallengeParticipantFlat::challengeTeamId));
    }
    private static Map<Long, Double> teamAverages(Map<Long, List<OngoingChallengeParticipantFlat>> byTeam) {
        return byTeam.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> averageAttendance(entry.getValue())));
    }

    private static Rank myTeamRank(
            OngoingChallengeParticipantFlat me,
            Map<Long, List<OngoingChallengeParticipantFlat>> byTeam
    ) {
        List<OngoingChallengeParticipantFlat> myTeam = byTeam.get(me.challengeTeamId());
        return new Rank(competitionRank(attendance(me), attendances(myTeam)), myTeam.size());
    }

    private static Rank teamRank(double myTeamAverage, Map<Long, Double> teamAverageById) {
        return new Rank(
                competitionRank(myTeamAverage, List.copyOf(teamAverageById.values())),
                teamAverageById.size()
        );
    }

    private static double overallTeamAverage(Map<Long, Double> teamAverageById) {
        return teamAverageById.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }

    private static AttendanceComparison comparison(double value, double reference) {
        return new AttendanceComparison(round(value), round(value - reference));
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

    public record Rank(int rank, int total) {
    }

    public record AttendanceComparison(int attendanceRate, int differencePoint) {
    }
}
