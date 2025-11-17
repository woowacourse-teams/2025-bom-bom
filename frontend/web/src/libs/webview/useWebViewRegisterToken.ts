import { useQueryClient } from '@tanstack/react-query';
import { useEffect } from 'react';
import { queries } from '@/apis/queries';
import { sendMessageToRN } from '@/libs/webview/webview.utils';
import { isWebView } from '@/utils/device';

export const useWebViewRegisterToken = () => {
  const queryClient = useQueryClient();

  useEffect(() => {
    if (!isWebView()) return;

    const data = queryClient.getQueryData(queries.userProfile().queryKey);
    const isLoggedIn = data && data.id;

    if (isLoggedIn) {
      sendMessageToRN({
        type: 'REGISTER_FCM_TOKEN_LOGGED_IN',
        payload: {
          memberId: data.id,
        },
      });
    }
  }, [queryClient]);
};
