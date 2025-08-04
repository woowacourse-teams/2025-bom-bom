import { fetcher } from './fetcher';
import { components } from '@/types/openapi';

export const postSignup = async ({
  nickname,
  email,
  gender,
}: components['schemas']['MemberSignupRequest']) => {
  return await fetcher.post<
    components['schemas']['MemberSignupRequest'],
    never
  >({
    path: '/auth/signup',
    body: {
      nickname,
      gender,
      email: `${email}@bombom.news`,
    },
  });
};
