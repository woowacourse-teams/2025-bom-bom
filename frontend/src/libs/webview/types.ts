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

declare global {
  interface Window {
    ReactNativeWebView?: {
      postMessage: (message: string) => void;
    };
  }
}
