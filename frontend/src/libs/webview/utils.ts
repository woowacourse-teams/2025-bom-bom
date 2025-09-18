import type { WindowWithWebkit } from './types';

export const isAndroid = (): boolean => !!window.ReactNativeWebView;

export const isIOS = (): boolean =>
  !!(window as WindowWithWebkit).webkit?.messageHandlers?.ReactNativeWebView;

export const isRunningInWebView = (): boolean => {
  return !!(isAndroid() || isIOS());
};
