import createCache from '@emotion/cache';
import { CacheProvider, Global } from '@emotion/react';
import createEmotionServer from '@emotion/server/create-instance';
import {
  QueryClient,
  QueryClientProvider,
  dehydrate,
} from '@tanstack/react-query';
import {
  createMemoryHistory,
  createRouter,
  RouterProvider,
} from '@tanstack/react-router';
import { StrictMode } from 'react';
import { renderToString } from 'react-dom/server';
import { queries } from './apis/queries';
import { routeTree } from './routeTree.gen';
import reset from './styles/reset';

interface RenderResult {
  html: string;
  css: string;
  dehydratedState: string;
}

export async function render(url: string): Promise<RenderResult> {
  // 각 요청마다 새로운 QueryClient 생성
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

  if (url === '/') {
    try {
      await Promise.all([
        queryClient.prefetchQuery(queries.newsletters()),
        queryClient.prefetchQuery(queries.monthlyReadingRank({ limit: 10 })),
        // queryClient.prefetchQuery(queries.myMonthlyReadingRank()), // 서버와 클라이언트 쿠키 동기화 이후
        // queryClient.prefetchQuery(queries.userProfile()), // 서버와 클라이언트 쿠키 동기화 이후
      ]);
    } catch (error) {
      // 프리페칭 실패 시에도 렌더링은 계속 진행
      console.warn('[SSR] Data prefetching failed:', error);
    }
  }

  // Router 로드 및 데이터 프리페칭
  await router.load();

  // Emotion 캐시 생성
  const cache = createCache({ key: 'css' });
  const { extractCriticalToChunks, constructStyleTagsFromChunks } =
    createEmotionServer(cache);

  // renderToString을 사용하여 HTML 생성
  const html = renderToString(
    <StrictMode>
      <CacheProvider value={cache}>
        <QueryClientProvider client={queryClient}>
          <Global styles={reset} />
          <RouterProvider router={router} />
        </QueryClientProvider>
      </CacheProvider>
    </StrictMode>,
  );

  // Critical CSS 추출
  const emotionChunks = extractCriticalToChunks(html);
  const emotionCss = constructStyleTagsFromChunks(emotionChunks);

  // QueryClient 상태를 dehydrate하여 직렬화
  const dehydratedState = JSON.stringify(dehydrate(queryClient));

  return {
    html,
    css: emotionCss,
    dehydratedState,
  };
}
