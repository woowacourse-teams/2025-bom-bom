package me.bombom.api.v1.newsletter.domain;

import lombok.Getter;

@Getter
public enum NewsletterPreviousStrategy {

    FIXED_WITH_LATEST,
    FIXED_ONLY,
    LATEST_ONLY,
    NONE
}
