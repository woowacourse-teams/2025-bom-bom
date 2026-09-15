package me.bombom.api.v1.notice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.bombom.api.v1.common.BaseEntity;

/**
 * 대표 공지는 전체에서 최대 1건이므로 id를 1로 고정한 단일 행으로 관리한다.
 * 이 서비스에서는 조회 전용으로만 사용한다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoticeRepresentative extends BaseEntity {

    @Id
    private Byte id;

    @Column(nullable = false)
    private Long noticeId;
}
