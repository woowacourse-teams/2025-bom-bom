package me.bombom.api.v1.challenge.service;

import static java.time.temporal.ChronoUnit.DAYS;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.challenge.domain.Challenge;
import me.bombom.api.v1.challenge.domain.ChallengeDailyGuide;
import me.bombom.api.v1.challenge.domain.ChallengeDailyGuideComment;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import me.bombom.api.v1.challenge.dto.request.DailyGuideCommentRequest;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengeDailyGuideService {

    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

    private final ChallengeRepository challengeRepository;
    private final ChallengeDailyGuideRepository challengeDailyGuideRepository;
    private final ChallengeDailyGuideCommentRepository challengeDailyGuideCommentRepository;
    private final ChallengeParticipantRepository challengeParticipantRepository;

    public TodayDailyGuideResponse getTodayDailyGuide(Long challengeId, Long memberId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "challenge")
                        .addContext(ErrorContextKeys.CHALLENGE_ID, challengeId));
        
        if (!challengeParticipantRepository.existsByChallengeIdAndMemberId(challengeId, memberId)) {
            throw new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "challengeParticipant")
                    .addContext(ErrorContextKeys.MEMBER_ID, memberId)
                    .addContext(ErrorContextKeys.CHALLENGE_ID, challengeId);
        }

        LocalDate today = LocalDate.now(SEOUL_ZONE);
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
                myComment
        );
    }

    @Transactional
    public void createDailyGuideComment(Long challengeId, int dayIndex, Long memberId,
                                        DailyGuideCommentRequest request) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "challenge")
                        .addContext(ErrorContextKeys.CHALLENGE_ID, challengeId));

        ChallengeParticipant participant = challengeParticipantRepository.findByChallengeIdAndMemberId(
                        challengeId, memberId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "challengeParticipant")
                        .addContext(ErrorContextKeys.MEMBER_ID, memberId)
                        .addContext(ErrorContextKeys.CHALLENGE_ID, challengeId));

        if (dayIndex != 0 && (dayIndex < 1 || dayIndex > challenge.getTotalDays())) {
            throw new CIllegalArgumentException(ErrorDetail.INVALID_INPUT_VALUE)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "challengeDailyGuide")
                    .addContext(ErrorContextKeys.CHALLENGE_ID, challengeId)
                    .addContext("dayIndex", dayIndex)
                    .addContext("reason", "유효하지 않은 일차 인덱스입니다.");
        }

        ChallengeDailyGuide guide = challengeDailyGuideRepository.findByChallengeIdAndDayIndex(challengeId, dayIndex)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "challengeDailyGuide")
                        .addContext(ErrorContextKeys.CHALLENGE_ID, challengeId)
                        .addContext("dayIndex", dayIndex));

        if (!guide.isCommentEnabled()) {
            throw new CIllegalArgumentException(ErrorDetail.INVALID_INPUT_VALUE)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "challengeDailyGuide")
                    .addContext(ErrorContextKeys.CHALLENGE_ID, challengeId)
                    .addContext("dayIndex", dayIndex)
                    .addContext("reason", "댓글 작성이 불가능한 가이드입니다.");
        }

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

        ChallengeDailyGuideComment comment = ChallengeDailyGuideComment.builder()
                .guideId(guide.getId())
                .participantId(participant.getId())
                .content(request.content())
                .build();
        challengeDailyGuideCommentRepository.save(comment);
    }

    private int calculateDayIndex(LocalDate startDate, LocalDate today) {
        DayOfWeek dayOfWeek = today.getDayOfWeek();
        boolean isWeekend = dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
        
        if (isWeekend) {
            return 0;
        }
        return (int) DAYS.between(startDate, today) + 1;
    }

    private MyCommentResponse createMyCommentResponse(TodayDailyGuideRow row) {
        boolean exists = row.getMyCommentExists() == 1;
        return new MyCommentResponse(
                exists,
                exists ? row.getMyCommentContent() : null,
                exists ? row.getMyCommentCreatedAt() : null
        );
    }
}
