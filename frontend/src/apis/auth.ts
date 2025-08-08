import { fetcher } from './fetcher';
import { components } from '@/types/openapi';

type PostSignupParams = components['schemas']['MemberSignupRequest'];

export const postSignup = async ({
  nickname,
  email,
  gender,
}: PostSignupParams) => {
  return await fetcher.post<PostSignupParams, never>({
    path: '/auth/signup',
    body: {
      nickname,
      gender,
      email: `${email}@bombom.news`,
    },
  });
};
