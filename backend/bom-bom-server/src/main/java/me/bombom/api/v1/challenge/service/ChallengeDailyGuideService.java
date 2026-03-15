package me.bombom.api.v1.challenge.service;

import static java.time.temporal.ChronoUnit.DAYS;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.challenge.domain.Challenge;
import me.bombom.api.v1.challenge.domain.ChallengeDailyGuide;
import me.bombom.api.v1.challenge.domain.ChallengeDailyGuideComment;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import me.bombom.api.v1.challenge.domain.ChallengeTeam;
import me.bombom.api.v1.challenge.dto.DailyGuideCommentContext;
import me.bombom.api.v1.challenge.dto.request.DailyGuideCommentRequest;
import me.bombom.api.v1.challenge.dto.response.DailyGuideCommentResponse;
import me.bombom.api.v1.challenge.dto.response.MemberDailyCommentResponse;
import me.bombom.api.v1.challenge.dto.response.TodayDailyGuideResponse;
import me.bombom.api.v1.challenge.dto.response.TodayDailyGuideResponse.MyCommentResponse;
import me.bombom.api.v1.challenge.repository.ChallengeDailyGuideCommentRepository;
import me.bombom.api.v1.challenge.repository.ChallengeDailyGuideRepository;
import me.bombom.api.v1.challenge.repository.ChallengeParticipantRepository;
import me.bombom.api.v1.challenge.repository.ChallengeRepository;
import me.bombom.api.v1.challenge.repository.TodayDailyGuideRow;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengeDailyGuideService {

    private static final int FIRST_DAY_INDEX = 1;
    private final Clock clock;

    private final ChallengeRepository challengeRepository;
    private final ChallengeDailyGuideRepository challengeDailyGuideRepository;
    private final ChallengeDailyGuideCommentRepository challengeDailyGuideCommentRepository;
    private final ChallengeParticipantRepository challengeParticipantRepository;
    private final ChallengeDailyTodoService challengeDailyTodoService;
    private final ChallengeTodoService challengeTodoService;
    private final ChallengeTeamService challengeTeamService;

    public TodayDailyGuideResponse getTodayDailyGuide(Long challengeId, Long memberId) {
        Challenge challenge = getChallenge(challengeId);

        validateChallengeParticipant(challengeId, memberId);

        LocalDate today = LocalDate.now(clock);
        if (today.isBefore(challenge.getStartDate()) || today.isAfter(challenge.getEndDate())) {
            throw new CIllegalArgumentException(ErrorDetail.INVALID_INPUT_VALUE)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "challenge")
                    .addContext(ErrorContextKeys.CHALLENGE_ID, challengeId)
                    .addContext("reason", "챌린지 기간이 아닙니다.");
        }

        int dayIndex = calculateDayIndex(challenge.getStartDate(), today);
        TodayDailyGuideRow row = challengeDailyGuideRepository.findTodayGuide(
                        challengeId, memberId, dayIndex)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "challengeDailyGuide")
                        .addContext(ErrorContextKeys.CHALLENGE_ID, challengeId)
                        .addContext("dayIndex", dayIndex));

        MyCommentResponse myComment = createMyCommentResponse(row);
        return new TodayDailyGuideResponse(
                row.getDayIndex(),
                row.getType(),
                row.getImageUrl(),
                row.getNotice(),
                row.isCommentEnabled(),
                myComment);
    }

    public Page<DailyGuideCommentResponse> getTotalComments(
            Long challengeId,
            int dayIndex,
            Long memberId,
            Pageable pageable
    ) {
        Challenge challenge = getChallenge(challengeId);

        validateChallengeParticipant(challengeId, memberId);

        if (dayIndex != 0 && (dayIndex < FIRST_DAY_INDEX || dayIndex > challenge.getTotalDays())) {
            throw new CIllegalArgumentException(ErrorDetail.INVALID_INPUT_VALUE)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "challengeDailyGuide")
                    .addContext(ErrorContextKeys.CHALLENGE_ID, challengeId)
                    .addContext("dayIndex", dayIndex)
                    .addContext("reason", "유효하지 않은 일차 인덱스입니다.");
        }

        ChallengeDailyGuide guide = getChallengeDailyGuide(challengeId, dayIndex);

        return challengeDailyGuideCommentRepository.findByGuideId(guide.getId(), pageable);
    }

    @Transactional
    public void createDailyGuideComment(
            Long challengeId,
            int dayIndex,
            Long memberId,
            DailyGuideCommentRequest request,
            LocalDate today
    ) {
        DailyGuideCommentContext context = loadCommentCreationContext(challengeId, dayIndex, memberId);
        validateCommentCreation(context);
        saveDailyGuideComment(context.participant(), context.guide(), request);
        handleFirstDay(context.participant(), memberId, dayIndex, today);
    }

    public MemberDailyCommentResponse getDailyGuideComment(Long challengeId, int dayIndex, Long memberId) {
        Challenge challenge = getChallenge(challengeId);
        validateChallengeParticipant(challengeId, memberId);
        validateDayIndex(challengeId, dayIndex, challenge);

        MemberDailyCommentResponse response = challengeDailyGuideCommentRepository.findMyComment(
                challengeId,
                dayIndex,
                memberId);
        return response != null ? response : new MemberDailyCommentResponse(null);
    }

    private Challenge getChallenge(Long challengeId) {
        return challengeRepository.findById(challengeId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "challenge")
                        .addContext(ErrorContextKeys.CHALLENGE_ID, challengeId));
    }

    private void validateChallengeParticipant(Long challengeId, Long memberId) {
        if (!challengeParticipantRepository.existsByChallengeIdAndMemberId(challengeId, memberId)) {
            throw new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "challengeParticipant")
                    .addContext(ErrorContextKeys.MEMBER_ID, memberId)
                    .addContext(ErrorContextKeys.CHALLENGE_ID, challengeId);
        }
    }

    private void validateDayIndex(Long challengeId, int dayIndex, Challenge challenge) {
        int maxAllowedDayIndex = calculateMaxAllowedDayIndex(challenge.getStartDate(), LocalDate.now(clock));
        if (dayIndex > maxAllowedDayIndex) {
            throw new CIllegalArgumentException(ErrorDetail.INVALID_INPUT_VALUE)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "challengeDailyGuide")
                    .addContext(ErrorContextKeys.CHALLENGE_ID, challengeId)
                    .addContext("dayIndex", dayIndex)
                    .addContext("maxAllowedDayIndex", maxAllowedDayIndex)
                    .addContext("reason", "유효하지 않은 일차 인덱스입니다.");
        }
    }

    private int calculateDayIndex(LocalDate startDate, LocalDate today) {
        if (isWeekend(today)) {
            return 0;
        }
        return (int) DAYS.between(startDate, today) + FIRST_DAY_INDEX;
    }

    private int calculateMaxAllowedDayIndex(LocalDate startDate, LocalDate today) {
        LocalDate targetDate = today;
        // 오늘이 주말이면 직전 금요일까지의 컨텐츠는 볼 수 있어야 함
        while (isWeekend(targetDate) && !targetDate.isBefore(startDate)) {
            targetDate = targetDate.minusDays(FIRST_DAY_INDEX);
        }
        return (int) DAYS.between(startDate, targetDate) + FIRST_DAY_INDEX;
    }

    private boolean isWeekend(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }

    private MyCommentResponse createMyCommentResponse(TodayDailyGuideRow row) {
        boolean exists = row.getMyCommentExists() == FIRST_DAY_INDEX;
        return new MyCommentResponse(
                exists,
                exists ? row.getMyCommentContent() : null,
                exists ? row.getMyCommentCreatedAt() : null);
    }

    private DailyGuideCommentContext loadCommentCreationContext(Long challengeId, int dayIndex, Long memberId) {
        Challenge challenge = getChallenge(challengeId);
        ChallengeParticipant participant = getChallengeParticipant(challengeId, memberId);
        validateDayIndex(challengeId, dayIndex, challenge);
        ChallengeDailyGuide guide = getChallengeDailyGuide(challengeId, dayIndex);
        return new DailyGuideCommentContext(challengeId, dayIndex, memberId, participant, guide);
    }

    private ChallengeParticipant getChallengeParticipant(Long challengeId, Long memberId) {
        return challengeParticipantRepository.findByChallengeIdAndMemberId(challengeId, memberId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "challengeParticipant")
                        .addContext(ErrorContextKeys.MEMBER_ID, memberId)
                        .addContext(ErrorContextKeys.CHALLENGE_ID, challengeId));
    }

    private ChallengeDailyGuide getChallengeDailyGuide(Long challengeId, int dayIndex) {
        return challengeDailyGuideRepository.findByChallengeIdAndDayIndex(challengeId, dayIndex)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "challengeDailyGuide")
                        .addContext(ErrorContextKeys.CHALLENGE_ID, challengeId)
                        .addContext("dayIndex", dayIndex));
    }

    private void validateCommentCreation(DailyGuideCommentContext context) {
        validateCommentEnabled(context.challengeId(), context.dayIndex(), context.guide());
        validateNoExistingComment(context.challengeId(), context.dayIndex(), context.guide(), context.participant());
    }

    private void validateCommentEnabled(Long challengeId, int dayIndex, ChallengeDailyGuide guide) {
        if (!guide.isCommentEnabled()) {
            throw new CIllegalArgumentException(ErrorDetail.INVALID_INPUT_VALUE)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "challengeDailyGuide")
                    .addContext(ErrorContextKeys.CHALLENGE_ID, challengeId)
                    .addContext("dayIndex", dayIndex)
                    .addContext("reason", "댓글 작성이 불가능한 가이드입니다.");
        }
    }

    private void validateNoExistingComment(
            Long challengeId,
            int dayIndex,
            ChallengeDailyGuide guide,
            ChallengeParticipant participant
    ) {
        ChallengeDailyGuideComment existingComment = challengeDailyGuideCommentRepository
                .findByGuideIdAndParticipantId(guide.getId(), participant.getId())
                .orElse(null);

        if (existingComment != null) {
            throw new CIllegalArgumentException(ErrorDetail.INVALID_INPUT_VALUE)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "challengeDailyGuideComment")
                    .addContext(ErrorContextKeys.CHALLENGE_ID, challengeId)
                    .addContext("dayIndex", dayIndex)
                    .addContext("reason", "이미 댓글이 존재합니다.");
        }
    }

    private void saveDailyGuideComment(
            ChallengeParticipant participant,
            ChallengeDailyGuide guide,
            DailyGuideCommentRequest request
    ) {
        ChallengeDailyGuideComment comment = ChallengeDailyGuideComment.builder()
                .guideId(guide.getId())
                .participantId(participant.getId())
                .content(request.content())
                .build();
        challengeDailyGuideCommentRepository.save(comment);
    }

    private void handleFirstDay(
            ChallengeParticipant participant,
            Long memberId,
            int dayIndex,
            LocalDate today
    ) {
        if (dayIndex != FIRST_DAY_INDEX) {
            return;
        }

        challengeTodoService.insertMindsetDone(participant, today);
        // READ 투두 자동 생성 (뉴스레터 1개 읽기)
        challengeDailyTodoService.updateChallengeDailyTodo(memberId, null, today);
        // COMMENT 투두 생성 (한 줄 코멘트 작성)
        challengeTodoService.insertCommentDone(participant, today);

        // 이미 완료되지 않았으면 progress 처리
        if (!challengeTodoService.isCompletedToday(participant.getId(), today)) {
            challengeTodoService.completeDailyTodo(participant, today);
            if (participant.getChallengeTeamId() != null) {
                ChallengeTeam challengeTeam = challengeTeamService.getByParticipant(participant);
                challengeTeamService.updateTeamProgress(challengeTeam);
            }
        }
    }
}
