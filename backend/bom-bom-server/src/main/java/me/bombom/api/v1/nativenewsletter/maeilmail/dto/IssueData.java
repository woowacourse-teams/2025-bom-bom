package me.bombom.api.v1.nativenewsletter.maeilmail.dto;

import java.util.List;
import java.util.Map;
import java.util.Set;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailTopic;

public record IssueData(
        Map<Long, MaeilMailTopic> issueTopicsByTrackId,
        Map<Long, List<Long>> contentIdsByTopicId,
        Map<MemberTopicKey, List<Long>> sentContentIdsByMemberTopic,
        Set<MemberTopicKey> issuedMemberTopicKeys,
        Long newsletterId
) {
}
