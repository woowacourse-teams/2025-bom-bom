import { Global } from '@emotion/react';
import Clarity from '@microsoft/clarity';
import {
  init as initSentry,
  tanstackRouterBrowserTracingIntegration,
} from '@sentry/react';
import { QueryClient } from '@tanstack/react-query';
import { RouterProvider, createRouter } from '@tanstack/react-router';
import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { ENV } from './apis/env.ts';
import GAInitializer from './libs/googleAnalytics/GAInitializer.tsx';
import { routeTree } from './routeTree.gen';
import reset from './styles/reset.ts';
import { isDevelopment, isProduction } from './utils/environment.ts';

if (isProduction) Clarity.init(ENV.clarityProjectId);

export const queryClient = new QueryClient();

const router = createRouter({
  routeTree,
  context: {
    queryClient,
  },
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
}

async function enableMocking() {
  if (ENV.enableMsw === 'true') {
    const { worker } = await import('./mocks/browser');

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
  createRoot(document.getElementById('root')!).render(
    <StrictMode>
      <Global styles={reset} />
      <RouterProvider router={router} />
      <GAInitializer />
    </StrictMode>,
  );
});
