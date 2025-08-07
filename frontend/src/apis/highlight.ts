import { fetcher } from './fetcher';
import { HighlightType } from '@/pages/detail/types/highlight';
import { components, operations } from '@/types/openapi';

export type GetHighlightsParams =
  operations['getHighlights']['parameters']['query'];
export type GetHighlightsResponse = HighlightType[];

export const getHighlights = async (params: GetHighlightsParams) => {
  return await fetcher.get<GetHighlightsResponse>({
    path: '/highlights',
    query: params,
  });
};

export type PostHighlightParams = {
  highlight: components['schemas']['HighlightCreateRequest'];
};

export const postHighlight = async (params: PostHighlightParams) => {
  return await fetcher.post({
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
