package me.bombom.api.v1.nativenewsletter.maeilmail.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailTrack;

public record MaeilMailUpdateSubscriptionRequest(

        @NotEmpty
        List<MaeilMailTrack> tracks
) {
}
