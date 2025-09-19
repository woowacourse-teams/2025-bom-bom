type WebViewMessageType =
  | 'SHOW_LOGIN_SCREEN'
  | 'LOGIN_SUCCESS'
  | 'LOGIN_FAILED';
export interface WebToRNMessage {
  type: WebViewMessageType;
  isAuthenticated?: boolean;
  provider?: string;
  error?: string;
}

type RNToWebMessageType = 'GOOGLE_LOGIN_TOKEN' | 'APPLE_LOGIN_TOKEN';

export interface RNToWebMessage {
  type: RNToWebMessageType;
  payload?: {
    identityToken?: string;
    authorizationCode?: string;
  };
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
