import AsyncStorage from '@react-native-async-storage/async-storage';
import { GoogleSignin } from '@react-native-google-signin/google-signin';
import * as AppleAuthentication from 'expo-apple-authentication';
import React, {
  createContext,
  PropsWithChildren,
  useContext,
  useRef,
  useState,
} from 'react';
import { WebView } from 'react-native-webview';

import { AuthContextType } from '../types/auth';
import { RNToWebMessage } from '../types/webview';

const AuthContext = createContext<AuthContextType | undefined>(undefined);

const WEB_CLIENT_ID = process.env.EXPO_PUBLIC_GOOGLE_WEB_CLIENT_ID;
const IOS_CLIENT_ID = process.env.EXPO_PUBLIC_GOOGLE_IOS_CLIENT_ID;
const ANDROID_CLIENT_ID = process.env.EXPO_PUBLIC_GOOGLE_ANDROID_CLIENT_ID;

export const AuthProvider = ({ children }: PropsWithChildren) => {
  const [showWebViewLogin, setShowWebViewLogin] = useState(false);
  const webViewRef = useRef<WebView>(null);

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
    GoogleSignin.configure({
      webClientId: IOS_CLIENT_ID,
      offlineAccess: true,
      hostedDomain: '',
      forceCodeForRefreshToken: true,
      accountName: '',
      iosClientId: IOS_CLIENT_ID,
      googleServicePlistPath: '',
      profileImageSize: 120,
    });

    await GoogleSignin.hasPlayServices();

    const userInfo = await GoogleSignin.signIn();

    if (userInfo?.data?.idToken) {
      setShowWebViewLogin(true);

      await AsyncStorage.setItem(
        'auth',
        JSON.stringify({
          identityToken: userInfo.data.idToken,
          authorizationCode: userInfo.data.serverAuthCode,
          provider: 'google',
        }),
      );
    } else {
      throw new Error('ID 토큰을 가져올 수 없습니다.');
    }
  };

  const loginWithApple = async (): Promise<void> => {
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
      setShowWebViewLogin(true);

      await AsyncStorage.setItem(
        'auth',
        JSON.stringify({
          identityToken: credential.identityToken,
          authorizationCode: credential.authorizationCode,
          provider: 'apple',
        }),
      );
    } else {
      throw new Error('Apple 로그인 정보를 가져올 수 없습니다.');
    }
  };

  return (
    <AuthContext.Provider
      value={{
        loginWithGoogle,
        loginWithApple,
        showWebViewLogin,
        setShowWebViewLogin,
        webViewRef,
        sendMessageToWeb,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
