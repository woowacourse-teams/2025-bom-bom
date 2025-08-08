import { fetcher } from './fetcher';
import { components } from '@/types/openapi';

type GetNewslettersResponse = components['schemas']['NewsletterResponse'][];

export const getNewsletters = async () => {
  return await fetcher.get<GetNewslettersResponse>({
    path: '/newsletters',
  });
};
