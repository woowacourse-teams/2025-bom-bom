package news.bombomemail.nativenewsletter.maeilmail.service.internal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import news.bombomemail.nativenewsletter.maeilmail.domain.MaeilMailSentContent;
import news.bombomemail.nativenewsletter.maeilmail.domain.MaeilMailSubscriptionTrack;
import news.bombomemail.nativenewsletter.maeilmail.domain.MaeilMailTopic;
import news.bombomemail.nativenewsletter.maeilmail.domain.MaeilMailTrack;
import news.bombomemail.nativenewsletter.maeilmail.dto.IssueData;
import news.bombomemail.nativenewsletter.maeilmail.dto.MemberTopicKey;
import news.bombomemail.nativenewsletter.maeilmail.dto.TopicContentId;
import news.bombomemail.nativenewsletter.maeilmail.repository.MaeilMailContentRepository;
import news.bombomemail.nativenewsletter.maeilmail.repository.MaeilMailIssueHistoryRepository;
import news.bombomemail.nativenewsletter.maeilmail.repository.MaeilMailSentContentRepository;
import news.bombomemail.nativenewsletter.maeilmail.repository.MaeilMailTopicRepository;
import news.bombomemail.subscribe.domain.Subscribe;
import news.bombomemail.subscribe.repository.SubscribeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MaeilMailIssueDataLoaderTest {

    @Mock
    private MaeilMailTopicRepository topicRepository;

    @Mock
    private MaeilMailContentRepository contentRepository;

    @Mock
    private MaeilMailSentContentRepository sentContentRepository;

    @Mock
    private MaeilMailIssueHistoryRepository issueHistoryRepository;

    @Mock
    private SubscribeRepository subscribeRepository;

    @InjectMocks
    private MaeilMailIssueDataLoader dataLoader;

    @Test
    void track의_curriculumIndex로_발행_topic을_고르고_필요한_데이터를_로딩한다() {
        // given
        LocalDate issueDate = LocalDate.of(2026, 4, 27);
        MaeilMailSubscriptionTrack beTrack = createTrack(1L, 10L, 100L, MaeilMailTrack.BE, 1);
        MaeilMailSubscriptionTrack feTrack = createTrack(2L, 11L, 200L, MaeilMailTrack.FE, 0);
        MaeilMailTopic beFirstTopic = createTopic(1000L, MaeilMailTrack.BE, "java", 1);
        MaeilMailTopic beSecondTopic = createTopic(1001L, MaeilMailTrack.BE, "spring", 2);
        MaeilMailTopic feTopic = createTopic(2000L, MaeilMailTrack.FE, "react", 1);
        MaeilMailSentContent sentContent = createSentContent(100L, beSecondTopic.getId(), 9000L);
        Set<MemberTopicKey> issuedKeys = Set.of(new MemberTopicKey(200L, feTopic.getId()));

        given(subscribeRepository.findAllById(List.of(10L, 11L))).willReturn(List.of(
                createSubscribe(10L, 50L, 100L),
                createSubscribe(11L, 50L, 200L)
        ));
        given(topicRepository.findAllByOrderByDisplayOrderAsc()).willReturn(List.of(
                beFirstTopic,
                beSecondTopic,
                feTopic
        ));
        given(contentRepository.findContentIdsByTopicIdIn(anyCollection())).willReturn(List.of(
                new TopicContentId(beSecondTopic.getId(), 9100L),
                new TopicContentId(feTopic.getId(), 9200L)
        ));
        given(sentContentRepository.findAllByMemberTopicKeys(List.of(
                new MemberTopicKey(100L, beSecondTopic.getId()),
                new MemberTopicKey(200L, feTopic.getId())
        ))).willReturn(List.of(sentContent));
        given(issueHistoryRepository.findIssuedMemberTopicKeys(issueDate, List.of(
                new MemberTopicKey(100L, beSecondTopic.getId()),
                new MemberTopicKey(200L, feTopic.getId())
        ))).willReturn(issuedKeys);

        // when
        IssueData result = dataLoader.load(issueDate, List.of(beTrack, feTrack));

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.newsletterId()).isEqualTo(50L);
            softly.assertThat(result.issueTopicsByTrackId()).containsEntry(beTrack.getId(), beSecondTopic);
            softly.assertThat(result.issueTopicsByTrackId()).containsEntry(feTrack.getId(), feTopic);
            softly.assertThat(result.contentIdsByTopicId().get(beSecondTopic.getId())).containsExactly(9100L);
            softly.assertThat(result.contentIdsByTopicId().get(feTopic.getId())).containsExactly(9200L);
            softly.assertThat(result.sentContentIdsByMemberTopic().get(new MemberTopicKey(100L, beSecondTopic.getId())))
                    .containsExactly(9000L);
            softly.assertThat(result.issuedMemberTopicKeys()).containsExactlyInAnyOrderElementsOf(issuedKeys);
        });
    }

    @Test
    void curriculumIndex가_topic_size보다_커도_나머지로_발행_topic을_순환_선택한다() {
        // given
        LocalDate issueDate = LocalDate.of(2026, 4, 27);
        MaeilMailSubscriptionTrack track = createTrack(1L, 10L, 100L, MaeilMailTrack.BE, 3);
        MaeilMailTopic firstTopic = createTopic(1000L, MaeilMailTrack.BE, "java", 1);
        MaeilMailTopic secondTopic = createTopic(1001L, MaeilMailTrack.BE, "spring", 2);

        given(subscribeRepository.findAllById(List.of(10L))).willReturn(List.of(
                createSubscribe(10L, 50L, 100L)
        ));
        given(topicRepository.findAllByOrderByDisplayOrderAsc()).willReturn(List.of(firstTopic, secondTopic));
        given(contentRepository.findContentIdsByTopicIdIn(anyCollection())).willReturn(List.of(
                new TopicContentId(secondTopic.getId(), 9100L)
        ));
        given(sentContentRepository.findAllByMemberTopicKeys(List.of(
                new MemberTopicKey(100L, secondTopic.getId())
        ))).willReturn(List.of());
        given(issueHistoryRepository.findIssuedMemberTopicKeys(issueDate, List.of(
                new MemberTopicKey(100L, secondTopic.getId())
        ))).willReturn(Set.of());

        // when
        IssueData result = dataLoader.load(issueDate, List.of(track));

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.issueTopicsByTrackId()).containsEntry(track.getId(), secondTopic);
            softly.assertThat(result.contentIdsByTopicId().get(secondTopic.getId())).containsExactly(9100L);
        });
    }

    @Test
    void track의_subscribe를_찾지_못하면_예외가_발생한다() {
        // given
        MaeilMailSubscriptionTrack track = createTrack(1L, 10L, 100L, MaeilMailTrack.BE, 0);
        given(subscribeRepository.findAllById(List.of(10L))).willReturn(List.of());

        // when & then
        assertThatThrownBy(() -> dataLoader.load(LocalDate.of(2026, 4, 27), List.of(track)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Subscribe");
    }

    @Test
    void 발행대상_track의_newsletterId가_하나가_아니면_예외가_발생한다() {
        // given
        MaeilMailSubscriptionTrack firstTrack = createTrack(1L, 10L, 100L, MaeilMailTrack.BE, 0);
        MaeilMailSubscriptionTrack secondTrack = createTrack(2L, 11L, 200L, MaeilMailTrack.FE, 0);
        given(subscribeRepository.findAllById(List.of(10L, 11L))).willReturn(List.of(
                createSubscribe(10L, 50L, 100L),
                createSubscribe(11L, 60L, 200L)
        ));

        // when & then
        assertThatThrownBy(() -> dataLoader.load(LocalDate.of(2026, 4, 27), List.of(firstTrack, secondTrack)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("newsletterId");
    }

    private MaeilMailSubscriptionTrack createTrack(
            Long id,
            Long subscribeId,
            Long memberId,
            MaeilMailTrack field,
            int curriculumIndex
    ) {
        MaeilMailSubscriptionTrack track = MaeilMailSubscriptionTrack.builder()
                .subscribeId(subscribeId)
                .memberId(memberId)
                .field(field)
                .build();
        ReflectionTestUtils.setField(track, "id", id);
        ReflectionTestUtils.setField(track, "curriculumIndex", curriculumIndex);
        return track;
    }

    private MaeilMailTopic createTopic(Long id, MaeilMailTrack track, String name, int displayOrder) {
        MaeilMailTopic topic = MaeilMailTopic.builder()
                .track(track)
                .name(name)
                .displayOrder(displayOrder)
                .build();
        ReflectionTestUtils.setField(topic, "id", id);
        return topic;
    }

    private MaeilMailSentContent createSentContent(Long memberId, Long topicId, Long contentId) {
        return MaeilMailSentContent.builder()
                .memberId(memberId)
                .topicId(topicId)
                .contentId(contentId)
                .build();
    }

    private Subscribe createSubscribe(Long id, Long newsletterId, Long memberId) {
        return Subscribe.builder()
                .id(id)
                .newsletterId(newsletterId)
                .memberId(memberId)
                .build();
    }
}
