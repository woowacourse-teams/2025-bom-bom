import { isWebView, sendMessageToRN } from '@/libs/webview/webview.utils';

export const openExternalLink = (link: string) => {
  if (isWebView()) {
    sendMessageToRN({
      type: 'OPEN_BROWSER',
      payload: { url: link },
    });
  } else {
    window.open(link, '_blank', 'noopener,noreferrer');
  }
};
