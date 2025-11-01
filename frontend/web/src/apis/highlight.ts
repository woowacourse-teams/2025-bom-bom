import { fetcher } from '@bombom/shared/apis';
import type { components, operations } from '@/types/openapi';

export type GetHighlightsParams = Omit<
  operations['getHighlights']['parameters']['query'],
  'pageable'
>;
export type GetHighlightsResponse =
  components['schemas']['PageHighlightResponse'];

export const getHighlights = async (params: GetHighlightsParams) => {
  return await fetcher.get<GetHighlightsResponse>({
    path: '/highlights',
    query: params,
  });
};

export type PostHighlightParams =
  components['schemas']['HighlightCreateRequest'];
export type PostHighlightResponse = components['schemas']['HighlightResponse'];

export const postHighlight = async (params: PostHighlightParams) => {
  return await fetcher.post<PostHighlightParams, PostHighlightResponse>({
    path: '/highlights',
    body: params,
  });
};

export type PatchHighlightParams =
  operations['updateHighlight']['parameters']['path'] &
    components['schemas']['UpdateHighlightRequest'];

export const patchHighlight = async ({
  id,
  color,
  memo,
}: PatchHighlightParams) => {
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

export type GetHighlightStatisticsNewsletterResponse =
  components['schemas']['HighlightStatisticsResponse'];

export const getHighlightStatisticsNewsletter = async () => {
  return await fetcher.get<GetHighlightStatisticsNewsletterResponse>({
    path: '/highlights/statistics/newsletters',
  });
};
