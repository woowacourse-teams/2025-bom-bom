package me.bombom.api.v1.nativenewsletter.maeilmail.service.internal;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailSubscriptionTrack;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailTrack;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.IssueChunkResult;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.IssueData;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.IssueEntrySelectionResult;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.PreparedIssueEntries;
import me.bombom.api.v1.nativenewsletter.maeilmail.repository.MaeilMailSubscriptionTrackRepository;
import me.bombom.api.v1.newsletter.domain.NewsletterPublicationStatus;
import me.bombom.api.v1.newsletter.domain.NewsletterSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MaeilMailIssueChunkProcessorTest {

    private static final LocalDate ISSUE_DATE = LocalDate.of(2026, 4, 24);
    private static final PageRequest PAGE_REQUEST = PageRequest.of(0, 10);

    @Mock
    private MaeilMailSubscriptionTrackRepository trackRepository;

    @Mock
    private MaeilMailIssueDataLoader issueDataLoader;

    @Mock
    private MaeilMailIssueEntrySelector entrySelector;

    @Mock
    private MaeilMailIssueEntryAssembler entryAssembler;

    @Mock
    private MaeilMailIssueResultRecorder issueResultRecorder;

    @InjectMocks
    private MaeilMailIssueChunkProcessor chunkProcessor;

    @Test
    void 발행_대상_track이_없으면_empty_result를_반환한다() {
        given(trackRepository.findSubscribedTracksByNewsletterSourceNotIssuedOnAfterId(
                ISSUE_DATE,
                NewsletterSource.MAEIL_MAIL,
                NewsletterPublicationStatus.ACTIVE,
                0L,
                PAGE_REQUEST
        )).willReturn(List.of());

        IssueChunkResult result = chunkProcessor.process(ISSUE_DATE, 0L, PAGE_REQUEST);

        assertSoftly(softly -> {
            softly.assertThat(result.hasTracks()).isFalse();
            softly.assertThat(result.lastTrackId()).isNull();
        });
        verifyNoInteractions(issueDataLoader, entrySelector, entryAssembler, issueResultRecorder);
    }

    @Test
    void 발행_대상_track이_있으면_데이터_로드부터_기록까지_처리하고_마지막_track_id를_반환한다() {
        MaeilMailSubscriptionTrack firstTrack = createTrack(1L);
        MaeilMailSubscriptionTrack secondTrack = createTrack(2L);
        IssueData issueData = new IssueData(Map.of(), Map.of(), Map.of(), Set.of(), 500L);
        IssueEntrySelectionResult selection = new IssueEntrySelectionResult(List.of(), List.of());
        PreparedIssueEntries preparedEntries = new PreparedIssueEntries(List.of(), List.of());
        given(trackRepository.findSubscribedTracksByNewsletterSourceNotIssuedOnAfterId(
                ISSUE_DATE,
                NewsletterSource.MAEIL_MAIL,
                NewsletterPublicationStatus.ACTIVE,
                0L,
                PAGE_REQUEST
        )).willReturn(List.of(firstTrack, secondTrack));
        given(issueDataLoader.load(ISSUE_DATE, List.of(firstTrack, secondTrack))).willReturn(issueData);
        given(entrySelector.select(List.of(firstTrack, secondTrack), issueData)).willReturn(selection);
        given(entryAssembler.assemble(selection)).willReturn(preparedEntries);

        IssueChunkResult result = chunkProcessor.process(ISSUE_DATE, 0L, PAGE_REQUEST);

        assertSoftly(softly -> {
            softly.assertThat(result.hasTracks()).isTrue();
            softly.assertThat(result.lastTrackId()).isEqualTo(2L);
        });
        verify(issueResultRecorder).record(preparedEntries, ISSUE_DATE);
    }

    private MaeilMailSubscriptionTrack createTrack(Long id) {
        MaeilMailSubscriptionTrack track = MaeilMailSubscriptionTrack.builder()
                .subscribeId(id + 100L)
                .memberId(id + 200L)
                .field(MaeilMailTrack.BE)
                .build();
        ReflectionTestUtils.setField(track, "id", id);
        return track;
    }
}
