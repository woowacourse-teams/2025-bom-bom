import { APP_STORE_LINK, PLAY_STORE_LINK } from '@bombom/shared';
import { isAndroidByUserAgent } from './device';

export const downloadApp = () => {
  window.location.href = isAndroidByUserAgent()
    ? PLAY_STORE_LINK
    : APP_STORE_LINK;
};
