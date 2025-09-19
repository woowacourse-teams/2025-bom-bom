export interface WebToRNMessage {
  type: 'SHOW_LOGIN_SCREEN' | 'LOGIN_SUCCESS' | 'LOGIN_FAILED';
  payload?: any;
}

export interface RNToWebMessage {
  type: 'GOOGLE_LOGIN_TOKEN' | 'APPLE_LOGIN_TOKEN';
  payload?: {
    idToken?: string;
    serverAuthCode?: string;
    identityToken?: string;
    authorizationCode?: string;
  };
}
