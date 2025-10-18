import { logger } from '@/utils/logger';
import type {
  RNToWebMessage,
  WebToRNMessage,
  WindowWithWebkit,
} from '@bombom/shared/webview';

export const isAndroid = (): boolean => navigator.userAgent.includes('google');
export const isIOS = (): boolean => navigator.userAgent.includes('Apple');

export const isWebView = (): boolean => {
  return !!(isAndroid() || isIOS());
};

export const isWeb = (): boolean => {
  return !isWebView();
};

export const sendMessageToRN = (message: WebToRNMessage): void => {
  if (!isWebView()) {
    console.warn('WebView 환경이 아닙니다. 메시지가 전송되지 않습니다.');
    return;
  }

  try {
    const messageString = JSON.stringify(message);

    if (isAndroid()) window.ReactNativeWebView?.postMessage(messageString);
    else if (isIOS())
      (
        window as WindowWithWebkit
      ).webkit?.messageHandlers?.ReactNativeWebView?.postMessage(messageString);

    logger.log('WebView 메시지 전송:', message);
  } catch (error) {
    logger.error('WebView 메시지 전송 실패:', error);
  }
};

export const addWebViewMessageListener = (
  callback: (message: RNToWebMessage) => void,
): (() => void) => {
  const messageHandler = (event: MessageEvent) => {
    try {
      const message: RNToWebMessage = JSON.parse(event.data);
      logger.log('WebView에서 메시지 수신:', message);
      callback(message);
    } catch (error) {
      logger.error('WebView 메시지 파싱 실패:', error);
    }
  };

  document.addEventListener('message', messageHandler as EventListener); // Android
  window.addEventListener('message', messageHandler as EventListener); // iOS
  return () => {
    document.removeEventListener('message', messageHandler as EventListener);
    window.removeEventListener('message', messageHandler as EventListener);
  };
};

declare global {
  interface Window {
    ReactNativeWebView?: {
      postMessage: (message: string) => void;
    };
  }
}
