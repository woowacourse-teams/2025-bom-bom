package news.bombom.notification.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import news.bombom.notification.domain.MemberNotificationSetting;
import news.bombom.notification.domain.NotificationCategory;
import news.bombom.notification.dto.response.NotificationCategorySettingResponse;
import news.bombom.notification.repository.MemberNotificationSettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationSettingService {

    private final MemberNotificationSettingRepository settingRepository;

    @Transactional
    public void updateCategorySetting(Long memberId, NotificationCategory category, boolean enabled) {
        MemberNotificationSetting setting = settingRepository.findByMemberId(memberId)
                .orElseGet(() -> createAndSaveDefaultSetting(memberId));

        setting.updateCategory(category, enabled);
    }

    public List<NotificationCategorySettingResponse> getCategorySettings(Long memberId) {
        MemberNotificationSetting setting = settingRepository.findByMemberId(memberId)
                .orElseGet(() -> createDefaultSetting(memberId));

        return List.of(
                new NotificationCategorySettingResponse(NotificationCategory.ARTICLE, setting.isArticleEnabled()),
                new NotificationCategorySettingResponse(NotificationCategory.EVENT, setting.isEventEnabled()));
    }

    public boolean getCategorySetting(Long memberId, NotificationCategory category) {
        MemberNotificationSetting setting = settingRepository.findByMemberId(memberId)
                .orElseGet(() -> createDefaultSetting(memberId));

        return setting.isEnabledFor(category);
    }

    private MemberNotificationSetting createAndSaveDefaultSetting(Long memberId) {
        MemberNotificationSetting setting = createDefaultSetting(memberId);
        return settingRepository.save(setting);
    }

    private MemberNotificationSetting createDefaultSetting(Long memberId) {
        return MemberNotificationSetting.builder()
                .memberId(memberId)
                .articleEnabled(true)
                .eventEnabled(false)
                .build();
    }
}
