package me.bombom.api.v1.notice.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NoticeCategory {

    NOTICE("공지"),
    UPDATE("업데이트"),
    EVENT("이벤트"),
    CHECK("점검"),
    ;

    private final String value;
}
