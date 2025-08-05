import { fetcher } from './fetcher';
import { components } from '@/types/openapi';

export const getNewsletters = async () => {
  return await fetcher.get<components['schemas']['NewsletterResponse'][]>({
    path: '/newsletters',
  });
};
