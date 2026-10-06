package news.bombom.inquiry.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InquiryRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long lastReadMessageIdByUser;

    @Builder
    private InquiryRoom(Long id, Long lastReadMessageIdByUser) {
        this.id = id;
        this.lastReadMessageIdByUser = lastReadMessageIdByUser;
    }

    public boolean hasRead(Long messageId) {
        return lastReadMessageIdByUser != null && lastReadMessageIdByUser >= messageId;
    }
}
