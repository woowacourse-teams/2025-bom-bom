type WebViewMessageType =
  | 'SHOW_LOGIN_SCREEN'
  | 'LOGIN_SUCCESS'
  | 'LOGIN_FAILED';

export interface WebToRNMessage {
  type: WebViewMessageType;
  payload?: any;
}

type RNToWebMessageType = 'GOOGLE_LOGIN_TOKEN' | 'APPLE_LOGIN_TOKEN';
export interface RNToWebMessage {
  type: RNToWebMessageType;
  payload?: {
    idToken?: string;
    serverAuthCode?: string;
    identityToken?: string;
    authorizationCode?: string;
  };
}
