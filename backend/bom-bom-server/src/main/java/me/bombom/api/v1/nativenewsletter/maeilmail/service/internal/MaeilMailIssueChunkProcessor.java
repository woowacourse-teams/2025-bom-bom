package me.bombom.api.v1.nativenewsletter.maeilmail.service.internal;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailSubscriptionTrack;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.IssueChunkResult;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.IssueData;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.IssueEntrySelectionResult;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.PreparedIssueEntries;
import me.bombom.api.v1.nativenewsletter.maeilmail.repository.MaeilMailSubscriptionTrackRepository;
import me.bombom.api.v1.newsletter.domain.NewsletterPublicationStatus;
import me.bombom.api.v1.newsletter.domain.NewsletterSource;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class MaeilMailIssueChunkProcessor {

    private final MaeilMailSubscriptionTrackRepository trackRepository;
    private final MaeilMailIssueDataLoader issueDataLoader;
    private final MaeilMailIssueEntrySelector entrySelector;
    private final MaeilMailIssueEntryAssembler entryAssembler;
    private final MaeilMailIssueResultRecorder issueResultRecorder;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public IssueChunkResult process(
            LocalDate issueDate,
            Long lastTrackId,
            Pageable pageable
    ) {
        List<MaeilMailSubscriptionTrack> tracks = loadIssueTargetTracks(issueDate, lastTrackId, pageable);
        if (tracks.isEmpty()) {
            return IssueChunkResult.empty();
        }

        Long chunkLastTrackId = tracks.getLast().getId();
        IssueData issueData = issueDataLoader.load(issueDate, tracks);
        IssueEntrySelectionResult selection = entrySelector.select(tracks, issueData);
        PreparedIssueEntries preparedEntries = entryAssembler.assemble(selection);
        issueResultRecorder.record(preparedEntries, issueDate);
        return IssueChunkResult.of(chunkLastTrackId);
    }

    private List<MaeilMailSubscriptionTrack> loadIssueTargetTracks(
            LocalDate issueDate,
            Long lastTrackId,
            Pageable pageable
    ) {
        return trackRepository.findSubscribedTracksByNewsletterSourceNotIssuedOnAfterId(
                issueDate,
                NewsletterSource.MAEIL_MAIL,
                NewsletterPublicationStatus.ACTIVE,
                lastTrackId,
                pageable
        );
    }
}
