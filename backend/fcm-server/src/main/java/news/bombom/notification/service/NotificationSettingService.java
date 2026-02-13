package news.bombom.notification.service;

import java.util.Arrays;
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
    public void ensureMemberNotificationSetting(Long memberId) {
        Arrays.stream(NotificationCategory.values())
                .forEach(category -> ensureCategorySetting(memberId, category));
    }

    @Transactional
    public void updateCategorySetting(Long memberId, NotificationCategory category, boolean enabled) {
        MemberNotificationSetting setting = settingRepository.findByMemberIdAndCategory(memberId, category)
                .orElseGet(() -> MemberNotificationSetting.builder()
                        .memberId(memberId)
                        .category(category)
                        .isEnabled(enabled)
                        .build());

        setting.updateEnabled(enabled);
        settingRepository.save(setting);
    }

    public List<NotificationCategorySettingResponse> getCategorySettings(Long memberId) {
        ensureMemberNotificationSetting(memberId);
        List<MemberNotificationSetting> settings = settingRepository.findAllByMemberId(memberId);
        return NotificationCategorySettingResponse.from(settings);
    }

    public NotificationCategorySettingResponse getCategorySetting(Long memberId, NotificationCategory category) {
        MemberNotificationSetting setting = settingRepository.findByMemberIdAndCategory(memberId, category)
                .orElseGet(() -> {
                    ensureCategorySetting(memberId, category);
                    return settingRepository.findByMemberIdAndCategory(memberId, category).orElseThrow();
                });
        return NotificationCategorySettingResponse.from(setting);
    }

    public boolean isEnabled(Long memberId, NotificationCategory category) {
        return settingRepository.findByMemberIdAndCategory(memberId, category)
                .map(MemberNotificationSetting::isEnabled)
                .orElse(category.isDefaultSetting());
    }

    private void ensureCategorySetting(Long memberId, NotificationCategory category) {
        if (!settingRepository.existsByMemberIdAndCategory(memberId, category)) {
            MemberNotificationSetting setting = MemberNotificationSetting.builder()
                    .memberId(memberId)
                    .category(category)
                    .isEnabled(category.isDefaultSetting())
                    .build();
            settingRepository.save(setting);
        }
    }
}
