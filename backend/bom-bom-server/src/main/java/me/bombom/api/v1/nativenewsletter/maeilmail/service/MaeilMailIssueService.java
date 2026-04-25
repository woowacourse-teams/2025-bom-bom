package me.bombom.api.v1.nativenewsletter.maeilmail.service;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.IssueChunkResult;
import me.bombom.api.v1.nativenewsletter.maeilmail.service.internal.MaeilMailIssueChunkProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MaeilMailIssueService {

    private static final Long START_TRACK_ID = 0L;

    private final Clock clock;
    private final MaeilMailIssueChunkProcessor chunkProcessor;

    @Value("${maeil-mail.issue.chunk-size:200}")
    private int issueChunkSize;

    public void issue() {
        LocalDate today = LocalDate.now(clock);
        if (isWeekend(today)) {
            return;
        }

        Long lastTrackId = START_TRACK_ID;
        while (true) {
            IssueChunkResult result = chunkProcessor.process(
                    today,
                    lastTrackId,
                    issuePageRequest()
            );
            if (!result.hasTracks()) {
                return;
            }

            lastTrackId = result.lastTrackId();
        }
    }

    private PageRequest issuePageRequest() {
        return PageRequest.of(0, Math.max(issueChunkSize, 1));
    }

    private boolean isWeekend(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }
}
