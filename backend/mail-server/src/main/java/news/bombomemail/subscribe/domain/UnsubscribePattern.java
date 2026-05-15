package news.bombomemail.subscribe.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import news.bombomemail.common.BaseEntity;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "pattern_key"))
public class UnsubscribePattern extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String patternKey;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String patternValue;

    @Builder
    public UnsubscribePattern(String patternKey, String patternValue) {
        this.patternKey = patternKey;
        this.patternValue = patternValue;
    }
}
