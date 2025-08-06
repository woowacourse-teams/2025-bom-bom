import { fetcher } from './fetcher';
import { HighlightType } from '@/pages/detail/types/highlight';
import { components, operations } from '@/types/openapi';

export type GetHighlightsParams = Omit<
  operations['getHighlights']['parameters']['query'],
  'member'
>;
export type GetHighlightsResponse = HighlightType[];

export const getHighlights = async ({ articleId }: GetHighlightsParams) => {
  return await fetcher.get<GetHighlightsResponse>({
    path: '/highlights',
    query: {
      articleId,
    },
  });
};

export type PostHighlightParams = {
  highlight: components['schemas']['HighlightCreateRequest'];
};

export const postHighlight = async ({ highlight }: PostHighlightParams) => {
  return await fetcher.post({
    path: '/highlights',
    body: highlight,
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
