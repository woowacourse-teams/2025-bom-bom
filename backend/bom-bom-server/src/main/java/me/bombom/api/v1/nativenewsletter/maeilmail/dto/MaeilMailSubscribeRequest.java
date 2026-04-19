package me.bombom.api.v1.nativenewsletter.maeilmail.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailTrack;

public record MaeilMailSubscribeRequest(

        @NotNull
        Long newsletterId,

        @NotEmpty
        List<MaeilMailTrack> tracks
) {
}
