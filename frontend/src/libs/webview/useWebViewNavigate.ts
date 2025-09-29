import { useNavigate } from '@tanstack/react-router';
import { useEffect } from 'react';
import { addWebViewMessageListener, isWebView } from './webview.utils';
import type { RNToWebMessage } from './webview.types';

export const useWebViewNavigate = () => {
  const navigate = useNavigate();

  useEffect(() => {
    if (!isWebView()) return;

    const cleanup = addWebViewMessageListener(
      async (message: RNToWebMessage) => {
        alert(message.type);
        switch (message.type) {
          case 'ANDROID_BACK_BUTTON_CLICKED':
            alert(1);
            navigate({ to: '..' });
            break;
        }
      },
    );

    return cleanup;
  }, [navigate]);
};
