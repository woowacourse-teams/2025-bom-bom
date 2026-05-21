package me.bombom.api.v1.challenge.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.challenge.domain.Challenge;
import me.bombom.api.v1.challenge.domain.ChallengeDailyResult;
import me.bombom.api.v1.challenge.domain.ChallengeDailyStatus;
import me.bombom.api.v1.challenge.domain.ChallengeGrade;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import me.bombom.api.v1.challenge.domain.ChallengeTeam;
import me.bombom.api.v1.challenge.domain.ChallengeTodoType;
import me.bombom.api.v1.challenge.dto.ChallengeProgressFlat;
import me.bombom.api.v1.challenge.dto.TeamChallengeProgressFlat;
import me.bombom.api.v1.challenge.dto.response.CertificationInfoResponse;
import me.bombom.api.v1.challenge.dto.response.ChallengeStreakResponse;
import me.bombom.api.v1.challenge.dto.response.MemberChallengeProgressResponse;
import me.bombom.api.v1.challenge.dto.response.TeamChallengeProgressResponse;
import me.bombom.api.v1.challenge.repository.ChallengeDailyResultRepository;
import me.bombom.api.v1.challenge.repository.ChallengeParticipantRepository;
import me.bombom.api.v1.challenge.repository.ChallengeRepository;
import me.bombom.api.v1.challenge.repository.ChallengeTeamRepository;
import me.bombom.api.v1.common.holiday.repository.HolidayRepository;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.CServerErrorException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.common.exception.UnauthorizedException;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengeProgressService {

    private final ChallengeRepository challengeRepository;
    private final ChallengeParticipantRepository challengeParticipantRepository;
    private final ChallengeDailyResultRepository challengeDailyResultRepository;
    private final ChallengeTeamRepository challengeTeamRepository;
    private final MemberRepository memberRepository;
    private final HolidayRepository holidayRepository;
    private final Clock clock;

    @Transactional
    public void proceedDailySurvivalCheck(Challenge challenge, LocalDate yesterday) {
        List<ChallengeParticipant> absentees = challengeParticipantRepository.findAbsentees(challenge.getId(), yesterday);
        boolean isHoliday = holidayRepository.existsByDate(yesterday);

        for (ChallengeParticipant absentee : absentees) {
            if (isHoliday) {
                absentee.applyHolidayShield();
                saveDailyResult(absentee, yesterday, ChallengeDailyStatus.HOLIDAY_SHIELD);
                continue;
            }
            if (absentee.useShieldIfAvailable()) {
                saveDailyResult(absentee, yesterday, ChallengeDailyStatus.SHIELD);
                continue;
            }
            absentee.resetStreak();
            checkFailure(absentee, challenge, yesterday);
        }
    }

    public MemberChallengeProgressResponse getMemberProgress(Long id, Member member) {
        Challenge challenge = getChallenge(id);
        validateParticipation(id, member);

        List<ChallengeProgressFlat> progressList = getDailyProgress(challenge, member);
        validateMemberProgressDataIntegrity(id, member, progressList);

        return MemberChallengeProgressResponse.of(member, progressList);
    }

    public ChallengeStreakResponse getMemberStreak(Long challengeId, Member member, int limit) {
        validateParticipation(challengeId, member);
        ChallengeParticipant participant = getChallengeParticipant(challengeId, member.getId());

        int streak = participant.getStreak();
        LocalDate today = LocalDate.now(clock);
        boolean completedToday = challengeDailyResultRepository.existsByParticipantIdAndDate(participant.getId(), today);

        if (completedToday) {
            if (streak == 0) {
                return ChallengeStreakResponse.empty();
            }
            List<ChallengeDailyResult> recentDaysResult = challengeDailyResultRepository.findByParticipantIdOrderByDateDesc(
                    participant.getId(),
                    PageRequest.of(0, limit)
            );
            return ChallengeStreakResponse.of(streak, recentDaysResult);
        }

        int fetchCount = streak > 0 ? limit - 1 : 0;
        List<ChallengeDailyResult> recentDays = fetchCount > 0
                ? challengeDailyResultRepository.findByParticipantIdOrderByDateDesc(participant.getId(), PageRequest.of(0, fetchCount))
                : List.of();
        return ChallengeStreakResponse.withTodayAbsent(streak, recentDays, today);
    }

    public TeamChallengeProgressResponse getTeamProgressByTeamId(Long challengeId, Long teamId, Member member) {
        Challenge challenge = getChallenge(challengeId);

        validateParticipation(challengeId, member);
        validateTeamBelongsToChallenge(challengeId, teamId);

        List<TeamChallengeProgressFlat> progressList = challengeParticipantRepository.findTeamProgress(teamId);

        return TeamChallengeProgressResponse.of(challenge, progressList);
    }

    public CertificationInfoResponse getCertificationInfo(Long challengeId, Long memberId) {
        Challenge challenge = getChallenge(challengeId);
        validateChallengeEnded(challenge);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new UnauthorizedException(ErrorDetail.INVALID_TOKEN)
                        .addContext(ErrorContextKeys.OPERATION, "getCertificationInfo")
                        .addContext(ErrorContextKeys.MEMBER_ID, memberId)
                        .addContext(ErrorContextKeys.DETAIL, "유효하지 않은 인증 정보입니다."));

        ChallengeParticipant challengeParticipant = getChallengeParticipant(challengeId, memberId);
        if (!challengeParticipant.isSurvived()) {
            throw new CIllegalArgumentException(ErrorDetail.PRECONDITION_FAILED)
                    .addContext(ErrorContextKeys.OPERATION, "getCertificationInfo")
                    .addContext(ErrorContextKeys.MEMBER_ID, memberId)
                    .addContext(ErrorContextKeys.DETAIL, "탈락한 참가자는 수료증을 발급받을 수 없습니다.");
        }

        int progress = challengeParticipant.calculateProgress(challenge.getTotalDays());
        ChallengeGrade challengeGrade = ChallengeGrade.calculate(progress, challengeParticipant.isSurvived());
        return CertificationInfoResponse.of(member, challenge, challengeGrade);
    }

    private void saveDailyResult(ChallengeParticipant participant, LocalDate date, ChallengeDailyStatus status) {
        ChallengeDailyResult result = ChallengeDailyResult.builder()
                .participantId(participant.getId())
                .date(date)
                .status(status)
                .build();
        challengeDailyResultRepository.save(result);
    }

    private void checkFailure(ChallengeParticipant participant, Challenge challenge, LocalDate yesterday) {
        int maxAllowedAbsent = challenge.calculateMaxAllowedAbsences();
        int passedWeekDays = challenge.calculatePassedWeekDays(yesterday);
        int currentAbsent = passedWeekDays - participant.getCompletedDays();
        if (currentAbsent > maxAllowedAbsent) {
            participant.markAsFailed();
        }
    }

    private Challenge getChallenge(Long challengeId) {
        return challengeRepository.findById(challengeId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "challenge")
                        .addContext(ErrorContextKeys.OPERATION, "getChallenge"));
    }

    private ChallengeParticipant getChallengeParticipant(Long challengeId, Long memberId) {
        return challengeParticipantRepository.findByChallengeIdAndMemberId(challengeId, memberId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "challengeParticipant")
                        .addContext(ErrorContextKeys.OPERATION, "getChallengeParticipant"));
    }

    private void validateParticipation(Long id, Member member) {
        boolean isParticipant = challengeParticipantRepository.existsByChallengeIdAndMemberId(id, member.getId());
        if (!isParticipant) {
            throw new UnauthorizedException(ErrorDetail.FORBIDDEN_RESOURCE)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "challengeParticipant")
                    .addContext(ErrorContextKeys.CHALLENGE_ID, id)
                    .addContext(ErrorContextKeys.MEMBER_ID, member.getId());
        }
    }

    private List<ChallengeProgressFlat> getDailyProgress(Challenge challenge, Member member) {
        List<ChallengeProgressFlat> progressList = challengeParticipantRepository.findMemberProgress(
                challenge.getId(),
                member.getId(),
                LocalDate.now(clock)
        );

        if (isFirstDay(challenge)) {
            return progressList.stream()
                    .filter(progress -> progress.todoType() == ChallengeTodoType.MINDSET)
                    .toList();
        }
        return progressList.stream()
                .filter(progress -> progress.todoType() != ChallengeTodoType.MINDSET)
                .toList();
    }

    private boolean isFirstDay(Challenge challenge) {
        return challenge.getStartDate().isEqual(LocalDate.now(clock));
    }

    private void validateMemberProgressDataIntegrity(Long id, Member member, List<ChallengeProgressFlat> progressList) {
        if (progressList.isEmpty()) {
            throw new CServerErrorException(ErrorDetail.INTERNAL_SERVER_ERROR)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "challengeParticipant")
                    .addContext(ErrorContextKeys.OPERATION, "getMemberProgress")
                    .addContext(ErrorContextKeys.DETAIL, "참가자 정보는 존재하나 일일 진행 상황 데이터가 조회되지 않습니다")
                    .addContext(ErrorContextKeys.CHALLENGE_ID, id)
                    .addContext(ErrorContextKeys.MEMBER_ID, member.getId());
        }
    }

    private void validateTeamBelongsToChallenge(Long challengeId, Long teamId) {
        ChallengeTeam team = challengeTeamRepository.findById(teamId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "challengeTeam")
                        .addContext(ErrorContextKeys.OPERATION, "validateTeamBelongsToChallenge")
                        .addContext(ErrorContextKeys.DETAIL, "팀을 찾을 수 없습니다: " + teamId));

        if (!team.getChallengeId().equals(challengeId)) {
            throw new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "challengeTeam")
                    .addContext(ErrorContextKeys.OPERATION, "validateTeamBelongsToChallenge")
                    .addContext(ErrorContextKeys.CHALLENGE_ID, challengeId)
                    .addContext(ErrorContextKeys.DETAIL, "해당 팀은 이 챌린지에 속하지 않습니다: " + teamId);
        }
    }

    private void validateChallengeEnded(Challenge challenge) {
        if (!challenge.isEnded(LocalDate.now(clock))) {
            throw new CIllegalArgumentException(ErrorDetail.PRECONDITION_FAILED)
                    .addContext(ErrorContextKeys.OPERATION, "getCertificationInfo")
                    .addContext(ErrorContextKeys.DETAIL, "진행 중인 챌린지는 수료증을 조회할 수 없습니다");
        }
    }
}
