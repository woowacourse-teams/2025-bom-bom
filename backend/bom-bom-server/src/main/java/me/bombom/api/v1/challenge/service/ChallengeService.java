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
import me.bombom.api.v1.challenge.domain.ChallengeNewsletter;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import me.bombom.api.v1.challenge.domain.ChallengeStatus;
import me.bombom.api.v1.challenge.dto.ChallengeParticipantCount;
import me.bombom.api.v1.challenge.dto.response.ChallengeDetailResponse;
import me.bombom.api.v1.challenge.dto.response.ChallengeNewsletterResponse;
import me.bombom.api.v1.challenge.dto.response.ChallengeResponse;
import me.bombom.api.v1.challenge.repository.ChallengeNewsletterRepository;
import me.bombom.api.v1.challenge.repository.ChallengeParticipantRepository;
import me.bombom.api.v1.challenge.repository.ChallengeRepository;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.newsletter.domain.Newsletter;
import me.bombom.api.v1.newsletter.repository.NewsletterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final ChallengeParticipantRepository challengeParticipantRepository;
    private final ChallengeNewsletterRepository challengeNewsletterRepository;
    private final NewsletterRepository newsletterRepository;

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
                .collect(toList());
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
        List<ChallengeNewsletter> challengeNewsletters = challengeNewsletterRepository.findAllByChallengeIdIn(
                challengeIds);
        if (challengeNewsletters.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> newsletterIds = challengeNewsletters.stream()
                .map(ChallengeNewsletter::getNewsletterId)
                .distinct()
                .toList();

        Map<Long, Newsletter> newslettersById = newsletterRepository.findAllById(newsletterIds)
                .stream()
                .collect(toMap(Newsletter::getId, newsletter -> newsletter));

        return challengeNewsletters.stream()
                .filter(cn -> newslettersById.containsKey(cn.getNewsletterId()))
                .collect(groupingBy(
                        ChallengeNewsletter::getChallengeId,
                        mapping(cn -> ChallengeNewsletterResponse.from(newslettersById.get(cn.getNewsletterId())),
                                toList())
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
        } else {
            return ChallengeDetailResponse.ongoing(progress);
        }
    }
}
