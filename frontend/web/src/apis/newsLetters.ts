import { fetcher } from './fetcher';
import type { components, operations } from '@/types/openapi';

type GetNewslettersResponse = components['schemas']['NewsletterResponse'][];

export const getNewsletters = async () => {
  return await fetcher.get<GetNewslettersResponse>({
    path: '/newsletters',
  });
};

export type GetNewsletterDetailParams =
  operations['getNewsletterWithDetail']['parameters']['path'];
export type GetNewsletterWithDetailResponse =
  components['schemas']['NewsletterWithDetailResponse'];

export const getNewsletterDetail = async ({
  id,
}: GetNewsletterDetailParams) => {
  return await fetcher.get<GetNewsletterWithDetailResponse>({
    path: `/newsletters/${id}`,
  });
};

export type GetMyNewslettersResponse =
  components['schemas']['NewsletterResponse'][];

export const getMyNewsletters = async () => {
  return await fetcher.get<GetMyNewslettersResponse>({
    path: '/members/me/newsletters',
  });
};
