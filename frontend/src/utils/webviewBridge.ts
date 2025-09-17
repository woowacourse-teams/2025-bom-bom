export interface WebToRNMessage {
  type: 'SHOW_LOGIN_SCREEN' | 'LOGIN_SUCCESS' | 'LOGIN_FAILED';
  payload?: {
    isAuthenticated?: boolean;
    provider?: string;
    error?: string;
  };
}

export interface RNToWebMessage {
  type: 'GOOGLE_LOGIN_TOKEN' | 'APPLE_LOGIN_TOKEN';
  payload?: {
    idToken?: string;
    serverAuthCode?: string;
  };
}

const isAndroid = () => window.ReactNativeWebView;
const isIOS = () =>
  (window as WindowWithWebkit).webkit?.messageHandlers?.ReactNativeWebView;

export const isRunningInWebView = (): boolean => {
  return !!(isAndroid() || isIOS());
};

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

interface WindowWithWebkit extends Window {
  webkit?: {
    messageHandlers?: {
      ReactNativeWebView?: {
        postMessage: (message: string) => void;
      };
    };
  };
}

declare global {
  interface Window {
    ReactNativeWebView?: {
      postMessage: (message: string) => void;
    };
  }
}
