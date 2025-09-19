// 웹뷰에서 React Native로 전송하는 메시지 타입
export interface WebToRNMessage {
  type: 'SHOW_LOGIN_SCREEN' | 'USER_ACTION' | 'LOGIN_SUCCESS' | 'LOGIN_FAILED';
  payload?: any;
}

// React Native에서 웹뷰로 전송하는 메시지 타입
export interface RNToWebMessage {
  type: 'GOOGLE_LOGIN_TOKEN' | 'APPLE_LOGIN_TOKEN';
  payload?: {
    idToken?: string;
    serverAuthCode?: string;
    identityToken?: string;
    authorizationCode?: string;
  };
}

// WebView 메시지 핸들러 타입
export interface WebViewMessageHandler {
  onShowLoginScreen: () => void;
  onLogoutRequest: () => void;
  sendAuthStateToWeb: (
    isAuthenticated: boolean,
    user?: any,
    token?: string,
  ) => void;
}
