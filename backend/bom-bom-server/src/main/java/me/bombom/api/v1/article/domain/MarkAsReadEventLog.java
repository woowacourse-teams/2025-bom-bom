package me.bombom.api.v1.article.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import java.io.Serializable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.bombom.api.v1.common.BaseEntity;

@Getter
@Entity
@IdClass(MarkAsReadEventLog.Pk.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MarkAsReadEventLog extends BaseEntity {

    @Id
    private Long memberId;

    @Id
    private Long articleId;

    @Getter
    @EqualsAndHashCode
    @NoArgsConstructor
    public static class Pk implements Serializable {
        private Long memberId;
        private Long articleId;
    }
}
