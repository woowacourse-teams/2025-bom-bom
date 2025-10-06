// Discriminated union types for type-safe WebView messaging

export type WebToRNMessage =
  | { type: 'SHOW_LOGIN_SCREEN' }
  | {
      type: 'LOGIN_SUCCESS';
      payload?: {
        isAuthenticated?: boolean;
        provider?: string;
        userId?: string;
      };
    }
  | { type: 'LOGIN_FAILED'; payload?: { error?: string; provider?: string } }
  | { type: 'OPEN_BROWSER'; payload: { url: string } };

export type RNToWebMessage =
  | {
      type: 'GOOGLE_LOGIN_TOKEN';
      payload: {
        token: string;
        identityToken?: string;
        authorizationCode?: string;
        email?: string;
        name?: string;
      };
    }
  | {
      type: 'APPLE_LOGIN_TOKEN';
      payload: {
        token: string;
        identityToken?: string;
        authorizationCode?: string;
        email?: string;
        name?: string;
      };
    };

export interface WindowWithWebkit extends Window {
  webkit?: {
    messageHandlers?: {
      ReactNativeWebView?: {
        postMessage: (message: string) => void;
      };
    };
  };
}
