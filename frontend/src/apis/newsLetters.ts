import { fetcher } from './fetcher';
import { components } from '@/types/openapi';

type GetNewslettersResponse = components['schemas']['NewsletterResponse'][];

export const getNewsletters = async () => {
  return await fetcher.get<GetNewslettersResponse>({
    path: '/newsletters',
  });
};

export interface NewsletterDetailResponse {
  name: string;
  description: string;
  imageUrl: string;
  categoryId: number;
  mainPageUrl: string;
  subscribeUrl: string;
  issueCycle: string;
  subscribePageImageUrl?: string;
  previousNewsletterUrl?: string;
}

export interface GetNewsletterDetailParams {
  newsletterId: number;
}

export const getNewsletterDetail = async ({
  newsletterId,
}: GetNewsletterDetailParams) => {
  return await fetcher.get<NewsletterDetailResponse>({
    path: `/newsletters/${newsletterId}`,
  });
};
