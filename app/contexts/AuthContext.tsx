import AsyncStorage from "@react-native-async-storage/async-storage";
import * as AppleAuthentication from "expo-apple-authentication";
import * as AuthSession from "expo-auth-session";
import React, {
  createContext,
  ReactNode,
  useContext,
  useEffect,
  useState,
} from "react";
import { Platform } from "react-native";

import { ApiClient } from "../services/api";
import { AuthContextType, User } from "../types/auth";

// Google OAuth 설정
const GOOGLE_CLIENT_ID = {
  ios: "1065103736736-6mgha0t4ejp8mvt2pn5dqmmbp0lh7u8k.apps.googleusercontent.com", // iOS 클라이언트 ID 필요
  android:
    "1065103736736-6mgha0t4ejp8mvt2pn5dqmmbp0lh7u8k.apps.googleusercontent.com", // Android 클라이언트 ID 필요
};

const AuthContext = createContext<AuthContextType | undefined>(undefined);

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showWebViewLogin, setShowWebViewLogin] = useState(false);

  const isAuthenticated = !!user;

  // 앱 시작 시 저장된 토큰으로 자동 로그인 시도
  useEffect(() => {
    const initializeAuth = async () => {
      try {
        const token = await AsyncStorage.getItem("authToken");
        if (token) {
          const response = await ApiClient.verifyToken();
          setUser(response.user);
        }
      } catch (error) {
        // 토큰이 유효하지 않으면 삭제
        await AsyncStorage.removeItem("authToken");
        console.log("Token verification failed:", error);
      } finally {
        setIsLoading(false);
      }
    };

    initializeAuth();
  }, []);

  const clearError = () => setError(null);

  // 이메일/비밀번호 로그인
  const login = async (email: string, password: string): Promise<void> => {
    try {
      setIsLoading(true);
      setError(null);

      const response = await ApiClient.loginWithEmail(email, password);

      // 토큰 저장
      await AsyncStorage.setItem("authToken", response.token);
      setUser(response.user);
    } catch (err: any) {
      setError(err.message || "로그인에 실패했습니다.");
      throw err;
    } finally {
      setIsLoading(false);
    }
  };

  // Google 로그인
  const loginWithGoogle = async (): Promise<void> => {
    try {
      setIsLoading(true);
      setError(null);

      // Google OAuth 설정
      const redirectUri = AuthSession.makeRedirectUri({
        scheme: "com.antarctica.bombom",
        path: "auth",
      });

      const request = new AuthSession.AuthRequest({
        clientId:
          Platform.OS === "ios"
            ? GOOGLE_CLIENT_ID.ios
            : GOOGLE_CLIENT_ID.android,
        scopes: ["openid", "profile", "email"],
        responseType: AuthSession.ResponseType.IdToken,
        redirectUri,
        extraParams: {},
      });

      const result = await request.promptAsync({
        authorizationEndpoint: "https://accounts.google.com/o/oauth2/v2/auth",
      });

      if (result.type === "success" && result.params.id_token) {
        const response = await ApiClient.loginWithGoogle(
          result.params.id_token
        );

        // 토큰 저장
        await AsyncStorage.setItem("authToken", response.token);
        setUser(response.user);
      } else {
        throw new Error("Google 로그인이 취소되었습니다.");
      }
    } catch (err: any) {
      setError(err.message || "Google 로그인에 실패했습니다.");
      throw err;
    } finally {
      setIsLoading(false);
    }
  };

  // Apple 로그인
  const loginWithApple = async (): Promise<void> => {
    try {
      setIsLoading(true);
      setError(null);

      if (!AppleAuthentication.isAvailableAsync()) {
        throw new Error("Apple 로그인을 사용할 수 없습니다.");
      }

      const credential = await AppleAuthentication.signInAsync({
        requestedScopes: [
          AppleAuthentication.AppleAuthenticationScope.FULL_NAME,
          AppleAuthentication.AppleAuthenticationScope.EMAIL,
        ],
      });

      if (credential.identityToken && credential.authorizationCode) {
        const response = await ApiClient.loginWithApple(
          credential.identityToken,
          credential.authorizationCode
        );

        // 토큰 저장
        await AsyncStorage.setItem("authToken", response.token);
        setUser(response.user);
      } else {
        throw new Error("Apple 로그인 정보를 가져올 수 없습니다.");
      }
    } catch (err: any) {
      if (err.code === "ERR_REQUEST_CANCELED") {
        setError("Apple 로그인이 취소되었습니다.");
      } else {
        setError(err.message || "Apple 로그인에 실패했습니다.");
      }
      throw err;
    } finally {
      setIsLoading(false);
    }
  };

  const logout = async (): Promise<void> => {
    try {
      setIsLoading(true);

      await ApiClient.logout();

      await AsyncStorage.removeItem("authToken");
      setUser(null);
      setError(null);
    } catch (err: any) {
      console.error("Logout error:", err);
      // 로그아웃 요청이 실패해도 로컬 상태는 정리
      await AsyncStorage.removeItem("authToken");
      setUser(null);
      setError(null);
    } finally {
      setIsLoading(false);
    }
  };

  const value: AuthContextType = {
    user,
    isLoading,
    isAuthenticated,
    login,
    loginWithGoogle,
    loginWithApple,
    logout,
    error,
    clearError,
    showWebViewLogin,
    setShowWebViewLogin,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
};
