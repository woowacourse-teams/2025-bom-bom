import { fetcher } from './fetcher';

interface PostSignupParams {
  nickname: string;
  email: string;
  gender: string;
}

export const postSignup = async ({
  nickname,
  email,
  gender,
}: PostSignupParams) => {
  return await fetcher.post({
    path: '/auth/signup',
    body: {
      nickname,
      gender,
      email: `${email}@bombom.news`,
    },
  });
};
