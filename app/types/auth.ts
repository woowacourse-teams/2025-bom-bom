export interface User {
  id: string;
  email: string;
  name?: string;
  provider: 'email' | 'google' | 'apple';
}

export interface AuthContextType {
  user: User | null;
  isLoading: boolean;
  isAuthenticated: boolean;
  login: (email: string, password: string) => Promise<void>;
  loginWithGoogle: () => Promise<void>;
  loginWithApple: () => Promise<void>;
  logout: () => Promise<void>;
  error: string | null;
  clearError: () => void;
}

export interface LoginResponse {
  token: string;
  user: User;
}