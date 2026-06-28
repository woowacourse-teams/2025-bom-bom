package me.bombom.api.v1.challenge.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.challenge.domain.MyChallengeSummary;
import me.bombom.api.v1.challenge.dto.EndedChallengeParticipationFlat;
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
        List<EndedChallengeParticipationFlat> endedParticipations =
                challengeParticipantRepository.findEndedChallengeParticipations(today);
        MyChallengeSummary summary = MyChallengeSummary.of(endedParticipations, memberId);
        return MyChallengeSummaryResponse.from(summary);
    }
}
