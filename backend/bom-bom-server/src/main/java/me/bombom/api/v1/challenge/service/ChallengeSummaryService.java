package me.bombom.api.v1.challenge.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.challenge.domain.CompletedChallengeSummary;
import me.bombom.api.v1.challenge.domain.MyChallengeSummary;
import me.bombom.api.v1.challenge.domain.OngoingChallengeSummary;
import me.bombom.api.v1.challenge.dto.MemberMedalParticipationFlat;
import me.bombom.api.v1.challenge.dto.MemberChallengeRankingStatsFlat;
import me.bombom.api.v1.challenge.dto.OngoingChallengeParticipantFlat;
import me.bombom.api.v1.challenge.dto.response.CompletedChallengeResponse;
import me.bombom.api.v1.challenge.dto.response.MyChallengeSummaryResponse;
import me.bombom.api.v1.challenge.dto.response.OngoingChallengesResponse;
import me.bombom.api.v1.challenge.repository.ChallengeParticipantRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengeSummaryService {

    private final ChallengeParticipantRepository challengeParticipantRepository;
    private final Clock clock;

    public MyChallengeSummaryResponse getMyChallengeSummary(Long memberId) {
        LocalDate today = LocalDate.now(clock);
        List<MemberChallengeRankingStatsFlat> aggregates =
                challengeParticipantRepository.findEndedChallengeAggregates(today);
        List<MemberMedalParticipationFlat> myParticipations =
                challengeParticipantRepository.findEndedChallengeParticipationsByMemberId(memberId, today);
        MyChallengeSummary summary = MyChallengeSummary.of(aggregates, myParticipations, memberId);
        return MyChallengeSummaryResponse.from(summary);
    }

    public OngoingChallengesResponse getOngoingChallenges(Long memberId) {
        LocalDate today = LocalDate.now(clock);
        List<OngoingChallengeParticipantFlat> participants =
                challengeParticipantRepository.findParticipantsOfMemberOngoingChallenges(memberId, today);

        Map<Long, List<OngoingChallengeParticipantFlat>> participantsByChallenge = participants.stream()
                .collect(Collectors.groupingBy(OngoingChallengeParticipantFlat::challengeId));

        List<OngoingChallengeSummary> summaries = participantsByChallenge.values()
                .stream()
                .map(challengeParticipants -> OngoingChallengeSummary.of(challengeParticipants, memberId, today))
                .sorted(Comparator.comparing(OngoingChallengeSummary::endDate)
                        .thenComparing(OngoingChallengeSummary::challengeId))
                .toList();

        return OngoingChallengesResponse.from(summaries);
    }

    public Page<CompletedChallengeResponse> getCompletedChallenges(Long memberId, Pageable pageable) {
        LocalDate today = LocalDate.now(clock);
        Pageable enforcedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());

        return challengeParticipantRepository.findCompletedChallenges(memberId, today, enforcedPageable)
                .map(CompletedChallengeSummary::from)
                .map(CompletedChallengeResponse::from);
    }
}
