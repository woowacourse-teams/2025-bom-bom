type WebViewMessageType =
  | 'SHOW_LOGIN_SCREEN'
  | 'LOGIN_SUCCESS'
  | 'LOGIN_FAILED'
  | 'OPEN_BROWSER';

export interface WebToRNMessage {
  type: WebViewMessageType;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  payload?: any;
}

type RNToWebMessageType = 'GOOGLE_LOGIN_TOKEN' | 'APPLE_LOGIN_TOKEN';

export interface RNToWebMessage {
  type: RNToWebMessageType;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  payload?: any;
}

export interface WindowWithWebkit extends Window {
  webkit?: {
    messageHandlers?: {
      ReactNativeWebView?: {
        postMessage: (message: string) => void;
      };
    };
  };
}
