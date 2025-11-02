import { fetcher } from '@bombom/shared/apis';

export const patchGuideArticleRead = async () => {
  return await fetcher.patch({
    path: `/guide/read`,
  });
};
