package news.bombomemail.article.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReceivedMailId implements Serializable {

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false, columnDefinition = "BINARY(32)")
    private byte[] messageIdHash;
}
