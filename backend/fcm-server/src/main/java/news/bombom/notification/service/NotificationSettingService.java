package news.bombom.notification.service;


import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import news.bombom.notification.domain.MemberNotificationSetting;
import news.bombom.notification.domain.NotificationCategory;
import news.bombom.notification.dto.response.NotificationCategorySettingResponse;
import news.bombom.notification.repository.MemberNotificationSettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NotificationSettingService {

    private final MemberNotificationSettingRepository settingRepository;
    private final NotificationTokenService tokenService;
    private final FcmTopicService fcmTopicService;

    @Transactional
    public List<MemberNotificationSetting> ensureMemberNotificationSetting(Long memberId) {
        List<MemberNotificationSetting> settings = new ArrayList<>(settingRepository.findAllByMemberId(memberId));
        Set<NotificationCategory> missingCategories = getMissingCategories(settings);

        if (missingCategories.isEmpty()) {
            return settings;
        }

        List<MemberNotificationSetting> newSettings = missingCategories.stream()
                .map(category -> createDefaultSetting(memberId, category))
                .toList();

        settings.addAll(settingRepository.saveAll(newSettings));
        return settings;
    }

    @Transactional
    public void updateCategorySetting(Long memberId, NotificationCategory category, boolean enabled) {
        MemberNotificationSetting setting = settingRepository.findByMemberIdAndCategory(memberId, category)
                .orElseGet(() -> createDefaultSetting(memberId, category));
        setting.updateEnabled(enabled);
        settingRepository.save(setting);

        if (category.isUseTopic()) {
            List<String> tokens = tokenService.getTokenStrings(memberId);
            fcmTopicService.updateSubscription(memberId, category, enabled, tokens);
        }
    }

    @Transactional
    public List<NotificationCategorySettingResponse> getCategorySettings(Long memberId) {
        List<MemberNotificationSetting> settings = ensureMemberNotificationSetting(memberId);
        return NotificationCategorySettingResponse.from(settings);
    }

    @Transactional
    public NotificationCategorySettingResponse getCategorySetting(Long memberId, NotificationCategory category) {
        List<MemberNotificationSetting> settings = ensureMemberNotificationSetting(memberId);
        return settings.stream()
                .filter(setting -> setting.getCategory() == category)
                .findFirst()
                .map(NotificationCategorySettingResponse::from)
                .orElseThrow(() -> new IllegalStateException("카테고리 설정이 존재해야 합니다. memberId=" + memberId + ", category=" + category));
    }

    public boolean isEnabled(Long memberId, NotificationCategory category) {
        return settingRepository.findByMemberIdAndCategory(memberId, category)
                .map(MemberNotificationSetting::isEnabled)
                .orElse(category.getDefaultSetting());
    }

    private MemberNotificationSetting createDefaultSetting(Long memberId, NotificationCategory category) {
        return MemberNotificationSetting.builder()
                .memberId(memberId)
                .category(category)
                .isEnabled(category.getDefaultSetting())
                .build();
    }

    private Set<NotificationCategory> getMissingCategories(List<MemberNotificationSetting> existing) {
        Set<NotificationCategory> missing = EnumSet.allOf(NotificationCategory.class);
        existing.forEach(setting -> missing.remove(setting.getCategory()));
        return missing;
    }
}
