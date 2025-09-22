import { Env } from '@/constants/env';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { GoogleSignin } from '@react-native-google-signin/google-signin';
import * as AppleAuthentication from 'expo-apple-authentication';

interface LoginWithGoogleCallback {
  identityToken: string;
  authorizationCode: string;
  provider: 'google' | 'apple';
}

export const loginWithGoogle = async (
  callbackWhenSuccess: ({
    identityToken,
    authorizationCode,
    provider,
  }: LoginWithGoogleCallback) => void,
): Promise<void> => {
  GoogleSignin.configure({
    webClientId: Env.IOS_CLIENT_ID,
    offlineAccess: true,
    hostedDomain: '',
    forceCodeForRefreshToken: true,
    accountName: '',
    iosClientId: Env.IOS_CLIENT_ID,
    googleServicePlistPath: '',
    profileImageSize: 120,
  });

  await GoogleSignin.hasPlayServices();

  const userInfo = await GoogleSignin.signIn();

  if (userInfo?.data?.idToken) {
    callbackWhenSuccess({
      identityToken: userInfo.data.idToken,
      authorizationCode: userInfo.data.serverAuthCode ?? '',
      provider: 'google',
    });

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
      provider: 'apple',
    });

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
