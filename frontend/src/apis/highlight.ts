import { fetcher } from './fetcher';
import { HighlightType } from '@/pages/detail/types/highlight';
import { components, operations } from '@/types/openapi';

export type GetHighlightsParams =
  operations['getHighlights']['parameters']['query'] & {
    newsletterId?: number;
  };
export type GetHighlightsResponse = HighlightType[];

export const getHighlights = async (params: GetHighlightsParams) => {
  return await fetcher.get<GetHighlightsResponse>({
    path: '/highlights',
    query: params,
  });
};

export type PostHighlightParams =
  components['schemas']['HighlightCreateRequest'];
export type PostHighlightResponse = HighlightType;

export const postHighlight = async (params: PostHighlightParams) => {
  return await fetcher.post<PostHighlightParams, PostHighlightResponse>({
    path: '/highlights',
    body: params,
  });
};

export type PatchHighlightParams =
  operations['updateHighlight']['parameters']['path'];
export type PatchHighlightRequest =
  components['schemas']['UpdateHighlightRequest'];

export const patchHighlight = async ({
  id,
  color,
  memo,
}: PatchHighlightParams & PatchHighlightRequest) => {
  return await fetcher.patch({
    path: `/highlights/${id}`,
    body: {
      color,
      memo,
    },
  });
};

export type DeleteHighlightParams =
  operations['deleteHighlight']['parameters']['path'];

export const deleteHighlight = async ({ id }: DeleteHighlightParams) => {
  return await fetcher.delete({
    path: `/highlights/${id.toString()}`,
  });
};

export interface GetHighlightStatisticsNewsletterResponse {
  totalCount: number;
  newsletters: {
    id: number;
    name: string;
    imageUrl: string;
    highlightCount: number;
  }[];
}

export const getHighlightStatisticsNewsletter = async () => {
  return await fetcher.get<GetHighlightStatisticsNewsletterResponse>({
    path: '/highlights/statistics/newsletters',
  });
};
