import createCache from '@emotion/cache';
import { CacheProvider } from '@emotion/react';
import createEmotionServer from '@emotion/server/create-instance';
import { QueryClient } from '@tanstack/react-query';
import {
  createMemoryHistory,
  createRouter,
  RouterProvider,
} from '@tanstack/react-router';
import { renderToString } from 'react-dom/server';
import { routeTree } from './routeTree.gen';
import reset from './styles/reset';

interface RenderResult {
  html: string;
  css: string;
  dehydratedState?: string;
}

export async function render(url: string): Promise<RenderResult> {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: {
        staleTime: 60 * 1000,
        retry: false,
      },
    },
  });

  const memoryHistory = createMemoryHistory({
    initialEntries: [url],
  });

  const router = createRouter({
    routeTree,
    history: memoryHistory,
    context: {
      queryClient,
    },
  });

  // Router 로드 및 데이터 프리페칭
  await router.load();

  // Emotion 캐시 생성
  const cache = createCache({ key: 'css' });
  const { extractCriticalToChunks, constructStyleTagsFromChunks } =
    createEmotionServer(cache);

  // 서버 렌더링
  const html = renderToString(
    <CacheProvider value={cache}>
      <RouterProvider router={router} />
    </CacheProvider>,
  );

  // Critical CSS 추출
  const emotionChunks = extractCriticalToChunks(html);
  const emotionCss = constructStyleTagsFromChunks(emotionChunks);

  // Reset styles를 문자열로 변환
  const resetCss = `<style>${reset.styles}</style>`;

  return {
    html,
    css: resetCss + emotionCss,
  };
}
