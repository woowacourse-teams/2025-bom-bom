import { fetcher } from './fetcher';
import { NewslettersResponse } from '../pages/today/types/article';

export const getNewsletters = async () => {
  return await fetcher.get<NewslettersResponse>({
    path: '/newsletters',
  });
};
