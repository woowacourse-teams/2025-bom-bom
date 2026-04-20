package me.bombom.api.v1.nativenewsletter.maeilmail.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailSubscriptionTrack;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailTrack;

public record MaeilMailSubscriptionResponse(

        @Schema(requiredMode = RequiredMode.REQUIRED)
        boolean subscribed,

        @NotNull
        List<MaeilMailTrack> tracks
) {

    public static MaeilMailSubscriptionResponse notSubscribed() {
        return new MaeilMailSubscriptionResponse(false, Collections.emptyList());
    }

    public static MaeilMailSubscriptionResponse subscribed(List<MaeilMailSubscriptionTrack> tracks) {
        return new MaeilMailSubscriptionResponse(true, tracks.stream()
                .map(MaeilMailSubscriptionTrack::getField)
                .toList());
    }
}
