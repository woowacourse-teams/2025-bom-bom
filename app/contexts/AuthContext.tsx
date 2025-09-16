import AsyncStorage from "@react-native-async-storage/async-storage";
import {
  GoogleSignin,
  statusCodes,
} from "@react-native-google-signin/google-signin";
import * as AppleAuthentication from "expo-apple-authentication";
import React, {
  createContext,
  ReactNode,
  useContext,
  useEffect,
  useRef,
  useState,
} from "react";
import { WebView } from "react-native-webview";

import { ApiClient } from "../services/api";
import { AuthContextType, User } from "../types/auth";
import { RNToWebMessage } from "../types/webview";

const CLIENT_ID =
  "190361254930-1464b7md34crhu077urc0hsvtsmb5ks5.apps.googleusercontent.com";

const AuthContext = createContext<AuthContextType | undefined>(undefined);

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showWebViewLogin, setShowWebViewLogin] = useState(false);
  const webViewRef = useRef<WebView>(null);

  const isAuthenticated = !!user;

  useEffect(() => {
    const initializeAuth = async () => {
      try {
        GoogleSignin.configure({
          webClientId: CLIENT_ID,
          offlineAccess: true,
          hostedDomain: "",
          forceCodeForRefreshToken: true,
          accountName: "",
          iosClientId: CLIENT_ID,
          googleServicePlistPath: "",
          profileImageSize: 120,
        });

        const token = await AsyncStorage.getItem("authToken");
        if (token) {
          const response = await ApiClient.verifyToken();
          if (response?.sessionId) {
            await AsyncStorage.setItem("sessionId", response.sessionId);
          }
        }
      } catch (error) {
        await AsyncStorage.removeItem("authToken");
        console.log("Token verification failed:", error);
      } finally {
        setIsLoading(false);
      }
    };

    initializeAuth();
  }, []);

  const clearError = () => setError(null);

  // WebView로 메시지 전송 (타입 안전)
  const sendMessageToWeb = (message: RNToWebMessage) => {
    try {
      const messageString = JSON.stringify(message);
      webViewRef.current?.postMessage(messageString);
      console.log("WebView로 메시지 전송:", message);
    } catch (error) {
      console.error("WebView 메시지 전송 실패:", error);
    }
  };

  // 웹뷰에서 오는 메시지 처리
  const handleWebViewMessage = (event: any) => {
    try {
      const message = JSON.parse(event.nativeEvent.data);

      switch (message.type) {
        case "LOGIN_SUCCESS":
          alert("LOGIN_SUCCESS");
          console.log("웹뷰에서 로그인 성공 알림 수신:", message.payload);
          setShowWebViewLogin(false);
          setUser({
            id: "web-user", // 실제로는 서버에서 받은 유저 정보 사용
            email: "",
            name: "",
            provider: message.payload?.provider || "unknown",
          });
          setIsLoading(false);
          break;

        case "LOGIN_FAILED":
          console.log("웹뷰에서 로그인 실패 알림 수신:", message.payload);
          setShowWebViewLogin(false);
          setError(message.payload?.error || "로그인에 실패했습니다.");
          setIsLoading(false);
          break;

        default:
          console.warn("알 수 없는 웹뷰 메시지:", message);
      }
    } catch (error) {
      console.error("웹뷰 메시지 파싱 오류:", error);
    }
  };

  const loginWithGoogle = async (): Promise<void> => {
    try {
      setIsLoading(true);
      setError(null);

      await GoogleSignin.hasPlayServices();

      const userInfo = await GoogleSignin.signIn();

      if (userInfo?.data?.idToken) {
        // 웹뷰 로그인 화면 표시
        setShowWebViewLogin(true);

        setUser({
          id: "web-user",
          email: "",
          name: "",
          provider: "google",
        });

        await AsyncStorage.setItem("authToken", userInfo.data.idToken);
        await AsyncStorage.setItem(
          "serverAuthCode",
          userInfo.data.serverAuthCode || ""
        );
      } else {
        throw new Error("ID 토큰을 가져올 수 없습니다.");
      }
    } catch (error: any) {
      if (error.code === statusCodes.SIGN_IN_CANCELLED) {
        setError("Google 로그인이 취소되었습니다.");
      } else if (error.code === statusCodes.IN_PROGRESS) {
        setError("로그인이 이미 진행 중입니다.");
      } else if (error.code === statusCodes.PLAY_SERVICES_NOT_AVAILABLE) {
        setError("Google Play Services를 사용할 수 없습니다.");
      } else {
        setError(error.message || "Google 로그인에 실패했습니다.");
      }
      throw error;
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
        // 웹뷰 로그인 화면 표시
        setShowWebViewLogin(true);

        // 웹뷰에 Apple 로그인 토큰 전송
        sendMessageToWeb({
          type: "APPLE_LOGIN_TOKEN",
          payload: {
            identityToken: credential.identityToken,
            authorizationCode: credential.authorizationCode,
          },
        });
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

      // API 서버 로그아웃
      await ApiClient.logout();

      // Google Sign-In 로그아웃
      try {
        await GoogleSignin.signOut();
      } catch (error) {
        console.log("Google signOut error:", error);
      }

      await AsyncStorage.removeItem("authToken");
      setUser(null);
      setError(null);
    } catch (err: any) {
      console.error("Logout error:", err);
      // 로그아웃 요청이 실패해도 로컬 상태는 정리
      try {
        await GoogleSignin.signOut();
      } catch (error) {
        console.log("Google signOut error:", error);
      }
      await AsyncStorage.removeItem("authToken");
      setUser(null);
      setError(null);
    } finally {
      setIsLoading(false);
    }
  };

  const value = {
    user,
    isLoading,
    isAuthenticated,
    loginWithGoogle,
    loginWithApple,
    logout,
    error,
    clearError,
    showWebViewLogin,
    setShowWebViewLogin,
    webViewRef,
    handleWebViewMessage,
    sendMessageToWeb,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
};
