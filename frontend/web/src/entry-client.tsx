import { Global } from '@emotion/react';
import {
  init as initSentry,
  tanstackRouterBrowserTracingIntegration,
} from '@sentry/react';
import {
  type DehydratedState,
  HydrationBoundary,
  QueryClient,
  QueryClientProvider,
} from '@tanstack/react-query';
import { RouterProvider, createRouter } from '@tanstack/react-router';
import { StrictMode } from 'react';
import { hydrateRoot } from 'react-dom/client';
import { ENV } from './apis/env';
import GAInitializer from './libs/googleAnalytics/GAInitializer';
import { routeTree } from './routeTree.gen';
import reset from './styles/reset';
import { isDevelopment } from './utils/environment';

// 서버에서 전달받은 dehydrated state 타입 정의
declare global {
  interface Window {
    __REACT_QUERY_STATE__?: DehydratedState;
  }
}

// if (isProduction) Clarity.init(ENV.clarityProjectId);

export const queryClient = new QueryClient();

const router = createRouter({
  routeTree,
  context: {
    queryClient,
  },
  scrollRestoration: true,
});

initSentry({
  dsn: ENV.sentryDsn,
  sendDefaultPii: true,
  integrations: [tanstackRouterBrowserTracingIntegration(router)],
  sampleRate: isDevelopment ? 1 : 0.1,
});

declare module '@tanstack/react-router' {
  interface Register {
    router: typeof router;
  }
  interface HistoryState {
    subscribeUrl: string;
  }
}

async function enableMocking() {
  if (ENV.enableMsw === 'true') {
    const { worker } = await import('./mocks/browser.ts');

    return worker.start({
      onUnhandledRequest: 'bypass',
      serviceWorker: {
        url: '/mockServiceWorker.js',
        options: {
          scope: '/',
        },
      },
      waitUntilReady: true,
    });
  }
}

enableMocking().then(() => {
  // 서버에서 전달받은 dehydrated state 가져오기
  const dehydratedState = window.__REACT_QUERY_STATE__;

  hydrateRoot(
    document.getElementById('root')!,
    <StrictMode>
      <QueryClientProvider client={queryClient}>
        <HydrationBoundary state={dehydratedState}>
          <Global styles={reset} />
          <RouterProvider router={router} />
          <GAInitializer />
        </HydrationBoundary>
      </QueryClientProvider>
    </StrictMode>,
  );
});
