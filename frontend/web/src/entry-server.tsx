import createCache from '@emotion/cache';
import { CacheProvider, Global } from '@emotion/react';
import createEmotionServer from '@emotion/server/create-instance';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import {
  createMemoryHistory,
  createRouter,
  RouterProvider,
} from '@tanstack/react-router';
import { StrictMode } from 'react';
import { renderToPipeableStream } from 'react-dom/server';
import { routeTree } from './routeTree.gen';
import reset from './styles/reset';
import type { Writable } from 'stream';

interface RenderResult {
  html: string;
  css: string;
  dehydratedState?: string;
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

  // Router 로드 및 데이터 프리페칭
  await router.load();

  // Emotion 캐시 생성
  const cache = createCache({ key: 'css' });
  const { extractCriticalToChunks, constructStyleTagsFromChunks } =
    createEmotionServer(cache);

  // Promise를 사용하여 스트림 완료 대기
  const html = await new Promise<string>((resolve, reject) => {
    let htmlContent = '';

    const writable: Writable = {
      write(chunk: string) {
        htmlContent += chunk;
        return true;
      },
      end() {
        resolve(htmlContent);
      },
      on() {
        return this;
      },
      once() {
        return this;
      },
      emit() {
        return false;
      },
    } as unknown as Writable;

    const { pipe } = renderToPipeableStream(
      <StrictMode>
        <CacheProvider value={cache}>
          <QueryClientProvider client={queryClient}>
            <Global styles={reset} />
            <RouterProvider router={router} />
          </QueryClientProvider>
        </CacheProvider>
      </StrictMode>,
      {
        onShellReady() {
          pipe(writable);
        },
        onError(error) {
          reject(error);
        },
      },
    );
  });

  // Critical CSS 추출
  const emotionChunks = extractCriticalToChunks(html);
  const emotionCss = constructStyleTagsFromChunks(emotionChunks);

  return {
    html,
    css: emotionCss,
  };
}
