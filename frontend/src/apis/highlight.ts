import { fetcher } from './fetcher';
import { HighlightType } from '@/pages/detail/types/highlight';

interface PostHighlightParams {
  highlight: Omit<HighlightType, 'id'>;
}

interface PatchHighlightParams {
  id: string;
  data: Partial<HighlightType>;
}

interface DeleteHighlightParams {
  id: string;
}

export const getHighlights = async () => {
  return await fetcher.get<HighlightType[]>({
    path: '/highlight',
  });
};

export const postHighlight = async ({ highlight }: PostHighlightParams) => {
  return await fetcher.post({
    path: '/highlight',
    body: highlight,
  });
};

export const patchHighlight = async ({ id, data }: PatchHighlightParams) => {
  return await fetcher.patch({
    path: `/highlight/${id}`,
    body: data,
  });
};

export const deleteHighlight = async ({ id }: DeleteHighlightParams) => {
  return await fetcher.delete({
    path: `/highlight/${id}`,
  });
};
