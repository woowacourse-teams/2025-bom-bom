package news.bombomemail.article.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 같은 회원에게 같은 Message-ID 메일이 두 번 저장되지 않게 하는 기록. 스키마는 메인 서버 Flyway(V39.1.5)가 관리한다.
 * 저장은 ReceivedMailRepository#insert 로만 한다. JPA save 는 같은 키가 있으면 merge 로 덮어써서 중복을 잡지 못한다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReceivedMail {

    @EmbeddedId
    private ReceivedMailId id;

    @Column(nullable = false)
    private Long articleId;

    @Column(nullable = false, columnDefinition = "DATETIME(6)")
    private LocalDateTime createdAt;
}
