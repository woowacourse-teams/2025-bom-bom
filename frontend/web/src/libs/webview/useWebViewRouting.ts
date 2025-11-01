import { useNavigate } from '@tanstack/react-router';
import { useEffect } from 'react';
import { addWebViewMessageListener } from './webview.utils';
import { isWebView } from '@/utils/device';
import type { RNToWebMessage } from '@bombom/shared';

export const useWebViewRouting = () => {
  const navigate = useNavigate();

  useEffect(() => {
    if (!isWebView()) return;

    const cleanup = addWebViewMessageListener((message: RNToWebMessage) => {
      if (message.type === 'NOTIFICATION_ROUTING') {
        navigate({
          to: message.payload.url,
        });
        return;
      }
    });

    return cleanup;
  }, [navigate]);
};
