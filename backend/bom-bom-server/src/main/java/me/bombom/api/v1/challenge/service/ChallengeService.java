package me.bombom.api.v1.challenge.service;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.challenge.domain.Challenge;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import me.bombom.api.v1.challenge.domain.ChallengeStatus;
import me.bombom.api.v1.challenge.domain.EligibilityReason;
import me.bombom.api.v1.challenge.dto.ChallengeNewsletterRow;
import me.bombom.api.v1.challenge.dto.ChallengeParticipantCount;
import me.bombom.api.v1.challenge.dto.response.ChallengeDetailResponse;
import me.bombom.api.v1.challenge.dto.response.ChallengeEligibilityResponse;
import me.bombom.api.v1.challenge.dto.response.ChallengeInfoResponse;
import me.bombom.api.v1.challenge.dto.response.ChallengeNewsletterResponse;
import me.bombom.api.v1.challenge.dto.response.ChallengeResponse;
import me.bombom.api.v1.challenge.repository.ChallengeNewsletterRepository;
import me.bombom.api.v1.challenge.repository.ChallengeParticipantRepository;
import me.bombom.api.v1.challenge.repository.ChallengeRepository;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.CServerErrorException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.common.exception.UnauthorizedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengeService {

    // TODO: 이후에 수료 처리 등 구현 시 관리 방법 고려
    private static final int SUCCESS_REQUIRED_PERCENT = 80;

    private final ChallengeRepository challengeRepository;
    private final ChallengeParticipantRepository challengeParticipantRepository;
    private final ChallengeNewsletterRepository challengeNewsletterRepository;

    public List<ChallengeResponse> getChallenges(Member member) {
        List<Challenge> challenges = challengeRepository.findAll();
        if (challenges.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> challengeIds = challenges.stream()
                .map(Challenge::getId)
                .toList();

        Map<Long, Long> participantCounts = getParticipantCounts(challengeIds);
        Map<Long, List<ChallengeNewsletterResponse>> newslettersByChallengeId = getNewslettersByChallengeId(challengeIds);
        Map<Long, ChallengeParticipant> myParticipation = findMyParticipation(member, challengeIds);

        return challenges.stream()
                .map(challenge -> toChallengeResponse(
                        challenge,
                        participantCounts,
                        newslettersByChallengeId,
                        myParticipation
                ))
                .toList();
    }

    public ChallengeInfoResponse getChallengeInfo(Long id) {
        Challenge challenge = challengeRepository.findById(id)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "challenge")
                        .addContext(ErrorContextKeys.OPERATION, "getChallengeInfo"));

        return ChallengeInfoResponse.of(challenge, SUCCESS_REQUIRED_PERCENT);
    }

    public ChallengeEligibilityResponse checkEligibility(Long challengeId, Member member) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "challenge")
                        .addContext(ErrorContextKeys.OPERATION, "checkEligibility"));

        if (member == null) {
            return new ChallengeEligibilityResponse(false, EligibilityReason.NOT_LOGGED_IN);
        }

        EligibilityReason reason = validateEligibility(challenge, challengeId, member.getId());
        return new ChallengeEligibilityResponse(reason == EligibilityReason.ELIGIBLE, reason);
    }

    @Transactional
    public void applyChallenge(Long challengeId, Member member) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "challenge")
                        .addContext(ErrorContextKeys.OPERATION, "applyChallenge"));

        EligibilityReason reason = validateEligibility(challenge, challengeId, member.getId());

        if (reason == EligibilityReason.ALREADY_APPLIED) {
            log.debug("챌린지 신청 중복 요청 - challengeId={}, memberId={}", challengeId, member.getId());
            return;
        }
        
        if (reason != EligibilityReason.ELIGIBLE) {
            throw createApplyException(challengeId, member, reason);
        }

        ChallengeParticipant participant = ChallengeParticipant.builder()
                .challengeId(challengeId)
                .memberId(member.getId())
                .build();

        challengeParticipantRepository.save(participant);
    }

    @Transactional
    public void cancelChallenge(Long challengeId, Member member) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "challenge")
                        .addContext(ErrorContextKeys.OPERATION, "cancelChallenge"));

        if (challenge.hasStarted(LocalDate.now())) {
            throw new CIllegalArgumentException(ErrorDetail.INVALID_INPUT_VALUE)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "challenge")
                    .addContext(ErrorContextKeys.OPERATION, "cancelChallenge")
                    .addContext("reason", "이미 시작된 챌린지는 취소할 수 없습니다.");
        }

        ChallengeParticipant participant = challengeParticipantRepository.findByChallengeIdAndMemberId(challengeId, member.getId())
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "challengeParticipant")
                        .addContext(ErrorContextKeys.MEMBER_ID, member.getId())
                        .addContext("challengeId", challengeId));

        challengeParticipantRepository.delete(participant);
    }

    private ChallengeResponse toChallengeResponse(
            Challenge challenge,
            Map<Long, Long> participantCounts,
            Map<Long, List<ChallengeNewsletterResponse>> newslettersByChallengeId,
            Map<Long, ChallengeParticipant> myParticipation
    ) {
        long participantCount = participantCounts.getOrDefault(challenge.getId(), 0L);
        List<ChallengeNewsletterResponse> newsletterResponses = newslettersByChallengeId.getOrDefault(
                challenge.getId(),
                Collections.emptyList()
        );
        ChallengeStatus status = challenge.getStatus(LocalDate.now());
        ChallengeParticipant myParticipant = myParticipation.get(challenge.getId());
        ChallengeDetailResponse detailResponse = calculateDetailResponse(challenge, myParticipant);

        return ChallengeResponse.of(challenge, participantCount, newsletterResponses, status, detailResponse);
    }

    private Map<Long, Long> getParticipantCounts(List<Long> challengeIds) {
        return challengeParticipantRepository.countByChallengeIdInGroupByChallengeId(challengeIds)
                .stream()
                .collect(toMap(ChallengeParticipantCount::challengeId, ChallengeParticipantCount::count));
    }

    private Map<Long, ChallengeParticipant> findMyParticipation(Member member, List<Long> challengeIds) {
        if (member == null) {
            return Collections.emptyMap();
        }
        return challengeParticipantRepository.findByMemberIdAndChallengeIdIn(member.getId(), challengeIds)
                .stream()
                .collect(toMap(ChallengeParticipant::getChallengeId, p -> p));
    }

    private Map<Long, List<ChallengeNewsletterResponse>> getNewslettersByChallengeId(List<Long> challengeIds) {
        List<ChallengeNewsletterRow> rows = challengeNewsletterRepository.findNewsletterResponsesByChallengeIds(challengeIds);

        return rows.stream()
                .collect(groupingBy(
                        ChallengeNewsletterRow::challengeId,
                        mapping(ChallengeNewsletterRow::response, toList())
                ));
    }

    private ChallengeDetailResponse calculateDetailResponse(
            Challenge challenge,
            ChallengeParticipant myParticipant
    ) {
        if (myParticipant == null) {
            return ChallengeDetailResponse.notJoined();
        }

        int progress = myParticipant.calculateProgress(challenge.getTotalDays());
        boolean isEnded = challenge.isEnded(LocalDate.now());

        if (isEnded) {
            return ChallengeDetailResponse.ended(progress, myParticipant.isSurvived());
        }
        return ChallengeDetailResponse.ongoing(progress);
    }

    private RuntimeException createApplyException(Long challengeId, Member member, EligibilityReason reason) {
        if (reason == EligibilityReason.NOT_LOGGED_IN) {
            return new UnauthorizedException(ErrorDetail.UNAUTHORIZED)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "challenge")
                    .addContext(ErrorContextKeys.OPERATION, "applyChallenge");
        }
        if (reason == EligibilityReason.ALREADY_STARTED) {
            return new CIllegalArgumentException(ErrorDetail.INVALID_INPUT_VALUE)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "challenge")
                    .addContext(ErrorContextKeys.OPERATION, "applyChallenge");
        }
        if (reason == EligibilityReason.NOT_SUBSCRIBED) {
            return new CIllegalArgumentException(ErrorDetail.PRECONDITION_FAILED)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "challenge")
                    .addContext(ErrorContextKeys.MEMBER_ID, member.getId())
                    .addContext("challengeId", challengeId);
        }
        return new CServerErrorException(ErrorDetail.INTERNAL_SERVER_ERROR)
                .addContext(ErrorContextKeys.ENTITY_TYPE, "challenge")
                .addContext(ErrorContextKeys.OPERATION, "applyChallenge")
                .addContext("reason", "ELIGIBLE 상태에서는 예외를 생성할 수 없습니다.");
    }

    private EligibilityReason validateEligibility(Challenge challenge, Long challengeId, Long memberId) {
        if (challenge.hasStarted(LocalDate.now())) {
            return EligibilityReason.ALREADY_STARTED;
        }

        boolean alreadyApplied = challengeParticipantRepository.existsByChallengeIdAndMemberId(challengeId, memberId);
        if (alreadyApplied) {
            return EligibilityReason.ALREADY_APPLIED;
        }

        boolean hasSubscribedNewsletter = challengeNewsletterRepository.existsSubscribedNewsletter(challengeId, memberId);
        if (!hasSubscribedNewsletter) {
            return EligibilityReason.NOT_SUBSCRIBED;
        }

        return EligibilityReason.ELIGIBLE;
    }
}
