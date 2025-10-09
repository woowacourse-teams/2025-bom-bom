import { isAppVersionSupported } from './version';
import { isWebView, sendMessageToRN } from '@/libs/webview/webview.utils';

export const openExternalLink = (link: string) => {
  const inAppBrowserUpdated = isAppVersionSupported('1.0.2');
  if (isWebView() && inAppBrowserUpdated) {
    sendMessageToRN({
      type: 'OPEN_BROWSER',
      payload: { url: link },
    });
  } else window.open(link, '_blank', 'noopener,noreferrer');
};
