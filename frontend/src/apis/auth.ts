import { fetcher } from './fetcher';
import { components, operations } from '@/types/openapi';

export type PostSignupParams = components['schemas']['MemberSignupRequest'];

export const postSignup = async (params: PostSignupParams) => {
  return await fetcher.post<PostSignupParams, never>({
    path: '/auth/signup',
    body: params,
  });
};

export type GetSignupCheckParams =
  operations['checkSignupDuplicate']['parameters']['query']['request'];

export const getSignupCheck = (params: GetSignupCheckParams) => {
  return fetcher.get({
    path: '/auth/signup/check',
    query: params,
  });
};

export const postLogout = () => {
  return fetcher.post({
    path: '/auth/logout',
  });
};

export const postWithdraw = () => {
  return fetcher.post({
    path: '/auth/withdraw',
  });
};

// Google 로그인 (앱에서 받은 idToken으로)
export const postGoogleLogin = async ({
  identityToken,
  authorizationCode,
}: components['schemas']['NativeLoginRequest']) => {
  return await fetcher.post({
    path: '/auth/login/google/native',
    body: {
      identityToken,
      authorizationCode,
    },
  });
};

// Apple 로그인 (앱에서 받은 identityToken으로)
export const postAppleLogin = async ({
  identityToken,
  authorizationCode,
}: components['schemas']['NativeLoginRequest']) => {
  return await fetcher.post({
    path: '/auth/login/apple/native',
    body: {
      identityToken,
      authorizationCode,
    },
  });
};
