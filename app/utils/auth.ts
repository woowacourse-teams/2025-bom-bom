import { ENV } from '@/constants/env';
import {
  ConfigureParams,
  GoogleSignin,
} from '@react-native-google-signin/google-signin';
import * as AppleAuthentication from 'expo-apple-authentication';
import { Platform } from 'react-native';

interface LoginWithGoogleCallback {
  identityToken: string;
  authorizationCode: string;
  name: string | null;
  email: string;
  provider: 'google' | 'apple';
}

export const loginWithGoogle = async (
  callbackWhenSuccess: ({
    identityToken,
    authorizationCode,
    name,
    email,
    provider,
  }: LoginWithGoogleCallback) => void,
): Promise<void> => {
  const defaultConfig: ConfigureParams = {
    offlineAccess: false,
    hostedDomain: '',
    forceCodeForRefreshToken: false,
    accountName: '봄봄',
    googleServicePlistPath: 'GoogleService-Info.plist',
    profileImageSize: 120,
  };
  if (Platform.OS === 'ios') {
    GoogleSignin.configure({
      ...defaultConfig,
      webClientId: ENV.webClientId,
      iosClientId: ENV.iosClientId,
    });
  } else {
    GoogleSignin.configure({
      ...defaultConfig,
      webClientId: ENV.webClientId,
    });
  }

  await GoogleSignin.hasPlayServices();

  const userInfo = await GoogleSignin.signIn();

  if (userInfo?.data?.idToken) {
    callbackWhenSuccess({
      identityToken: userInfo.data.idToken,
      authorizationCode: userInfo.data.serverAuthCode ?? '',
      name: userInfo.data?.user?.name ?? '',
      email: userInfo.data?.user?.email ?? '',
      provider: 'google',
    });
  } else {
    throw new Error('ID 토큰을 가져올 수 없습니다.');
  }
};

export const loginWithApple = async (
  callbackWhenSuccess: ({
    identityToken,
    authorizationCode,
    provider,
  }: LoginWithGoogleCallback) => void,
): Promise<void> => {
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
    callbackWhenSuccess({
      identityToken: credential.identityToken,
      authorizationCode: credential.authorizationCode,
      name: `${credential.fullName?.familyName ?? ''}${credential.fullName?.givenName ?? ''}`,
      email: credential.email ?? '',
      provider: 'apple',
    });
  } else {
    throw new Error('Apple 로그인 정보를 가져올 수 없습니다.');
  }
};
