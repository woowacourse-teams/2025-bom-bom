package me.bombom.api.v1.challenge.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.challenge.domain.MyChallengeSummary;
import me.bombom.api.v1.challenge.dto.MemberMedalParticipationFlat;
import me.bombom.api.v1.challenge.dto.MemberChallengeRankingStatsFlat;
import me.bombom.api.v1.challenge.dto.response.MyChallengeSummaryResponse;
import me.bombom.api.v1.challenge.repository.ChallengeParticipantRepository;
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
}
