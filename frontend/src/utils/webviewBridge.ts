// WebView와 React Native 간 통신을 위한 유틸리티 함수들

export interface WebToRNMessage {
  type: 'SHOW_LOGIN_SCREEN' | 'LOGOUT_REQUEST' | 'USER_ACTION';
  payload?: unknown;
}

export interface RNToWebMessage {
  type: 'LOGIN_SUCCESS' | 'LOGOUT_SUCCESS' | 'AUTH_STATE_CHANGED';
  payload?: {
    user?: {
      id: string;
      email: string;
      name?: string;
      provider: string;
    };
    token?: string;
    isAuthenticated: boolean;
  };
}

/**
 * React Native WebView가 실행되고 있는지 확인
 */
export const isRunningInWebView = (): boolean => {
  return !!(
    window.ReactNativeWebView ||
    (window as WindowWithWebkit).webkit?.messageHandlers?.ReactNativeWebView
  );
};

/**
 * React Native로 메시지 전송
 */
export const sendMessageToRN = (message: WebToRNMessage): void => {
  if (!isRunningInWebView()) {
    console.warn('WebView 환경이 아닙니다. 메시지가 전송되지 않습니다.');
    return;
  }

  try {
    const messageString = JSON.stringify(message);

    // Android
    if (window.ReactNativeWebView) {
      window.ReactNativeWebView.postMessage(messageString);
    }
    // iOS
    else if (
      (window as WindowWithWebkit).webkit?.messageHandlers?.ReactNativeWebView
    ) {
      (
        window as WindowWithWebkit
      ).webkit?.messageHandlers?.ReactNativeWebView?.postMessage(messageString);
    }

    console.log('WebView 메시지 전송:', message);
  } catch (error) {
    console.error('WebView 메시지 전송 실패:', error);
  }
};

/**
 * 로그인 화면 표시 요청
 */
export const requestShowLoginScreen = (): void => {
  sendMessageToRN({
    type: 'SHOW_LOGIN_SCREEN',
  });
};

/**
 * 로그아웃 요청
 */
export const requestLogout = (): void => {
  sendMessageToRN({
    type: 'LOGOUT_REQUEST',
  });
};

/**
 * React Native에서 온 메시지 수신 리스너 등록
 */
export const addWebViewMessageListener = (
  callback: (message: RNToWebMessage) => void,
): (() => void) => {
  if (!isRunningInWebView()) {
    return () => {}; // cleanup 함수
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

  // 메시지 이벤트 리스너 등록
  window.addEventListener('message', messageHandler);

  // cleanup 함수 반환
  return () => {
    window.removeEventListener('message', messageHandler);
  };
};

// 전역 WebView 브릿지 객체 타입 정의 확장
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
