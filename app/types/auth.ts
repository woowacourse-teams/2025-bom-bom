export interface User {
  id: string;
  name?: string;
  email?: string;
  provider: 'google' | 'apple';
}

export interface AuthContextType {
  loginWithGoogle: () => Promise<void>;
  loginWithApple: () => Promise<void>;
  logout: () => Promise<void>;
  error: string | null;
  clearError: () => void;
  // WebView 통신 관련
  showWebViewLogin: boolean;
  setShowWebViewLogin: (show: boolean) => void;
  webViewRef: React.RefObject<any>;
  sendMessageToWeb: (message: any) => void;
}

export interface LoginResponse {
  message: string;
  sessionId: string;
  status: string;
}
