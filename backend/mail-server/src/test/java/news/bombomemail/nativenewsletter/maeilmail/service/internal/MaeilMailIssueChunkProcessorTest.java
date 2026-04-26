package news.bombomemail.nativenewsletter.maeilmail.service.internal;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.LocalDate;
import java.util.List;
import news.bombomemail.article.domain.Article;
import news.bombomemail.nativenewsletter.maeilmail.domain.MaeilMailSentContent;
import news.bombomemail.nativenewsletter.maeilmail.domain.MaeilMailSubscriptionTrack;
import news.bombomemail.nativenewsletter.maeilmail.domain.MaeilMailTrack;
import news.bombomemail.nativenewsletter.maeilmail.dto.IssueChunkResult;
import news.bombomemail.nativenewsletter.maeilmail.dto.IssueData;
import news.bombomemail.nativenewsletter.maeilmail.dto.IssueEntry;
import news.bombomemail.nativenewsletter.maeilmail.dto.PreparedIssueEntries;
import news.bombomemail.nativenewsletter.maeilmail.repository.MaeilMailSubscriptionTrackRepository;
import news.bombomemail.newsletter.domain.NewsletterPublicationStatus;
import news.bombomemail.newsletter.domain.NewsletterSource;
import news.bombomemail.subscribe.domain.SubscribeStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MaeilMailIssueChunkProcessorTest {

    @Mock
    private MaeilMailSubscriptionTrackRepository trackRepository;

    @Mock
    private MaeilMailIssueDataLoader issueDataLoader;

    @Mock
    private MaeilMailIssueEntryPreparer entryPreparer;

    @Mock
    private MaeilMailIssuePublisher issuePublisher;

    @Mock
    private MaeilMailIssueJobManager issueJobManager;

    private MaeilMailIssueChunkProcessor chunkProcessor;

    @BeforeEach
    void setup() {
        chunkProcessor = new MaeilMailIssueChunkProcessor(
                trackRepository,
                issueDataLoader,
                entryPreparer,
                issuePublisher,
                issueJobManager
        );
    }

    @Test
    void 발행대상_track이_없으면_empty_result를_반환한다() {
        // given
        LocalDate issueDate = LocalDate.of(2026, 4, 27);
        PageRequest pageRequest = PageRequest.of(0, 200);
        given(trackRepository.findIssueTargetsAfterId(
                issueDate,
                SubscribeStatus.SUBSCRIBED,
                NewsletterSource.MAEIL_MAIL,
                NewsletterPublicationStatus.ACTIVE,
                0L,
                pageRequest
        )).willReturn(List.of());

        // when
        IssueChunkResult result = chunkProcessor.process(1L, issueDate, 0L, pageRequest);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.hasTracks()).isFalse();
            softly.assertThat(result.trackCount()).isZero();
            softly.assertThat(result.issuedArticleCount()).isZero();
            softly.assertThat(result.previouslyIssuedTrackCount()).isZero();
        });
        verifyNoInteractions(issueDataLoader, entryPreparer, issuePublisher, issueJobManager);
    }

    @Test
    void 발행대상_track을_로딩하고_선택_조립_기록까지_처리한다() {
        // given
        LocalDate issueDate = LocalDate.of(2026, 4, 27);
        PageRequest pageRequest = PageRequest.of(0, 200);
        MaeilMailSubscriptionTrack firstTrack = createTrack(1L, 10L, 100L);
        MaeilMailSubscriptionTrack secondTrack = createTrack(2L, 11L, 200L);
        List<MaeilMailSubscriptionTrack> tracks = List.of(firstTrack, secondTrack);
        IssueData issueData = new IssueData(
                java.util.Map.of(),
                java.util.Map.of(),
                java.util.Map.of(),
                java.util.Set.of(),
                50L
        );
        PreparedIssueEntries preparedEntries = new PreparedIssueEntries(
                List.of(new IssueEntry(
                        Article.builder()
                                .title("title")
                                .contents("contents")
                                .contentsText("contentsText")
                                .contentsSummary("summary")
                                .memberId(100L)
                                .newsletterId(50L)
                                .arrivedDateTime(java.time.LocalDateTime.of(2026, 4, 27, 7, 0))
                                .build(),
                        List.of(1L),
                        MaeilMailSentContent.builder()
                                .memberId(100L)
                                .topicId(1000L)
                                .contentId(9000L)
                                .build()
                )),
                List.of(2L)
        );

        given(trackRepository.findIssueTargetsAfterId(
                issueDate,
                SubscribeStatus.SUBSCRIBED,
                NewsletterSource.MAEIL_MAIL,
                NewsletterPublicationStatus.ACTIVE,
                0L,
                pageRequest
        )).willReturn(tracks);
        given(issueDataLoader.load(issueDate, tracks)).willReturn(issueData);
        given(entryPreparer.prepare(tracks, issueData)).willReturn(preparedEntries);

        // when
        IssueChunkResult result = chunkProcessor.process(1L, issueDate, 0L, pageRequest);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.hasTracks()).isTrue();
            softly.assertThat(result.lastTrackId()).isEqualTo(2L);
            softly.assertThat(result.trackCount()).isEqualTo(2);
            softly.assertThat(result.issuedArticleCount()).isEqualTo(1);
            softly.assertThat(result.previouslyIssuedTrackCount()).isEqualTo(1);
        });
        verify(issuePublisher).publish(preparedEntries, issueDate);
        verify(issueJobManager).recordChunk(1L, result);
    }

    private MaeilMailSubscriptionTrack createTrack(Long id, Long subscribeId, Long memberId) {
        MaeilMailSubscriptionTrack track = MaeilMailSubscriptionTrack.builder()
                .subscribeId(subscribeId)
                .memberId(memberId)
                .field(MaeilMailTrack.BE)
                .build();
        ReflectionTestUtils.setField(track, "id", id);
        return track;
    }
}
