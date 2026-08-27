package me.bombom.api.v1.faq.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FaqCategory {

    INTRODUCTION("서비스"),
    FEATURE("기능"),
    ACCOUNT("계정"),
    NEWSLETTER("뉴스레터"),
    ETC("기타"),
    ;

    private final String value;
}
