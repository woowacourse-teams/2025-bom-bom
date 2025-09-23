import { fetcher } from './fetcher';
import type { components, operations } from '@/types/openapi';

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

export type PostNativeLoginParams = components['schemas']['NativeLoginRequest'];
export type PostNativeLoginResponse =
  components['schemas']['NativeLoginResponse'];

export const postGoogleLogin = async (params: PostNativeLoginParams) => {
  return await fetcher.post<PostNativeLoginParams, PostNativeLoginResponse>({
    path: '/auth/login/google/native',
    body: params,
  });
};

export const postAppleLogin = async (params: PostNativeLoginParams) => {
  return await fetcher.post<PostNativeLoginParams, PostNativeLoginResponse>({
    path: '/auth/login/apple/native',
    body: params,
  });
};
