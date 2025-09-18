import { isAndroid, isIOS, isRunningInWebView } from '../libs/webview/utils';
import type {
  WebToRNMessage,
  RNToWebMessage,
  WindowWithWebkit,
} from '../libs/webview/types';

export const sendMessageToRN = (message: WebToRNMessage): void => {
  if (!isRunningInWebView()) {
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

    console.log('WebView 메시지 전송:', message);
  } catch (error) {
    console.error('WebView 메시지 전송 실패:', error);
  }
};

export const requestShowLoginScreen = (): void => {
  sendMessageToRN({
    type: 'SHOW_LOGIN_SCREEN',
  });
};

export const addWebViewMessageListener = (
  callback: (message: RNToWebMessage) => void,
): (() => void) => {
  if (!isRunningInWebView()) {
    return () => {};
  }

  const messageHandler = (event: MessageEvent) => {
    try {
      const message: RNToWebMessage = JSON.parse(event.data);
      console.log('WebView에서 메시지 수신:', message);
      callback(message);
    } catch (error) {
      console.error('WebView 메시지 파싱 실패:', error);
    }
  };

  window.addEventListener('message', messageHandler);
  return () => {
    window.removeEventListener('message', messageHandler);
  };
};
