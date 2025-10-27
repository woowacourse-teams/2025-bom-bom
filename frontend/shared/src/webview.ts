export interface WindowWithWebkit extends Window {
  webkit?: {
    messageHandlers?: {
      ReactNativeWebView?: {
        postMessage: (message: string) => void;
      };
    };
  };
}

export type WebToRNMessage =
  | { type: 'SHOW_LOGIN_SCREEN' }
  | {
      type: 'LOGIN_SUCCESS';
      payload?: {
        isAuthenticated?: boolean;
        provider?: string;
        memberId?: number;
      };
    }
  | { type: 'LOGIN_FAILED'; payload?: { error?: string; provider?: string } }
  | { type: 'OPEN_BROWSER'; payload: { url: string } }
  | { type: 'OPEN_NOTIFICATION_SETTINGS' }
  | { type: 'REQUEST_NOTIFICATION_STATUS' }
  | { type: 'TOGGLE_NOTIFICATION'; payload: { enabled: boolean } };

export type RNToWebMessage =
  | {
      type: 'GOOGLE_LOGIN_TOKEN';
      payload: {
        identityToken: string;
        authorizationCode?: string;
        email: string;
        name: string;
      };
    }
  | {
      type: 'APPLE_LOGIN_TOKEN';
      payload: {
        identityToken: string;
        authorizationCode: string;
        email: string;
        name: string;
      };
    }
  | {
      type: 'NOTIFICATION_ROUTING';
      payload: {
        url: string;
      };
    }
  | {
      type: 'NOTIFICATION_STATUS';
      payload: {
        enabled: boolean;
      };
    };
