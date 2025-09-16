// 웹뷰에서 React Native로 전송하는 메시지 타입
export interface WebToRNMessage {
  type: "SHOW_LOGIN_SCREEN" | "LOGOUT_REQUEST" | "USER_ACTION";
  payload?: any;
}

// React Native에서 웹뷰로 전송하는 메시지 타입
export interface RNToWebMessage {
  type:
    | "LOGIN_SUCCESS"
    | "LOGOUT_SUCCESS"
    | "AUTH_STATE_CHANGED"
    | "GOOGLE_LOGIN_TOKEN"
    | "APPLE_LOGIN_TOKEN";
  payload?: {
    user?: {
      id: string;
      email: string;
      name?: string;
      provider: string;
    };
    token?: string;
    isAuthenticated?: boolean;
    // Google 로그인 토큰
    idToken?: string;
    serverAuthCode?: string;
    // Apple 로그인 토큰
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
    token?: string
  ) => void;
}
