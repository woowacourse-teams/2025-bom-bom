export interface User {
  id: string;
  name?: string;
  email?: string;
  provider: "google" | "apple";
}

export interface AuthContextType {
  user: User | null;
  isLoading: boolean;
  isAuthenticated: boolean;
  loginWithGoogle: () => Promise<void>;
  loginWithApple: () => Promise<void>;
  logout: () => Promise<void>;
  error: string | null;
  clearError: () => void;
  // WebView 통신 관련
  showWebViewLogin: boolean;
  setShowWebViewLogin: (show: boolean) => void;
  webViewRef: React.RefObject<any>;
  handleWebViewMessage: (event: any) => void;
  sendMessageToWeb: (message: any) => void;
}

export interface LoginResponse {
  message: string;
  sessionId: string;
  status: string;
}
