import { fetcher } from './fetcher';

interface PostSignupParams {
  nickname: string;
  birthdate: string;
  gender: string;
}

export const postSignup = async ({
  nickname,
  birthdate,
  gender,
}: PostSignupParams) => {
  return await fetcher.post({
    path: '/auth/signup',
    body: {
      nickname: '피터',
      // birthdate: '2025-07-23',
      gender: 'MALE',
      // email: 'test@bombom.news',
    },
  });
};
