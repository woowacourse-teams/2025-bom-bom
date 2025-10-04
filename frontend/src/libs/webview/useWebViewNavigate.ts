import { useRouter } from '@tanstack/react-router';
import { useEffect } from 'react';
import { addWebViewMessageListener, isWebView } from './webview.utils';
import type { RNToWebMessage } from './webview.types';

export const useWebViewNavigate = () => {
  const router = useRouter();

  useEffect(() => {
    if (!isWebView()) return;

    const cleanup = addWebViewMessageListener(
      async (message: RNToWebMessage) => {
        switch (message.type) {
          case 'ANDROID_BACK_BUTTON_CLICKED':
            router.history.back();
            break;
        }
      },
    );

    return cleanup;
  }, [router]);
};
