package news.bombom.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import news.bombom.notification.domain.MemberNotificationSetting;
import news.bombom.notification.domain.NotificationCategory;
import news.bombom.notification.dto.response.NotificationCategorySettingResponse;
import news.bombom.notification.repository.MemberNotificationSettingRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationSettingServiceTest {

    @Mock
    private MemberNotificationSettingRepository settingRepository;

    @InjectMocks
    private NotificationSettingService notificationSettingService;

    private final Long TEST_MEMBER_ID = 1L;

    @Test
    @DisplayName("회원 알림 설정 보장 - 모든 카테고리에 대해 행이 없으면 한꺼번에 생성한다")
    void ensureMemberNotificationSetting_NoSettings_CreatesAll() {
        // given
        when(settingRepository.findAllByMemberId(TEST_MEMBER_ID)).thenReturn(List.of());

        // when
        notificationSettingService.ensureMemberNotificationSetting(TEST_MEMBER_ID);

        // then
        verify(settingRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("카테고리 설정 업데이트 - 기존 설정 존재 시 해당 행만 업데이트")
    void updateCategorySetting_ExistingSetting_UpdatesIt() {
        // Given
        MemberNotificationSetting setting = createSetting(NotificationCategory.ARTICLE, true);
        when(settingRepository.findByMemberIdAndCategory(TEST_MEMBER_ID, NotificationCategory.ARTICLE))
                .thenReturn(Optional.of(setting));

        // When
        notificationSettingService.updateCategorySetting(TEST_MEMBER_ID, NotificationCategory.ARTICLE, false);

        // Then
        assertThat(setting.isEnabled()).isFalse();
        verify(settingRepository).save(setting);
    }

    @Test
    @DisplayName("모든 카테고리 설정 조회 - 조회 시 설정을 보장(ensure)한다")
    void getCategorySettings_EnsuresAndReturnsAll() {
        // Given
        List<MemberNotificationSetting> settings = List.of(
                createSetting(NotificationCategory.ARTICLE, true),
                createSetting(NotificationCategory.EVENT, false));
        when(settingRepository.findAllByMemberId(TEST_MEMBER_ID)).thenReturn(settings);

        // When
        List<NotificationCategorySettingResponse> result = notificationSettingService
                .getCategorySettings(TEST_MEMBER_ID);

        // Then
        assertThat(result).hasSize(2);
        verify(settingRepository, times(1)).findAllByMemberId(TEST_MEMBER_ID);
    }

    @Test
    @DisplayName("설정 활성화 여부 확인 - 설정 행이 없으면 기본값을 반환한다")
    void isEnabled_NoSetting_ReturnsDefault() {
        // Given
        when(settingRepository.findByMemberIdAndCategory(TEST_MEMBER_ID, NotificationCategory.EVENT))
                .thenReturn(Optional.empty());

        // When
        boolean result = notificationSettingService.isEnabled(TEST_MEMBER_ID, NotificationCategory.EVENT);

        // Then
        assertThat(result).isEqualTo(NotificationCategory.EVENT.getDefaultSetting());
    }

    private MemberNotificationSetting createSetting(NotificationCategory category, boolean isEnabled) {
        return MemberNotificationSetting.builder()
                .memberId(TEST_MEMBER_ID)
                .category(category)
                .isEnabled(isEnabled)
                .build();
    }
}
