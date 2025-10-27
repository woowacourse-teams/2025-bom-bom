import { useCallback } from 'react';
import { Alert, Linking } from 'react-native';
import { putNotificationSettings } from '@/apis/notification';
import { getDeviceUUID } from '@/utils/device';
import { useWebView } from '@/contexts/WebViewContext';
import { checkNotificationPermission } from '@/utils/notification';

export const useNotificationSettings = () => {
  const { sendMessageToWeb } = useWebView();

  const sendNotificationStatusToWeb = useCallback(
    async (memberId: number | undefined) => {
      if (!memberId) return;

      try {
        const deviceUuid = await getDeviceUUID();
        if (!deviceUuid) return;

        const hasPermission = await checkNotificationPermission();

        sendMessageToWeb({
          type: 'NOTIFICATION_STATUS',
          payload: {
            enabled: hasPermission,
          },
        });
      } catch (error) {
        console.error('알림 상태 조회 실패:', error);
        sendMessageToWeb({
          type: 'NOTIFICATION_STATUS',
          payload: {
            enabled: false,
          },
        });
      }
    },
    [sendMessageToWeb],
  );

  const handleToggleNotification = useCallback(
    async (memberId: number | undefined, enabled: boolean) => {
      if (!memberId) {
        Alert.alert('오류', '로그인이 필요합니다.');
        return;
      }

      try {
        const hasPermission = await checkNotificationPermission();
        if (enabled && !hasPermission) {
          Alert.alert(
            '알림 권한 필요',
            '알림을 받으려면 시스템 설정에서 알림 권한을 허용해주세요.',
            [
              { text: '취소', style: 'cancel' },
              {
                text: '설정 열기',
                onPress: () => {
                  Linking.openSettings();
                },
              },
            ],
          );
          sendMessageToWeb({
            type: 'NOTIFICATION_STATUS',
            payload: {
              enabled: false,
            },
          });
          return;
        }

        const deviceUuid = await getDeviceUUID();
        if (!deviceUuid) {
          throw new Error('Device UUID를 가져올 수 없습니다.');
        }

        await putNotificationSettings({
          memberId,
          deviceUuid,
          enabled,
        });

        sendMessageToWeb({
          type: 'NOTIFICATION_STATUS',
          payload: {
            enabled: enabled && hasPermission,
          },
        });
      } catch (error) {
        console.error('알림 설정 변경 실패:', error);
        Alert.alert('오류', '알림 설정을 변경하는데 실패했습니다.');
        sendNotificationStatusToWeb(memberId);
      }
    },
    [sendMessageToWeb, sendNotificationStatusToWeb],
  );

  return {
    sendNotificationStatusToWeb,
    handleToggleNotification,
  };
};
