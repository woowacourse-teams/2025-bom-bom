package me.bombom.api.v1.nativenewsletter.maeilmail.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailTopic;
import me.bombom.api.v1.nativenewsletter.maeilmail.repository.MemberTopicKey;

public record IssueContext(
        Map<Long, MaeilMailTopic> todayTopicsByTrackId,
        Map<Long, List<Long>> contentIdsByTopicId,
        Map<MemberTopicKey, List<Long>> sentContentIdsByMemberTopic,
        Long newsletterId,
        LocalDateTime issuedAt
) {
}
