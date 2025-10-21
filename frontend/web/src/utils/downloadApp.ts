import { APP_STORE_LINK, PLAY_STORE_LINK } from '@bombom/shared';
import { getDeviceInWebApp } from './device';

export const downloadApp = () => {
  const device = getDeviceInWebApp();
  if (!device) return;

  window.location.href =
    device === 'android' ? PLAY_STORE_LINK : APP_STORE_LINK;
};
