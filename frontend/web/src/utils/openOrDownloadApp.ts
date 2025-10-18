import { APP_DEEP_LINK, APP_STORE_LINK, PLAY_STORE_LINK } from '@bombom/shared';
import { isAndroidByUserAgent } from './device';

export const openOrDownloadApp = () => {
  const isAndroid = isAndroidByUserAgent();

  window.location.href = APP_DEEP_LINK;

  setTimeout(() => {
    window.location.href = isAndroid ? PLAY_STORE_LINK : APP_STORE_LINK;
  }, 2000);
};
