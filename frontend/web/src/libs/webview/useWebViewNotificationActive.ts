import { useEffect } from 'react';
import { useWebViewDeviceUuid } from './useWebViewDeviceUuid';
import { addWebViewMessageListener } from './webview.utils';
import { useUserInfo } from '@/hooks/useUserInfo';
import useNotificationMutation from '@/pages/MyPage/useNotificationMutation';
import { isWebView } from '@/utils/device';
import type { RNToWebMessage } from '@bombom/shared/webview';

export const useWebViewNotificationActive = () => {
  const { userInfo } = useUserInfo();
  const deviceUuid = useWebViewDeviceUuid();

  const { mutate: updateNotificationSettings } = useNotificationMutation({
    memberId: userInfo?.id ?? 0,
    deviceUuid,
  });

  useEffect(() => {
    if (!isWebView()) return;
    if (!userInfo?.id || deviceUuid.length === 0) return;

    const cleanup = addWebViewMessageListener((message: RNToWebMessage) => {
      if (message.type === 'REQUEST_NOTIFICATION_ACTIVE') {
        updateNotificationSettings(true);
      }
    });

    return cleanup;
  }, [deviceUuid, updateNotificationSettings, userInfo?.id]);
};
