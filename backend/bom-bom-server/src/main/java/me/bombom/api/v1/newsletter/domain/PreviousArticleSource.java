package me.bombom.api.v1.newsletter.domain;

public enum PreviousArticleSource {

    /**
     * 직접 지정한 지난 아티클 (previous_article 테이블)
     */
    FIXED,

    /**
     * 최근 발행된 아티클 (article 테이블)
     */
    LATEST
}
