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
import me.bombom.api.v1.challenge.dto.ChallengeNewsletterRow;
import me.bombom.api.v1.challenge.dto.ChallengeParticipantCount;
import me.bombom.api.v1.challenge.dto.response.ChallengeDetailResponse;
import me.bombom.api.v1.challenge.dto.response.ChallengeNewsletterResponse;
import me.bombom.api.v1.challenge.dto.response.ChallengeResponse;
import me.bombom.api.v1.challenge.repository.ChallengeNewsletterRepository;
import me.bombom.api.v1.challenge.repository.ChallengeParticipantRepository;
import me.bombom.api.v1.challenge.repository.ChallengeRepository;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.challenge.dto.response.ChallengeInfoResponse;
import me.bombom.api.v1.challenge.dto.response.ChallengeInfoResponse;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
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
        Map<Long, ChallengeParticipant> myParticipation = findMyParticipation(member);

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

    private Map<Long, ChallengeParticipant> findMyParticipation(Member member) {
        if (member == null) {
            return Collections.emptyMap();
        }
        return challengeParticipantRepository.findAllByMemberId(member.getId())
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
}
