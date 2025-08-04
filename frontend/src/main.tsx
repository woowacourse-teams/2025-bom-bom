import { Global } from '@emotion/react';
import { init as initSentry } from '@sentry/react';
import { QueryClient } from '@tanstack/react-query';
import { RouterProvider, createRouter } from '@tanstack/react-router';
import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { ENV } from './apis/env.ts';
import { routeTree } from './routeTree.gen';
import reset from './styles/reset.ts';

initSentry({
  dsn: ENV.sentryDsn,
  sendDefaultPii: true,
});

export const queryClient = new QueryClient();

const router = createRouter({
  routeTree,
  context: {
    queryClient,
  },
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
    });
  }
}

enableMocking().then(() => {
  createRoot(document.getElementById('root')!).render(
    <StrictMode>
      <Global styles={reset} />
      <RouterProvider router={router} />
    </StrictMode>,
  );
});
