import AsyncStorage from '@react-native-async-storage/async-storage';
import {
  GoogleSignin,
  statusCodes,
} from '@react-native-google-signin/google-signin';
import * as AppleAuthentication from 'expo-apple-authentication';
import React, {
  createContext,
  ReactNode,
  useContext,
  useRef,
  useState,
} from 'react';
import { WebView } from 'react-native-webview';

import { ApiClient } from '../services/api';
import { AuthContextType } from '../types/auth';
import { RNToWebMessage } from '../types/webview';

const CLIENT_ID =
  '190361254930-1464b7md34crhu077urc0hsvtsmb5ks5.apps.googleusercontent.com';

const AuthContext = createContext<AuthContextType | undefined>(undefined);

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [error, setError] = useState<string | null>(null);
  const [showWebViewLogin, setShowWebViewLogin] = useState(false);
  const webViewRef = useRef<WebView>(null);

  const clearError = () => setError(null);

  const sendMessageToWeb = (message: RNToWebMessage) => {
    try {
      const messageString = JSON.stringify(message);
      webViewRef.current?.postMessage(messageString);
      console.log('WebView로 메시지 전송:', message);
    } catch (error) {
      console.error('WebView 메시지 전송 실패:', error);
    }
  };

  const loginWithGoogle = async (): Promise<void> => {
    try {
      setError(null);

      await GoogleSignin.hasPlayServices();

      const userInfo = await GoogleSignin.signIn();

      if (userInfo?.data?.idToken) {
        setShowWebViewLogin(true);

        await AsyncStorage.setItem('provider', 'google');
        await AsyncStorage.setItem('authToken', userInfo.data.idToken);
        await AsyncStorage.setItem(
          'serverAuthCode',
          userInfo.data.serverAuthCode || '',
        );
      } else {
        throw new Error('ID 토큰을 가져올 수 없습니다.');
      }
    } catch (error: any) {
      if (error.code === statusCodes.SIGN_IN_CANCELLED) {
        setError('Google 로그인이 취소되었습니다.');
      } else if (error.code === statusCodes.IN_PROGRESS) {
        setError('로그인이 이미 진행 중입니다.');
      } else if (error.code === statusCodes.PLAY_SERVICES_NOT_AVAILABLE) {
        setError('Google Play Services를 사용할 수 없습니다.');
      } else {
        setError(error.message || 'Google 로그인에 실패했습니다.');
      }
      throw error;
    } finally {
    }
  };

  // Apple 로그인
  const loginWithApple = async (): Promise<void> => {
    try {
      setError(null);

      if (!AppleAuthentication.isAvailableAsync()) {
        throw new Error('Apple 로그인을 사용할 수 없습니다.');
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

        await AsyncStorage.setItem('provider', 'apple');
        await AsyncStorage.setItem('authToken', credential.identityToken);
        await AsyncStorage.setItem(
          'serverAuthCode',
          credential.authorizationCode || '',
        );
      } else {
        throw new Error('Apple 로그인 정보를 가져올 수 없습니다.');
      }
    } catch (err: any) {
      if (err.code === 'ERR_REQUEST_CANCELED') {
        setError('Apple 로그인이 취소되었습니다.');
      } else {
        setError(err.message || 'Apple 로그인에 실패했습니다.');
      }
      throw err;
    }
  };

  const logout = async (): Promise<void> => {
    try {
      // API 서버 로그아웃
      await ApiClient.logout();

      // Google Sign-In 로그아웃
      try {
        await GoogleSignin.signOut();
      } catch (error) {
        console.log('Google signOut error:', error);
      }

      await AsyncStorage.removeItem('authToken');
      setError(null);
    } catch (err: any) {
      console.error('Logout error:', err);
      // 로그아웃 요청이 실패해도 로컬 상태는 정리
      try {
        await GoogleSignin.signOut();
      } catch (error) {
        console.log('Google signOut error:', error);
      }
      await AsyncStorage.removeItem('authToken');
      setError(null);
    }
  };

  const value = {
    loginWithGoogle,
    loginWithApple,
    logout,
    error,
    clearError,
    showWebViewLogin,
    setShowWebViewLogin,
    webViewRef,
    sendMessageToWeb,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
