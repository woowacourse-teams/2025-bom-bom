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
    public MemberNotificationSetting ensureMemberNotificationSetting(Long memberId) {
        return settingRepository.findByMemberId(memberId)
                .orElseGet(() -> createDefaultSetting(memberId));
    }

    @Transactional
    public MemberNotificationSetting createDefaultSetting(Long memberId) {
        MemberNotificationSetting memberNotificationSetting = MemberNotificationSetting.builder()
                .memberId(memberId)
                .articleEnabled(true)
                .eventEnabled(false)
                .build();
        return settingRepository.save(memberNotificationSetting);
    }

    @Transactional
    public void updateCategorySetting(Long memberId, NotificationCategory category, boolean enabled) {
        MemberNotificationSetting setting = settingRepository.findByMemberId(memberId)
                .orElseGet(() -> createDefaultSetting(memberId));
        setting.updateCategory(category, enabled);
    }

    public List<NotificationCategorySettingResponse> getCategorySettings(Long memberId) {
        MemberNotificationSetting setting = settingRepository.findByMemberId(memberId)
                .orElseGet(() -> createDefaultSetting(memberId));
        return NotificationCategorySettingResponse.from(setting);
    }

    public NotificationCategorySettingResponse getCategorySetting(Long memberId, NotificationCategory category) {
        MemberNotificationSetting setting = settingRepository.findByMemberId(memberId)
                .orElseGet(() -> createDefaultSetting(memberId));
        return NotificationCategorySettingResponse.from(setting, category);
    }
}
