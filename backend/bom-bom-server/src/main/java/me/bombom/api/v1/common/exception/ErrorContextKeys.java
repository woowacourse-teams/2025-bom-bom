package me.bombom.api.v1.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * 예외 컨텍스트에서 자주 사용되는 핵심 키들을 정의하는 enum
 * 
 * 가이드라인:
 * - 여러 서비스에서 공통으로 사용되는 키들만 enum으로 관리
 * - 특정 상황에서만 사용되는 키들은 String을 직접 사용
 * - enum과 String 키를 혼용할 수 있음
 */
@Getter
@ToString
@RequiredArgsConstructor
public enum ErrorContextKeys {
    MEMBER_ID("memberId"),
    ARTICLE_ID("articleId"),
    NEWSLETTER_ID("newsletterId"),
    ENTITY_TYPE("entityType"),
    ACTUAL_OWNER_ID("actualOwnerId"),
    OPERATION("operation"),
    HIGHLIGHT_ID("highlightId");
    
    private final String key;
}
