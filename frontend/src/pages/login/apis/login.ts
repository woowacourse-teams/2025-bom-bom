import { fetcher } from '../../../apis/fetcher';

export const login = async () => {
  return await fetcher.get({ path: '/auth/login/google' });
};
