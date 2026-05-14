package news.bombomemail.nativenewsletter.maeilmail.service.internal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import news.bombomemail.article.domain.Article;
import news.bombomemail.nativenewsletter.maeilmail.domain.MaeilMailContent;
import news.bombomemail.nativenewsletter.maeilmail.domain.MaeilMailSentContent;
import news.bombomemail.nativenewsletter.maeilmail.domain.MaeilMailSubscriptionTrack;
import news.bombomemail.nativenewsletter.maeilmail.domain.MaeilMailTopic;
import news.bombomemail.nativenewsletter.maeilmail.domain.MaeilMailTrack;
import news.bombomemail.nativenewsletter.maeilmail.dto.IssueData;
import news.bombomemail.nativenewsletter.maeilmail.dto.IssueEntry;
import news.bombomemail.nativenewsletter.maeilmail.dto.MemberTopicKey;
import news.bombomemail.nativenewsletter.maeilmail.dto.PreparedIssueEntries;
import news.bombomemail.nativenewsletter.maeilmail.repository.MaeilMailContentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MaeilMailIssueEntryPreparerTest {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    @Mock
    private MaeilMailContentRepository contentRepository;

    @Mock
    private MaeilMailIssueContentAssigner contentAssigner;

    private MaeilMailIssueEntryPreparer entryPreparer;

    @BeforeEach
    void setup() {
        LocalDateTime arrivedAt = LocalDateTime.of(2026, 4, 26, 7, 0);
        Clock clock = Clock.fixed(arrivedAt.atZone(SEOUL).toInstant(), SEOUL);
        entryPreparer = new MaeilMailIssueEntryPreparer(clock, contentRepository, contentAssigner);
    }

    @Test
    void 같은_member_topic은_하나의_issue_entry로_합친다() {
        // given
        MaeilMailSubscriptionTrack firstTrack = createTrack(1L, 10L, 100L, MaeilMailTrack.BE, 0);
        MaeilMailSubscriptionTrack secondTrack = createTrack(2L, 11L, 100L, MaeilMailTrack.BE, 0);
        MaeilMailTopic topic = createTopic(1000L, MaeilMailTrack.BE, "java", 1);
        MaeilMailContent content = createContent(
                9000L,
                topic.getId(),
                "매일메일 제목",
                "<p>본문</p>",
                "본문",
                "요약",
                3
        );
        IssueData issueData = createIssueData(
                Map.of(1L, topic, 2L, topic),
                Map.of(topic.getId(), List.of(content.getId())),
                Map.of(),
                Set.of(),
                50L
        );
        given(contentAssigner.assignContentIdOrRecycle(100L, topic.getId(), issueData.contentIdsByTopicId(),
                issueData.sentContentIdsByMemberTopic()))
                .willReturn(Optional.of(content.getId()));
        given(contentRepository.findAllById(List.of(content.getId()))).willReturn(List.of(content));

        // when
        PreparedIssueEntries result = entryPreparer.prepare(List.of(firstTrack, secondTrack), issueData);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.entries()).hasSize(1);
            softly.assertThat(result.previouslyIssuedTrackIds()).isEmpty();

            IssueEntry entry = result.entries().getFirst();
            softly.assertThat(entry.trackIds()).containsExactly(1L, 2L);

            Article article = entry.article();
            softly.assertThat(article.getTitle()).isEqualTo("매일메일 제목");
            softly.assertThat(article.getContents()).isEqualTo("<p>본문</p>");
            softly.assertThat(article.getContentsText()).isEqualTo("본문");
            softly.assertThat(article.getContentsSummary()).isEqualTo("요약");
            softly.assertThat(article.getExpectedReadTime()).isEqualTo(3);
            softly.assertThat(article.getMemberId()).isEqualTo(100L);
            softly.assertThat(article.getNewsletterId()).isEqualTo(50L);
            softly.assertThat(article.getArrivedDateTime()).isEqualTo(LocalDateTime.of(2026, 4, 26, 7, 0));

            MaeilMailSentContent sentContent = entry.sentContent();
            softly.assertThat(sentContent.getMemberId()).isEqualTo(100L);
            softly.assertThat(sentContent.getTopicId()).isEqualTo(topic.getId());
            softly.assertThat(sentContent.getContentId()).isEqualTo(content.getId());
            softly.assertThat(entry.contentId()).isEqualTo(content.getId());
        });
        verify(contentAssigner).assignContentIdOrRecycle(100L, topic.getId(), issueData.contentIdsByTopicId(),
                issueData.sentContentIdsByMemberTopic());
    }

    @Test
    void 이미_발행된_member_topic은_entry를_만들지_않고_track_id만_반환한다() {
        // given
        MaeilMailSubscriptionTrack track = createTrack(1L, 10L, 100L, MaeilMailTrack.BE, 0);
        MaeilMailTopic topic = createTopic(1000L, MaeilMailTrack.BE, "java", 1);
        IssueData issueData = createIssueData(
                Map.of(track.getId(), topic),
                Map.of(topic.getId(), List.of(9000L)),
                Map.of(),
                Set.of(new MemberTopicKey(100L, topic.getId())),
                50L
        );

        // when
        PreparedIssueEntries result = entryPreparer.prepare(List.of(track), issueData);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.entries()).isEmpty();
            softly.assertThat(result.previouslyIssuedTrackIds()).containsExactly(1L);
        });
        verifyNoInteractions(contentAssigner, contentRepository);
    }

    @Test
    void 선택할_content가_없으면_entry를_만들지_않는다() {
        // given
        MaeilMailSubscriptionTrack track = createTrack(1L, 10L, 100L, MaeilMailTrack.BE, 0);
        MaeilMailTopic topic = createTopic(1000L, MaeilMailTrack.BE, "java", 1);
        IssueData issueData = createIssueData(
                Map.of(track.getId(), topic),
                Map.of(),
                Map.of(),
                Set.of(),
                50L
        );
        given(contentAssigner.assignContentIdOrRecycle(100L, topic.getId(), issueData.contentIdsByTopicId(),
                issueData.sentContentIdsByMemberTopic()))
                .willReturn(Optional.empty());

        // when
        PreparedIssueEntries result = entryPreparer.prepare(List.of(track), issueData);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.entries()).isEmpty();
            softly.assertThat(result.previouslyIssuedTrackIds()).isEmpty();
        });
        verifyNoInteractions(contentRepository);
    }

    @Test
    void 발행_topic이_없는_track은_건너뛴다() {
        // given
        MaeilMailSubscriptionTrack track = createTrack(1L, 10L, 100L, MaeilMailTrack.BE, 0);
        IssueData issueData = createIssueData(Map.of(), Map.of(), Map.of(), Set.of(), 50L);

        // when
        PreparedIssueEntries result = entryPreparer.prepare(List.of(track), issueData);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.entries()).isEmpty();
            softly.assertThat(result.previouslyIssuedTrackIds()).isEmpty();
        });
        verifyNoInteractions(contentAssigner, contentRepository);
    }

    @Test
    void 선택된_content가_조회되지_않으면_예외를_던진다() {
        // given
        MaeilMailSubscriptionTrack pendingTrack = createTrack(1L, 10L, 100L, MaeilMailTrack.BE, 0);
        MaeilMailSubscriptionTrack issuedTrack = createTrack(2L, 11L, 200L, MaeilMailTrack.BE, 0);
        MaeilMailTopic pendingTopic = createTopic(1000L, MaeilMailTrack.BE, "java", 1);
        MaeilMailTopic issuedTopic = createTopic(2000L, MaeilMailTrack.BE, "spring", 2);
        IssueData issueData = createIssueData(
                Map.of(pendingTrack.getId(), pendingTopic, issuedTrack.getId(), issuedTopic),
                Map.of(pendingTopic.getId(), List.of(9000L)),
                Map.of(),
                Set.of(new MemberTopicKey(200L, issuedTopic.getId())),
                50L
        );
        given(contentAssigner.assignContentIdOrRecycle(100L, pendingTopic.getId(), issueData.contentIdsByTopicId(),
                issueData.sentContentIdsByMemberTopic()))
                .willReturn(Optional.of(9000L));
        given(contentRepository.findAllById(List.of(9000L))).willReturn(List.of());

        // when & then
        assertThatThrownBy(() -> entryPreparer.prepare(List.of(pendingTrack, issuedTrack), issueData))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("매일메일 발행 content를 찾을 수 없습니다.")
                .hasMessageContaining("9000");
    }

    private IssueData createIssueData(
            Map<Long, MaeilMailTopic> issueTopicsByTrackId,
            Map<Long, List<Long>> contentIdsByTopicId,
            Map<MemberTopicKey, List<Long>> sentContentIdsByMemberTopic,
            Set<MemberTopicKey> issuedMemberTopicKeys,
            Long newsletterId
    ) {
        return new IssueData(
                issueTopicsByTrackId,
                contentIdsByTopicId,
                sentContentIdsByMemberTopic,
                issuedMemberTopicKeys,
                newsletterId
        );
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

    private MaeilMailContent createContent(
            Long id,
            Long topicId,
            String title,
            String contentBody,
            String contentsText,
            String contentsSummary,
            int expectedReadTime
    ) {
        MaeilMailContent content = MaeilMailContent.builder()
                .topicId(topicId)
                .title(title)
                .content(contentBody)
                .contentsText(contentsText)
                .contentsSummary(contentsSummary)
                .expectedReadTime(expectedReadTime)
                .build();
        ReflectionTestUtils.setField(content, "id", id);
        return content;
    }
}
