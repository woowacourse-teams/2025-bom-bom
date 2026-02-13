package news.bombom.notification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import news.bombom.notification.common.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberNotificationSetting extends BaseEntity {

    @Id
    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean articleEnabled = true;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean eventEnabled = false;

    @Builder
    public MemberNotificationSetting(
            @NonNull Long memberId,
            boolean articleEnabled,
            boolean eventEnabled) {
        this.memberId = memberId;
        this.articleEnabled = articleEnabled;
        this.eventEnabled = eventEnabled;
    }

    public void updateArticleEnabled(boolean enabled) {
        this.articleEnabled = enabled;
    }

    public void updateEventEnabled(boolean enabled) {
        this.eventEnabled = enabled;
    }

    public void updateCategory(NotificationCategory category, boolean enabled) {
        switch (category) {
            case ARTICLE -> this.articleEnabled = enabled;
            case EVENT -> this.eventEnabled = enabled;
        }
    }

    public boolean isEnabledFor(NotificationCategory category) {
        return switch (category) {
            case ARTICLE -> articleEnabled;
            case EVENT -> eventEnabled;
        };
    }
}
