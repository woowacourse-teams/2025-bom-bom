package me.bombom.api.v1.nativenewsletter.maeilmail.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailSubscriptionTrack;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.IssueChunkResult;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.IssueContext;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.IssueEntries;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.IssueEntrySelection;
import me.bombom.api.v1.nativenewsletter.maeilmail.repository.MaeilMailSubscriptionTrackRepository;
import me.bombom.api.v1.nativenewsletter.maeilmail.repository.MemberTopicKey;
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
    private final MaeilMailIssueContextLoader contextLoader;
    private final MaeilMailIssueEntrySelector entrySelector;
    private final MaeilMailIssueEntryAssembler entryAssembler;
    private final MaeilMailIssueResultRecorder issueResultRecorder;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public IssueChunkResult process(
            LocalDate issueDate,
            Long lastTrackId,
            Set<MemberTopicKey> issuedMemberTopicKeys,
            Pageable pageable
    ) {
        List<MaeilMailSubscriptionTrack> tracks = loadIssueTargetTracks(issueDate, lastTrackId, pageable);
        if (tracks.isEmpty()) {
            return IssueChunkResult.empty();
        }

        Long chunkLastTrackId = tracks.getLast().getId();
        IssueContext context = contextLoader.load(tracks);
        IssueEntrySelection selection = entrySelector.select(tracks, context, issuedMemberTopicKeys);
        IssueEntries entries = entryAssembler.assemble(selection, context);
        if (!entries.isEmpty()) {
            issueResultRecorder.record(entries, issueDate);
        }

        return IssueChunkResult.of(chunkLastTrackId, entries.memberTopicKeys());
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
