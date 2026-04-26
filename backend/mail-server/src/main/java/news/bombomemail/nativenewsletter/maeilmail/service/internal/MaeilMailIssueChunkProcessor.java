package news.bombomemail.nativenewsletter.maeilmail.service.internal;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import news.bombomemail.nativenewsletter.maeilmail.domain.MaeilMailSubscriptionTrack;
import news.bombomemail.nativenewsletter.maeilmail.dto.IssueChunkResult;
import news.bombomemail.nativenewsletter.maeilmail.dto.IssueData;
import news.bombomemail.nativenewsletter.maeilmail.dto.PreparedIssueEntries;
import news.bombomemail.nativenewsletter.maeilmail.repository.MaeilMailSubscriptionTrackRepository;
import news.bombomemail.newsletter.domain.NewsletterPublicationStatus;
import news.bombomemail.newsletter.domain.NewsletterSource;
import news.bombomemail.subscribe.domain.SubscribeStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class MaeilMailIssueChunkProcessor {

    private final MaeilMailSubscriptionTrackRepository trackRepository;
    private final MaeilMailIssueDataLoader issueDataLoader;
    private final MaeilMailIssueEntryPreparer entryPreparer;
    private final MaeilMailIssuePublisher issuePublisher;
    private final MaeilMailIssueJobManager issueJobManager;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public IssueChunkResult process(
            Long issueJobId,
            LocalDate issueDate,
            Long lastTrackId,
            Pageable pageable
    ) {
        long startedAt = System.currentTimeMillis();
        List<MaeilMailSubscriptionTrack> tracks = loadIssueTargetTracks(issueDate, lastTrackId, pageable);
        if (tracks.isEmpty()) {
            return IssueChunkResult.empty();
        }

        Long chunkLastTrackId = tracks.getLast().getId();
        IssueData issueData = issueDataLoader.load(issueDate, tracks);
        PreparedIssueEntries preparedEntries = entryPreparer.prepare(tracks, issueData);
        issuePublisher.publish(preparedEntries, issueDate);
        IssueChunkResult result = IssueChunkResult.of(
                chunkLastTrackId,
                tracks.size(),
                preparedEntries.entries().size(),
                preparedEntries.previouslyIssuedTrackIds().size()
        );
        issueJobManager.recordChunk(issueJobId, result);
        log.info(
                "매일메일 발행 chunk 처리 완료 - issueDate={}, issueJobId={}, lastTrackId={}, trackCount={}, issuedArticleCount={}, previouslyIssuedTrackCount={}, elapsedMs={}",
                issueDate,
                issueJobId,
                chunkLastTrackId,
                result.trackCount(),
                result.issuedArticleCount(),
                result.previouslyIssuedTrackCount(),
                System.currentTimeMillis() - startedAt
        );
        return result;
    }

    private List<MaeilMailSubscriptionTrack> loadIssueTargetTracks(
            LocalDate issueDate,
            Long lastTrackId,
            Pageable pageable
    ) {
        return trackRepository.findIssueTargetsAfterId(
                issueDate,
                SubscribeStatus.SUBSCRIBED,
                NewsletterSource.MAEIL_MAIL,
                NewsletterPublicationStatus.ACTIVE,
                lastTrackId,
                pageable
        );
    }
}
