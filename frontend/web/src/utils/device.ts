import { isAppVersionSupported } from './version';
import type { WindowWithWebkit } from '@bombom/shared/webview';

export const isWebView = () => {
  const isUserAgentSupported = isAppVersionSupported('1.0.4');
  if (!isUserAgentSupported) return !!window.ReactNativeWebView;
  return navigator.userAgent.includes('bombom');
};

export const getDeviceInWebView = () => {
  if (!isWebView()) return null;

  const isUserAgentSupported = isAppVersionSupported('1.0.4');
  if (!isUserAgentSupported) {
    return (window as WindowWithWebkit).webkit?.messageHandlers
      ?.ReactNativeWebView
      ? 'ios'
      : 'android';
  }

  if (navigator.userAgent.includes('google')) return 'android';
  if (navigator.userAgent.includes('Apple')) return 'ios';
  return null;
};

export const getDeviceInWebApp = () => {
  if (isWebView()) return null;
  if (/Android/i.test(navigator.userAgent)) return 'android';
  if (/iPhone|iPad|iPod/i.test(navigator.userAgent)) return 'ios';
  return null;
};

export const getDevice = () => {
  if (isWebView()) {
    return getDeviceInWebView();
  } else {
    return getDeviceInWebApp();
  }
};

export const isAndroid = () => {
  return getDevice() === 'android';
};

export const isIOS = () => {
  return getDevice() === 'ios';
};
