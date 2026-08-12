package me.bombom.api.v1.faq.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FaqCategory {

    MEMBER("회원"),
    NEWSLETTER("뉴스레터"),
    CHALLENGE("챌린지"),
    ETC("기타"),
    ;

    private final String value;
}
