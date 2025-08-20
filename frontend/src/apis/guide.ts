import { fetcher } from './fetcher';

export const patchGuideArticleRead = async () => {
  return await fetcher.patch({
    path: `/guide/read`,
  });
};
