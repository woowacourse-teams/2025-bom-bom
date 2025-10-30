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
  | { type: 'REQUEST_DEVICE_INFO' }
  | { type: 'CHECK_NOTIFICATION_PERMISSION'; payload: { enabled: boolean } }
  | {
      type: 'LOGIN_STATUS_RESPONSE';
      payload: {
        isLoggedIn: boolean;
        memberId?: number;
      };
    };

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
    }
  | {
      type: 'DEVICE_INFO';
      payload: {
        deviceUuid: string;
      };
    }
  | { type: 'CHECK_LOGIN_STATUS' };
