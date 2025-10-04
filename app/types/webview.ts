type WebViewMessageType =
  | 'SHOW_LOGIN_SCREEN'
  | 'LOGIN_SUCCESS'
  | 'LOGIN_FAILED'
  | 'OPEN_BROWSER';

export interface WebToRNMessage {
  type: WebViewMessageType;
  payload?: any;
}

type RNToWebMessageType =
  | 'GOOGLE_LOGIN_TOKEN'
  | 'APPLE_LOGIN_TOKEN'
  | 'ANDROID_BACK_BUTTON_CLICKED';

export interface RNToWebMessage {
  type: RNToWebMessageType;
  payload?: any;
}
