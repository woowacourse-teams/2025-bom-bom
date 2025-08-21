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
