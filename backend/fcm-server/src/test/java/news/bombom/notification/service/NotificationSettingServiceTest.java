package news.bombom.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import news.bombom.notification.domain.MemberNotificationSetting;
import news.bombom.notification.domain.NotificationCategory;
import news.bombom.notification.dto.response.NotificationCategorySettingResponse;
import news.bombom.notification.repository.MemberNotificationSettingRepository;
import org.assertj.core.api.SoftAssertions;
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
    @DisplayName("회원 알림 설정 보장 - 기존 설정 존재")
    void ensureMemberNotificationSetting_ExistingSetting_ReturnsExisting() {
        // given
        MemberNotificationSetting existingSetting = createSetting(true, true);
        when(settingRepository.findByMemberId(TEST_MEMBER_ID))
                .thenReturn(Optional.of(existingSetting));

        // when
        MemberNotificationSetting result = notificationSettingService.ensureMemberNotificationSetting(TEST_MEMBER_ID);

        // then
        assertThat(result).isEqualTo(existingSetting);
        verify(settingRepository, never()).save(any());
    }

    @Test
    @DisplayName("회원 알림 설정 보장 - 설정 없음, 기본값 생성 및 저장")
    void ensureMemberNotificationSetting_NoSetting_CreateAndReturn() {
        // given
        when(settingRepository.findByMemberId(TEST_MEMBER_ID))
                .thenReturn(Optional.empty());
        when(settingRepository.save(any(MemberNotificationSetting.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        MemberNotificationSetting result = notificationSettingService
                .ensureMemberNotificationSetting(TEST_MEMBER_ID);

        // then
        assertThat(result.getMemberId()).isEqualTo(TEST_MEMBER_ID);
        assertThat(result.isArticleEnabled()).isTrue();
        assertThat(result.isEventEnabled()).isFalse();
        verify(settingRepository).save(any(MemberNotificationSetting.class));
    }

    @Test
    @DisplayName("카테고리 설정 업데이트 - 기존 설정 존재")
    void updateCategorySetting_ExistingSetting_Success() {
        // Given
        MemberNotificationSetting setting = createSetting(true, false);
        when(settingRepository.findByMemberId(TEST_MEMBER_ID))
                .thenReturn(Optional.of(setting));

        // When
        notificationSettingService.updateCategorySetting(TEST_MEMBER_ID, NotificationCategory.EVENT, true);

        // Then
        assertThat(setting.isEventEnabled()).isTrue();
    }

    @Test
    @DisplayName("카테고리 설정 업데이트 - 설정 없음, 새로 생성")
    void updateCategorySetting_NoSetting_CreateNew() {
        // Given
        when(settingRepository.findByMemberId(TEST_MEMBER_ID))
                .thenReturn(Optional.empty());
        when(settingRepository.save(any(MemberNotificationSetting.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        notificationSettingService.updateCategorySetting(TEST_MEMBER_ID, NotificationCategory.EVENT, true);

        // Then
        verify(settingRepository).save(any(MemberNotificationSetting.class));
    }

    @Test
    @DisplayName("모든 카테고리 설정 조회")
    void getCategorySettings_ReturnsAllSettings() {
        // Given
        MemberNotificationSetting setting = createSetting(true, false);
        when(settingRepository.findByMemberId(TEST_MEMBER_ID))
                .thenReturn(Optional.of(setting));

        // When
        List<NotificationCategorySettingResponse> result = notificationSettingService
                .getCategorySettings(TEST_MEMBER_ID);

        // Then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(result).hasSize(2);
            softly.assertThat(result).extracting(NotificationCategorySettingResponse::category)
                    .containsExactlyInAnyOrder(NotificationCategory.ARTICLE,
                            NotificationCategory.EVENT);
            softly.assertThat(result).filteredOn(r -> r.category() == NotificationCategory.ARTICLE)
                    .extracting(NotificationCategorySettingResponse::enabled)
                    .containsExactly(true);
            softly.assertThat(result).filteredOn(r -> r.category() == NotificationCategory.EVENT)
                    .extracting(NotificationCategorySettingResponse::enabled)
                    .containsExactly(false);
        });
    }

    @Test
    @DisplayName("특정 카테고리 설정 조회")
    void getCategorySetting_ReturnsSpecificSetting() {
        // Given
        MemberNotificationSetting setting = createSetting(true, false);
        when(settingRepository.findByMemberId(TEST_MEMBER_ID))
                .thenReturn(Optional.of(setting));

        // When
        NotificationCategorySettingResponse articleResponse = notificationSettingService.getCategorySetting(
                TEST_MEMBER_ID,
                NotificationCategory.ARTICLE);
        NotificationCategorySettingResponse eventResponse = notificationSettingService.getCategorySetting(
                TEST_MEMBER_ID,
                NotificationCategory.EVENT);

        // Then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(articleResponse.category()).isEqualTo(NotificationCategory.ARTICLE);
            softly.assertThat(articleResponse.enabled()).isTrue();
            softly.assertThat(eventResponse.category()).isEqualTo(NotificationCategory.EVENT);
            softly.assertThat(eventResponse.enabled()).isFalse();
        });
    }

    @Test
    @DisplayName("설정 없을 때 기본값 반환")
    void getCategorySettings_NoSetting_ReturnsDefaults() {
        // Given
        when(settingRepository.findByMemberId(TEST_MEMBER_ID))
                .thenReturn(Optional.empty());
        when(settingRepository.save(any(MemberNotificationSetting.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        List<NotificationCategorySettingResponse> result = notificationSettingService
                .getCategorySettings(TEST_MEMBER_ID);

        // Then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(result).hasSize(2);
            softly.assertThat(result).filteredOn(r -> r.category() == NotificationCategory.ARTICLE)
                    .extracting(NotificationCategorySettingResponse::enabled)
                    .containsExactly(true);
            softly.assertThat(result).filteredOn(r -> r.category() == NotificationCategory.EVENT)
                    .extracting(NotificationCategorySettingResponse::enabled)
                    .containsExactly(false);
        });
    }

    private MemberNotificationSetting createSetting(boolean articleEnabled, boolean eventEnabled) {
        return MemberNotificationSetting.builder()
                .memberId(TEST_MEMBER_ID)
                .articleEnabled(articleEnabled)
                .eventEnabled(eventEnabled)
                .build();
    }
}
