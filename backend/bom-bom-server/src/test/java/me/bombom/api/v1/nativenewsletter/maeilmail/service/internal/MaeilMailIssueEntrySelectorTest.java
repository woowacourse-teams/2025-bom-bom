package me.bombom.api.v1.nativenewsletter.maeilmail.service.internal;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailSubscriptionTrack;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailTopic;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailTrack;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.IssueData;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.IssueEntrySelectionResult;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.MemberTopicKey;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MaeilMailIssueEntrySelectorTest {

    @Mock
    private MaeilMailIssueContentAssigner contentAssigner;

    @InjectMocks
    private MaeilMailIssueEntrySelector entrySelector;

    @Test
    void 같은_member_topic_트랙은_하나의_pending_entry로_합친다() {
        MaeilMailSubscriptionTrack firstTrack = createTrack(1L, 10L, MaeilMailTrack.BE);
        MaeilMailSubscriptionTrack secondTrack = createTrack(2L, 10L, MaeilMailTrack.BE);
        MaeilMailTopic topic = createTopic(100L, MaeilMailTrack.BE);
        IssueData issueData = new IssueData(
                Map.of(1L, topic, 2L, topic),
                Map.of(100L, List.of(1000L)),
                Map.of(),
                Set.of(),
                500L
        );
        given(contentAssigner.assignContentIdOrRecycle(10L, 100L, issueData.contentIdsByTopicId(),
                issueData.sentContentIdsByMemberTopic()))
                .willReturn(Optional.of(1000L));

        IssueEntrySelectionResult result = entrySelector.select(List.of(firstTrack, secondTrack), issueData);

        assertSoftly(softly -> {
            softly.assertThat(result.pendingEntries()).hasSize(1);
            softly.assertThat(result.pendingEntries().getFirst().memberId()).isEqualTo(10L);
            softly.assertThat(result.pendingEntries().getFirst().topicId()).isEqualTo(100L);
            softly.assertThat(result.pendingEntries().getFirst().newsletterId()).isEqualTo(500L);
            softly.assertThat(result.pendingEntries().getFirst().contentId()).isEqualTo(1000L);
            softly.assertThat(result.pendingEntries().getFirst().trackIds()).containsExactly(1L, 2L);
            softly.assertThat(result.previouslyIssuedTrackIds()).isEmpty();
        });
        verify(contentAssigner).assignContentIdOrRecycle(10L, 100L, issueData.contentIdsByTopicId(),
                issueData.sentContentIdsByMemberTopic());
    }

    @Test
    void 이미_발행된_member_topic은_pending_entry에서_제외하고_track_id만_반환한다() {
        MaeilMailSubscriptionTrack track = createTrack(1L, 10L, MaeilMailTrack.BE);
        MaeilMailTopic topic = createTopic(100L, MaeilMailTrack.BE);
        IssueData issueData = new IssueData(
                Map.of(1L, topic),
                Map.of(100L, List.of(1000L)),
                Map.of(),
                Set.of(new MemberTopicKey(10L, 100L)),
                500L
        );

        IssueEntrySelectionResult result = entrySelector.select(List.of(track), issueData);

        assertSoftly(softly -> {
            softly.assertThat(result.pendingEntries()).isEmpty();
            softly.assertThat(result.previouslyIssuedTrackIds()).containsExactly(1L);
        });
        verify(contentAssigner, never()).assignContentIdOrRecycle(10L, 100L, issueData.contentIdsByTopicId(),
                issueData.sentContentIdsByMemberTopic());
    }

    @Test
    void 발행할_토픽이나_컨텐츠가_없으면_pending_entry를_만들지_않는다() {
        MaeilMailSubscriptionTrack missingTopicTrack = createTrack(1L, 10L, MaeilMailTrack.BE);
        MaeilMailSubscriptionTrack emptyContentTrack = createTrack(2L, 20L, MaeilMailTrack.BE);
        MaeilMailTopic topic = createTopic(100L, MaeilMailTrack.BE);
        IssueData issueData = new IssueData(
                Map.of(2L, topic),
                Map.of(),
                Map.of(),
                Set.of(),
                500L
        );
        given(contentAssigner.assignContentIdOrRecycle(20L, 100L, issueData.contentIdsByTopicId(),
                issueData.sentContentIdsByMemberTopic()))
                .willReturn(Optional.empty());

        IssueEntrySelectionResult result = entrySelector.select(List.of(missingTopicTrack, emptyContentTrack), issueData);

        assertSoftly(softly -> {
            softly.assertThat(result.pendingEntries()).isEmpty();
            softly.assertThat(result.previouslyIssuedTrackIds()).isEmpty();
        });
        verify(contentAssigner).assignContentIdOrRecycle(20L, 100L, issueData.contentIdsByTopicId(),
                issueData.sentContentIdsByMemberTopic());
    }

    private MaeilMailSubscriptionTrack createTrack(Long id, Long memberId, MaeilMailTrack track) {
        MaeilMailSubscriptionTrack subscriptionTrack = MaeilMailSubscriptionTrack.builder()
                .subscribeId(id + 100L)
                .memberId(memberId)
                .field(track)
                .build();
        ReflectionTestUtils.setField(subscriptionTrack, "id", id);
        return subscriptionTrack;
    }

    private MaeilMailTopic createTopic(Long id, MaeilMailTrack track) {
        MaeilMailTopic topic = MaeilMailTopic.builder()
                .track(track)
                .name("topic-" + id)
                .displayOrder(0)
                .build();
        ReflectionTestUtils.setField(topic, "id", id);
        return topic;
    }
}
