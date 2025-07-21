import { fetcher } from '../../../apis/fetcher';
import { NewslettersResponse } from '../types/article';

export const getNewsletters = async () => {
  return await fetcher.get<NewslettersResponse>({
    path: '/newsletters',
  });
};
