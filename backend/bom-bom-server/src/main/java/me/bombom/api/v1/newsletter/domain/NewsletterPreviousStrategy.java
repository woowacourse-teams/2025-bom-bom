package me.bombom.api.v1.newsletter.domain;

import lombok.Getter;

@Getter
public enum NewsletterPreviousStrategy {

    FIXED_WITH_RECENT,
    FIXED_ONLY,
    RECENT_ONLY,
    INACTIVE
}
