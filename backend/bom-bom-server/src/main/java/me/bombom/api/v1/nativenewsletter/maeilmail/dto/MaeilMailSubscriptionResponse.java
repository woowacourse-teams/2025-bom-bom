package me.bombom.api.v1.nativenewsletter.maeilmail.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailSubscriptionTrack;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailTrack;

public record MaeilMailSubscriptionResponse(

        @NotNull
        List<MaeilMailTrack> tracks
) {

    public static MaeilMailSubscriptionResponse from(List<MaeilMailSubscriptionTrack> tracks) {
        List<MaeilMailTrack> fields = tracks.stream()
                .map(MaeilMailSubscriptionTrack::getField)
                .toList();
        return new MaeilMailSubscriptionResponse(fields);
    }
}
