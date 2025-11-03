import { logger } from '@bombom/shared/utils';
import { useEffect } from 'react';
import { addWebViewMessageListener, sendMessageToRN } from './webview.utils';
import { useUserInfo } from '@/hooks/useUserInfo';
import { isWebView } from '@/utils/device';
import type { RNToWebMessage } from '@bombom/shared/webview';

export const useWebViewLoginStatus = () => {
  const { userInfo, isLoggedIn } = useUserInfo();

  useEffect(() => {
    if (!isWebView()) return;

    const cleanup = addWebViewMessageListener(
      async (message: RNToWebMessage) => {
        if (message.type === 'CHECK_LOGIN_STATUS') {
          try {
            sendMessageToRN({
              type: 'LOGIN_STATUS',
              payload: {
                isLoggedIn,
                memberId: userInfo?.id,
              },
            });
          } catch (error) {
            logger.error('로그인 상태 확인 실패:', error);
            sendMessageToRN({
              type: 'LOGIN_STATUS',
              payload: {
                isLoggedIn: false,
              },
            });
          }
        }
      },
    );

    return cleanup;
  }, [isLoggedIn, userInfo]);
};
